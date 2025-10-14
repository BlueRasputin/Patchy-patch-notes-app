
import { PlaywrightCrawler, Dataset } from 'crawlee';

const sources = [
    { 
        url: 'https://github.com/facebook/react/releases',
        techName: 'React',
        type: 'github_releases'
    },
    { 
        url: 'https://www.oracle.com/java/technologies/javase/25all-relnotes.html',
        techName: 'Java',
        type: 'oracle_notes'
    },
    { 
        url: 'https://www.python.org/downloads/',
        techName: 'Python',
        type: 'python_releases'
    },
    { 
        url: 'https://nodejs.org/en/blog/release/',
        techName: 'Node.js',
        type: 'nodejs_blog'
    },
    { 
        url: 'https://rubyonrails.org/releases/',
        techName: 'Ruby on Rails',
        type: 'rails_releases'
    }
];

const crawler = new PlaywrightCrawler({
    async requestHandler({ request, page, log, pushData }) {
        try {
            const title = await page.title();
            log.info(`Scraping: ${request.loadedUrl}`);

            // Find which tech this URL belongs to
            const source = sources.find(s => request.loadedUrl.includes(new URL(s.url).hostname));
            if (!source) {
                log.warning(`Unknown source: ${request.loadedUrl}`);
                return;
            }

            // Extract content and version based on source type
            const extractedData = await extractPatchNoteData(page, source);

            if (extractedData.content && extractedData.content.length > 200) {
                await pushData({
                    techName: source.techName,
                    url: request.loadedUrl,
                    title: title,
                    version: extractedData.version,
                    content: extractedData.content,
                    releaseDate: extractedData.releaseDate,
                    scrapedAt: new Date().toISOString()
                });
                
                log.info(`Saved patch notes for ${source.techName} ${extractedData.version}`);
            } else {
                log.warning(`Insufficient content found for ${source.techName}`);
            }

        } catch (error) {
            log.error(`Error processing ${request.loadedUrl}:`, error.message);
        }
    },
    maxRequestsPerCrawl: 10,
    headless: true,
});

async function extractPatchNoteData(page, source) {
    switch (source.type) {
        case 'github_releases':
            return await extractGitHubData(page);
        case 'oracle_notes':
            return await extractOracleData(page);
        case 'python_releases':
            return await extractPythonData(page);
        case 'nodejs_blog':
            return await extractNodeJSData(page);
        case 'rails_releases':
            return await extractRailsData(page);
        default:
            return await extractGenericData(page);
    }
}

async function extractGitHubData(page) {
    return await page.evaluate(() => {
        // Get the latest release
        const releaseElement = document.querySelector('.Box-row, .release');
        if (!releaseElement) return { content: '', version: 'Unknown' };

        // Extract version
        const versionElement = releaseElement.querySelector('h1 a, .f1 a, .release-title a');
        const version = versionElement?.textContent?.trim() || 'Latest';

        // Extract content
        const contentElement = releaseElement.querySelector('.markdown-body, .release-body');
        const content = contentElement?.textContent?.trim() || releaseElement.textContent?.trim() || '';

        // Extract date
        const dateElement = releaseElement.querySelector('relative-time, time');
        const releaseDate = dateElement?.getAttribute('datetime') || dateElement?.textContent?.trim() || '';

        return { version, content, releaseDate };
    });
}

async function extractOracleData(page) {
    return await page.evaluate(() => {
        const contentArea = document.querySelector('.u30-contentArea, .cc01w1, main');
        if (!contentArea) return { content: '', version: 'Unknown' };

        // Look for version in headings
        const versionElement = contentArea.querySelector('h1, h2, h3');
        const version = versionElement?.textContent?.match(/\d+\.\d+[\.\d]*/)?.[0] || 'Latest';

        const content = contentArea.textContent?.trim() || '';

        return { version, content, releaseDate: '' };
    });
}

async function extractPythonData(page) {
    return await page.evaluate(() => {
        const releaseElement = document.querySelector('.download-list-widget .release-number, .release');
        if (!releaseElement) return { content: '', version: 'Unknown' };

        // Extract version
        const versionLink = releaseElement.querySelector('a');
        const version = versionLink?.textContent?.trim() || 'Latest';

        // Get content from the page
        const contentElement = document.querySelector('.release-content, main, .content');
        const content = contentElement?.textContent?.trim() || document.body.textContent?.trim() || '';

        return { version, content, releaseDate: '' };
    });
}

async function extractNodeJSData(page) {
    return await page.evaluate(() => {
        const article = document.querySelector('article, .blog-post');
        if (!article) return { content: '', version: 'Unknown' };

        // Extract version from title
        const titleElement = article.querySelector('h1, .blog-post-title');
        const title = titleElement?.textContent || '';
        const version = title.match(/v?\d+\.\d+[\.\d]*/)?.[0] || 'Latest';

        // Extract content
        const contentElement = article.querySelector('.blog-post-content, .content');
        const content = contentElement?.textContent?.trim() || article.textContent?.trim() || '';

        // Extract date
        const dateElement = article.querySelector('time, .blog-post-date');
        const releaseDate = dateElement?.getAttribute('datetime') || dateElement?.textContent?.trim() || '';

        return { version, content, releaseDate };
    });
}

async function extractRailsData(page) {
    return await page.evaluate(() => {
        const post = document.querySelector('.post, article');
        if (!post) return { content: '', version: 'Unknown' };

        // Extract version from title
        const titleElement = post.querySelector('h1, h2, .post-title');
        const title = titleElement?.textContent || '';
        const version = title.match(/\d+\.\d+[\.\d]*/)?.[0] || 'Latest';

        // Extract content
        const contentElement = post.querySelector('.post-content, .entry-content');
        const content = contentElement?.textContent?.trim() || post.textContent?.trim() || '';

        // Extract date
        const dateElement = post.querySelector('.post-date, time');
        const releaseDate = dateElement?.getAttribute('datetime') || dateElement?.textContent?.trim() || '';

        return { version, content, releaseDate };
    });
}

async function extractGenericData(page) {
    return await page.evaluate(() => {
        // Try to find version in title or headings
        const title = document.title;
        const headings = Array.from(document.querySelectorAll('h1, h2, h3')).map(h => h.textContent);
        const allText = [title, ...headings].join(' ');
        const version = allText.match(/v?\d+\.\d+[\.\d]*/)?.[0] || 'Latest';

        // Get main content
        const contentSelectors = ['article', '.content', 'main', '.post-content', '.markdown-body'];
        let content = '';
        
        for (const selector of contentSelectors) {
            const element = document.querySelector(selector);
            if (element && element.textContent.length > 100) {
                content = element.textContent.trim();
                break;
            }
        }
        
        if (!content) {
            content = document.body.textContent.trim();
        }

        return { version, content, releaseDate: '' };
    });
}

// Run the crawler
console.log('Starting patch note crawler...');
await crawler.run(sources.map(s => s.url));

// Get results and send to backend
const dataset = await Dataset.open();
const results = await dataset.getData();

console.log(`Crawled ${results.items.length} patch note pages`);

// Send to backend
if (results.items.length > 0) {
    try {
        const response = await fetch('http://localhost:8080/api/process-crawled-notes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(results.items)
        });
        
        if (response.ok) {
            console.log('Data sent to backend successfully');
        }
    } catch (error) {
        console.log('Backend not available, data saved locally');
    }
}