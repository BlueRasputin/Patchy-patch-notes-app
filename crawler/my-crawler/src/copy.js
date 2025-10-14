// import { PlaywrightCrawler } from 'crawlee';



// const sources = [
//     { 
//         url: 'https://github.com/facebook/react/releases',
//         techName: 'React',
//         type: 'github_releases',
//         selectors: {
//             releaseContainer: '.Box-row',
//             version: 'h1 a, .f1 a',
//             date: 'relative-time',
//             content: '.markdown-body'
//         }
//     },
//     { 
//         url: 'https://www.oracle.com/java/technologies/javase/25all-relnotes.html',
//         techName: 'Java',
//         type: 'oracle_notes',
//         selectors: {
//             content: '.u30-contentArea, .cc01w1',
//             version: 'h2, h3',
//             sections: '.u30-contentArea p, .u30-contentArea ul'
//         }
//     },
//     { 
//         url: 'https://www.python.org/downloads/',
//         techName: 'Python',
//         type: 'python_releases',
//         selectors: {
//             releaseContainer: '.release-number',
//             version: 'a',
//             content: '.release-content'
//         }
//     },
//     { 
//         url: 'https://nodejs.org/en/blog/release/',
//         techName: 'Node.js',
//         type: 'nodejs_blog',
//         selectors: {
//             releaseContainer: 'article, .blog-post',
//             version: 'h1, .blog-post-title',
//             date: 'time, .blog-post-date',
//             content: '.blog-post-content, article .content'
//         }
//     },
//     { 
//         url: 'https://rubyonrails.org/releases/',
//         techName: 'Ruby on Rails',
//         type: 'rails_releases',
//         selectors: {
//             releaseContainer: '.post, article',
//             version: 'h2, .post-title',
//             date: '.post-date, time',
//             content: '.post-content, .entry-content'
//         }
//     }
// ];


// // PlaywrightCrawler crawls the web using a headless
// // browser controlled by the Playwright library.
// const crawler = new PlaywrightCrawler({
//     // Use the requestHandler to process each of the crawled pages.
//     async requestHandler({ request, page, enqueueLinks, log, pushData }) {
//         const title = await page.title();
//         log.info(`Title of ${request.loadedUrl} is '${title}'`);

//         // Save results as JSON to ./storage/datasets/default
//         await pushData({ title, url: request.loadedUrl });

//         // Extract links from the current page
//         // and add them to the crawling queue.
//         await enqueueLinks();
//     },
//     // Comment this option to scrape the full website.
//     maxRequestsPerCrawl: 50,
//     // Uncomment this option to see the browser window.
//     // headless: false,
// });

// // Add first URL to the queue and start the crawl.
// await crawler.run(sources);
