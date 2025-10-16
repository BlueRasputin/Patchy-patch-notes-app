// import { PlaywrightCrawler, Dataset } from 'crawlee';

// const sources = [
//     { 
//         url: 'https://react.dev/blog/2025/10/01/react-19-2',
//         techName: 'React'
//     },
//     { 
//         url: 'https://www.oracle.com/java/technologies/javase/25all-relnotes.html',
//         techName: 'Java'
//     },
//     { 
//         url: 'https://www.python.org/downloads/',
//         techName: 'Python'
//     },
//     { 
//         url: 'https://nodejs.org/en/blog/release/',
//         techName: 'Node.js'
//     },
//     { 
//         url: 'https://rubyonrails.org/2025/9/22/Rails-Version-8-0-3-has-been-released',
//         techName: 'Ruby on Rails'
//     }
// ];

// const crawler = new PlaywrightCrawler({
//     async requestHandler({ request, page, log, pushData }) {
//         try {
//             log.info(`Scraping: ${request.loadedUrl}`);

//             // Find tech associated with this URL
//             const source = sources.find(s => request.loadedUrl.includes(new URL(s.url).hostname));
//             if (!source) {
//                 log.warning(`Unknown source: ${request.loadedUrl}`);
//                 return;
//             }

            
//             const content = await page.evaluate(() => {
                
//                 const scripts = document.querySelectorAll('script, style, nav, header, footer');
//                 scripts.forEach(el => el.remove());
                
            
//                 const mainContent = document.querySelector('.release-note');
//                 const content = mainContent ? mainContent.textContent : document.body.textContent;
                
                
//                 return content.replace(/\s+/g, ' ').trim();
//             });

//             if (content && content.length > 100) {
//                 await pushData({
//                     techName: source.techName,
//                     url: request.loadedUrl,
//                     content: content,
//                     scrapedAt: new Date().toISOString()
//                 });
                
//                 log.info(`Saved content for ${source.techName} (${content.length} chars)`);
//             } else {
//                 log.warning(`Insufficient content found for ${source.techName} (${content ? content.length : 0} chars)`);
//             }

//         } catch (error) {
//             log.error(`Error processing ${request.loadedUrl}:`, error.message);
//         }
//     },
//     maxRequestsPerCrawl: 10,
//     headless: true,
// });

// // Run the crawler
// console.log('Argh, starting to sail...');
// await crawler.run(sources.map(s => s.url));

// // Get results and send to backend
// const dataset = await Dataset.open();
// const results = await dataset.getData();

// console.log(`Plundered ${results.items.length} patch note pages`);

// // Send to backend
// if (results.items.length > 0) {
//     try {
//         console.log('Sending data to backend...');
        
//         const response = await fetch('http://localhost:8080/api/process-crawled-notes', {
//             method: 'POST',
//             headers: { 'Content-Type': 'application/json' },
//             body: JSON.stringify(results.items)
//         });
        
//         if (response.ok) {
//             const responseText = await response.text();
//             console.log('Backend response:', responseText);
//         } else {
//             console.error('Backend error:', response.status, await response.text());
//         }
//     } catch (error) {
//         console.error('Network error:', error.message);
//         console.log('Data saved locally in ./storage/datasets/default/');
//     }
// } else {
//     console.log('No data to send to backend');
// }






import { PlaywrightCrawler, Dataset } from 'crawlee';

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
        url: 'https://github.com/rails/rails/releases/tag/v8.0.3',
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

            const content = await page.evaluate((techName) => {
                const scripts = document.querySelectorAll('script, style, nav, header, footer');
                scripts.forEach(el => el.remove());
                
                let mainContent;
                
                if (techName === 'React') {
                    mainContent = document.querySelector('.min-w-0 isolate');
                } else if (techName === 'Java') {
                    mainContent = document.querySelector('.cc01w1 cwidth');
                } else if (techName === 'Python') {
                    mainContent = document.querySelector('.main-content');
                } else if (techName === 'Node.js') {
                    mainContent = document.querySelector('.layouts-module_mzYk8q_postLayout');
                } else if (techName === 'Ruby on Rails') {
                    mainContent = document.querySelector('.Box-body');
                }else if (techName === 'Spring') {
                    mainContent = document.querySelector('.column is-9 pr-6');
                } else {
                    mainContent = document.querySelector('main, article, .content');
                }

                const content = mainContent ? mainContent.textContent : document.body.textContent;
                return content.replace(/\s+/g, ' ').trim();
            }, source.techName);

            if (content && content.length > 100) {
                await pushData({
                    techName: source.techName,
                    url: request.loadedUrl,
                    content: content,
                    scrapedAt: new Date().toISOString()
                });
                
                log.info(`Saved content for ${source.techName} (${content.length} chars)`);
            } else {
                log.warning(`Insufficient content found for ${source.techName} (${content ? content.length : 0} chars)`);
            }

        } catch (error) {
            log.error(`Error processing ${request.loadedUrl}:`, error.message);
        }
    },
    maxRequestsPerCrawl: 10,
    headless: true,
});

console.log('Argh, starting to sail...');
await crawler.run(sources.map(s => s.url));

const dataset = await Dataset.open();
const results = await dataset.getData();

console.log(`Plundered ${results.items.length} patch note pages`);

if (results.items.length > 0) {
    try {
        console.log('Sending data to backend...');
        
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