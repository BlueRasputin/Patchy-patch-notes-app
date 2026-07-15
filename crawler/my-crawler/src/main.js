import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import { PlaywrightCrawler, Dataset } from 'crawlee';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const techCatalogPath = path.resolve(__dirname, '../../../server/src/main/resources/tech-catalog.json');
const targetTechs = new Set(
    (process.env.TARGET_TECHS ?? '')
        .split(',')
        .map((value) => value.trim().toLowerCase())
        .filter(Boolean)
);
const skipBackendUpload = process.env.SKIP_BACKEND_UPLOAD === 'true';

const sources = JSON.parse(readFileSync(techCatalogPath, 'utf8'))
    .filter((entry) => targetTechs.size === 0 || targetTechs.has(entry.name.toLowerCase()))
    .map((entry) => ({
    techName: entry.name,
    url: entry.patchNotesUrl,
    contentSelector: entry.contentSelector,
    contentStrategy: entry.contentStrategy ?? 'default'
}));



const crawler = new PlaywrightCrawler({
    async requestHandler({ request, page, log, pushData }) {
        try {
            log.info(`Scraping: ${request.loadedUrl}`);

            const source = sources.find(s => request.loadedUrl.includes(new URL(s.url).hostname));
            if (!source) {
                log.warning(`Unknown source: ${request.loadedUrl}`);
                return;
            }

            const extracted = await page.evaluate((sourceConfig) => {
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

                const extractLatestSectionContent = () => {
                    const container = document.querySelector(sourceConfig.contentSelector || 'main, article, .content');
                    if (!container) {
                        return null;
                    }

                    const versionHeadingRegex = /\b\d+\.\d+(\.\d+)?\b/;
                    const firstHeading = Array.from(container.querySelectorAll('h1, h2, h3'))
                        .find((heading) => versionHeadingRegex.test(heading.textContent?.trim() ?? ''));
                    if (!firstHeading) {
                        return null;
                    }

                    const parts = [];
                    let currentNode = firstHeading;

                    while (currentNode) {
                        if (
                            currentNode !== firstHeading &&
                            currentNode.matches &&
                            currentNode.matches('h1, h2') &&
                            versionHeadingRegex.test(currentNode.textContent?.trim() ?? '')
                        ) {
                            break;
                        }

                        const clone = currentNode.cloneNode(true);
                        clone.querySelectorAll('script, style, nav, header, footer').forEach((node) => node.remove());

                        const text = clone.textContent?.replace(/\s+/g, ' ').trim();
                        if (text) {
                            parts.push(text);
                        }

                        currentNode = currentNode.nextElementSibling;
                    }

                    return parts.join('\n');
                };

                const selectMainContent = () => {
                    if (sourceConfig.contentStrategy === 'java-sections') {
                        const sections = Array.from(document.querySelectorAll('.cc01w1.cwidth'));
                        if (sections.length > 0) {
                            const combined = document.createElement('div');
                            sections.forEach((section) => combined.appendChild(section.cloneNode(true)));
                            return combined;
                        }
                        return document.querySelector('.f11w1, main, article');
                    }

                    return document.querySelector(sourceConfig.contentSelector || 'main, article, .content');
                };

                let normalizedContent;

                if (sourceConfig.contentStrategy === 'spring-blog') {
                    const springContent = extractStructuredSpringContent();
                    normalizedContent = springContent
                        ? springContent.replace(/\s+/g, ' ').trim()
                        : '';
                } else if (sourceConfig.contentStrategy === 'latest-section') {
                    const latestSection = extractLatestSectionContent();
                    normalizedContent = latestSection
                        ? latestSection.replace(/\s+/g, ' ').trim()
                        : '';
                } else {
                    const mainContent = selectMainContent();
                    const workingNode = mainContent ? mainContent.cloneNode(true) : document.body.cloneNode(true);

                    const content = workingNode.textContent;
                    normalizedContent = content.replace(/\s+/g, ' ').trim();
                }

                const versionRegex = /\bv?\d+\.\d+(\.\d+)?([-.][A-Za-z0-9]+)?\b/;
                const headingCandidates = [
                    document.querySelector('h1')?.textContent,
                    ...Array.from(document.querySelectorAll('h2, h3'))
                        .slice(0, 12)
                        .map((heading) => heading.textContent),
                    document.querySelector('title')?.textContent
                ].filter(Boolean);
                let releaseVersion = null;
                for (const heading of headingCandidates) {
                    const match = heading.match(versionRegex);
                    if (match) {
                        releaseVersion = match[0];
                        break;
                    }
                }

                return { content: normalizedContent, releaseVersion };
            }, source);

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
    maxRequestsPerCrawl: Math.max(sources.length, 1),
    headless: true,
});

console.log('Ahoy, starting to sail...');
await crawler.run(sources.map(s => s.url));

const dataset = await Dataset.open();
const results = await dataset.getData();

console.log(`Plundered ${results.items.length} patch note pages`);

if (results.items.length > 0 && !skipBackendUpload) {
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
} else if (results.items.length > 0) {
    console.log('Skipping backend upload because SKIP_BACKEND_UPLOAD=true');
} else {
    console.log('No data to send to backend');
}
