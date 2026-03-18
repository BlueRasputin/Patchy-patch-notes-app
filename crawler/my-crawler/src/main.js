import { PlaywrightCrawler, Dataset } from 'crawlee';
// list of sources for the ccrawler to scrape patch notes from
const sources = [
    { 
        url: 'https://react.dev/blog/2025/10/01/react-19-2',
        techName: 'React'
    },
    { 
        url: 'https://www.oracle.com/java/technologies/javase/25all-relnotes.html',
        techName: 'Java'
    },
    { 
        url: 'https://www.python.org/downloads/release/python-3139/',
        techName: 'Python'
    },
    { 
        url: 'https://nodejs.org/en/blog/release/v24.10.0',
        techName: 'Node.js'
    },
    { 
        url: 'https://github.com/rails/rails/releases/tag/v8.1.0',
        techName: 'Ruby on Rails'
    },
    { 
        url: 'https://spring.io/blog/2025/10/09/spring-batch-6-0-0-m4-released',
        techName: 'Spring'
    }
];



const crawler = new PlaywrightCrawler({
    async requestHandler({ request, page, log, pushData }) {
        try {
            log.info(`Scraping: ${request.loadedUrl}`);

            const source = sources.find(s => request.loadedUrl.includes(new URL(s.url).hostname));
            if (!source) {
                log.warning(`Unknown source: ${request.loadedUrl}`);
                return;
            }

            const extracted = await page.evaluate((techName) => {
                //Remove formatting and scripts that may interfere with text extraction
                const scripts = document.querySelectorAll('script, style, nav, header, footer');
                scripts.forEach(el => el.remove());

                const extractStructuredSpringContent = () => {
                    const title = document.querySelector('.column.is-9.pr-6 .blog-post.mb-4 h1')?.textContent?.trim();
                    const body = document.querySelector('.column.is-9.pr-6 .markdown');
                    if (!body) {
                        return null;
                    }

                    const parts = [];
                    if (title) {
                        parts.push(title);
                    }

                    const nodes = Array.from(body.querySelectorAll('h2, h3, p, li'));
                    for (const node of nodes) {
                        const clone = node.cloneNode(true);
                        clone.querySelectorAll('a[href^=\"#\"]').forEach((anchor) => anchor.remove());

                        const text = clone.textContent.replace(/\s+/g, ' ').trim();
                        if (!text) {
                            continue;
                        }

                        if (
                            text.includes('Spring Batch Home') ||
                            text.includes('Source on Github') ||
                            text.includes('Reference documentation')
                        ) {
                            continue;
                        }

                        parts.push(text);
                    }

                    return parts.join('\n');
                };

                const selectMainContent = (name) => {
                    // Custom selectors for each patch notes page to limit redundant or irrelevant information.
                    if (name === 'React') {
                        return document.querySelector('.min-w-0.isolate, article, main');
                    }
                    if (name === 'Java') {
                        const sections = Array.from(document.querySelectorAll('.cc01w1.cwidth'));
                        if (sections.length > 0) {
                            const combined = document.createElement('div');
                            sections.forEach((section) => combined.appendChild(section.cloneNode(true)));
                            return combined;
                        }
                        return document.querySelector('.f11w1, main, article');
                    }
                    if (name === 'Python') {
                        return document.querySelector('.main-content, article, main');
                    }
                    if (name === 'Node.js') {
                        return document.querySelector('.layouts-module_mzYk8q_postLayout, article, main');
                    }
                    if (name === 'Ruby on Rails') {
                        return document.querySelector('.Box-body, main, article');
                    }
                    if (name === 'Spring') {
                        return document.querySelector('.column.is-9.pr-6 .blog-post, .column.is-9.pr-6, main');
                    }
                    return document.querySelector('main, article, .content');
                };

                let normalizedContent;

                if (techName === 'Spring') {
                    const springContent = extractStructuredSpringContent();
                    normalizedContent = springContent
                        ? springContent.replace(/\s+/g, ' ').trim()
                        : '';
                } else {
                    const mainContent = selectMainContent(techName);
                    const workingNode = mainContent ? mainContent.cloneNode(true) : document.body.cloneNode(true);

                    const content = workingNode.textContent;
                    normalizedContent = content.replace(/\s+/g, ' ').trim();
                }

                const headingCandidates = [
                    document.querySelector('h1')?.textContent,
                    document.querySelector('title')?.textContent
                ].filter(Boolean);

                const versionRegex = /\bv?\d+\.\d+(\.\d+)?([-.][A-Za-z0-9]+)?\b/;
                let releaseVersion = null;
                for (const heading of headingCandidates) {
                    const match = heading.match(versionRegex);
                    if (match) {
                        releaseVersion = match[0];
                        break;
                    }
                }

                return { content: normalizedContent, releaseVersion };
            }, source.techName);

            if (extracted.content && extracted.content.length > 100) {
                // save data to local storage (in case of failure to connect to backend)
                await pushData({
                    techName: source.techName,
                    url: request.loadedUrl,
                    content: extracted.content,
                    releaseVersion: extracted.releaseVersion,
                    scrapedAt: new Date().toISOString()
                });
                
                log.info(`Saved content for ${source.techName} (${extracted.content.length} chars)`);
            } else {
                log.warning(`Insufficient content found for ${source.techName} (${extracted.content ? extracted.content.length : 0} chars)`);
            }

        } catch (error) {
            log.error(`Error processing ${request.loadedUrl}:`, error.message);
        }
    },
    maxRequestsPerCrawl: 10,
    headless: true,
});

console.log('Ahoy, starting to sail...');
await crawler.run(sources.map(s => s.url));

const dataset = await Dataset.open();
const results = await dataset.getData();

console.log(`Plundered ${results.items.length} patch note pages`);

if (results.items.length > 0) {
    try {
        console.log('Sending data to backend...');
        // send data to backend
        const response = await fetch('http://localhost:8080/api/process-crawled-notes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(results.items)
        });
        
        if (response.ok) {
            const responseText = await response.text();
            console.log('Backend response:', responseText);
        } else {
            console.error('Backend error:', response.status, await response.text());
        }
    } catch (error) {
        console.error('Network error:', error.message);
        console.log('Data saved locally in ./storage/datasets/default/');
    }
} else {
    console.log('No data to send to backend');
}
