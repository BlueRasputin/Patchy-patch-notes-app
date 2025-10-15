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
        url: 'https://www.python.org/downloads/',
        techName: 'Python'
    },
    { 
        url: 'https://nodejs.org/en/blog/release/',
        techName: 'Node.js'
    },
    { 
        url: 'https://rubyonrails.org/2025/9/22/Rails-Version-8-0-3-has-been-released',
        techName: 'Ruby on Rails'
    }
];

const crawler = new PlaywrightCrawler({
    async requestHandler({ request, page, log, pushData }) {
        try {
            log.info(`Scraping: ${request.loadedUrl}`);

            // Find tech associated with this URL
            const source = sources.find(s => request.loadedUrl.includes(new URL(s.url).hostname));
            if (!source) {
                log.warning(`Unknown source: ${request.loadedUrl}`);
                return;
            }

            
            const content = await page.evaluate(() => {
                
                const scripts = document.querySelectorAll('script, style, nav, header, footer');
                scripts.forEach(el => el.remove());
                
            
                const mainContent = document.querySelector('.release-note');
                const content = mainContent ? mainContent.textContent : document.body.textContent;
                
                
                return content.replace(/\s+/g, ' ').trim();
            });

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

// Run the crawler
console.log('Argh, starting to sail...');
await crawler.run(sources.map(s => s.url));

// Get results and send to backend
const dataset = await Dataset.open();
const results = await dataset.getData();

console.log(`Plundered ${results.items.length} patch note pages`);

// Send to backend
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