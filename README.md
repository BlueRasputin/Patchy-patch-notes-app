# Welcome to Patchy!
### Your First Mate for Navigating the Seas of Changelogs.


One of the biggest problems faced by developers today is attempting to keep track of all the different updates to software development languages, frameworks and libraries. With each new language learned, it becomes  why I built Patchy.

Patchy is a tool for finding the latest updates on development tools and software you use. It aggregates information from various sources, and utilizes AI generated responses to reduce those patchnotes into digestible summaries to keep you informed on the latest updates to your favorite languages, libraries, and frameworks.

Patchy is a means to streamline your development workflow and enhance your productivity by spending less time hunting down the latest updates on the frameworks and libraries you use every day. To begin, select your most utilized patch notes on the Homepage.



#### Tech stack:

- React (JS/HTML)
- CSS
- React Toastify for browser notifications
- Java
- Maven
- Node.js
- SpringBoot
- MySQL


#### Installation and run steps:

1. Clone repository to local machine
2. Obtain Gemini API key from google and enter into app.env file on backend
3. open Patchy/Server file in IntelliJ (or similar IDE)
4. Open MySQL Workbench and create schema titles 'patchy'
5. start Patchy.js to instantiate database and start server
6. run the 'tech\_table\_sample\_data.sql' file in sql to instantiate tech table data (and add other queries for whatever tech you want to see on the site)
7. open Patchy/ui in VSCode
8. navigate into the ui folder and enter 'npm run dev' to open on localhost
9. create account and begin adding techs to your bay





[Link to Wireframe](https://docs.google.com/presentation/d/1R4BeHVkl3Rgo0GHLR5LxLE0B74aQVonB5e7gJyndMOw/edit?usp=drive_link)







[Link to ERD](https://www.figma.com/board/d9h6N4kq2ZuTrusCRf8lQC/Unit-2-Final-ERD?node-id=0-1&t=Fd4bsHiCHGkbfZxt-1)







#### Future development plans:

- Integrate a patch notes table to update daily rather than upon opening the bay
- Add an admin profile to add techs to tech list
- format patch notes with different fields such as version, and latest release, and type (framework, library, language)
- integrate a page for users without accounts to quickly view all patch notes with a search function





