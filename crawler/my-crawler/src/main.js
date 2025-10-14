// For more information, see https://crawlee.dev/
import { PlaywrightCrawler } from 'crawlee';



const sources = [
    { url: 'https://github.com/facebook/react/releases'},
    { url: 'https://www.oracle.com/java/technologies/javase/25all-relnotes.html'},
    { url: 'https://www.python.org/downloads/'},
    { url: 'https://nodejs.org/en/blog/release/'},
    { url: 'https://rubyonrails.org/releases/'},
]

// PlaywrightCrawler crawls the web using a headless
// browser controlled by the Playwright library.
const crawler = new PlaywrightCrawler({
    // Use the requestHandler to process each of the crawled pages.
    async requestHandler({ request, page, enqueueLinks, log, pushData }) {
        const title = await page.title();
        log.info(`Title of ${request.loadedUrl} is '${title}'`);

        // Save results as JSON to ./storage/datasets/default
        await pushData({ title, url: request.loadedUrl });

        // Extract links from the current page
        // and add them to the crawling queue.
        await enqueueLinks();
    },
    // Comment this option to scrape the full website.
    maxRequestsPerCrawl: 50,
    // Uncomment this option to see the browser window.
    // headless: false,
});

// Add first URL to the queue and start the crawl.
await crawler.run(sources);
