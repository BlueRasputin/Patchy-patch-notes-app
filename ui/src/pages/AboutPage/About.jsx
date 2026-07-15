import './About.css';
function About() {

    return(
        <div className="aboutpage">
            <h1>About Patchy</h1>
        
        <div className="about-body">
            <h2>What is Patchy?</h2>
            <h3>Patchy keeps you up-to-date with the tools that matter to you.
            <br />
            Stay informed about the latest updates to your development tools without the hassle. Patchy pulls information directly from official sources and uses Claude AI to transform lengthy patch notes into clear, concise summaries—so you can quickly understand what's new in your favorite languages, libraries, and frameworks </h3>
            <br />
            <h2>Why use Patchy?</h2>
            <h3>In the fast-paced world of software development, staying current with updates is crucial. However, sifting through extensive patch notes can be time-consuming and overwhelming. Patchy simplifies this process by curating and summarizing updates from the technologies you use most, allowing you to focus on what matters: building great software. </h3>
            <br />
            <h2>Spend less time searching, more time building. Select the tools you use most, and let Patchy deliver the updates that matter to your workflow. </h2>
        </div>
        </div>
    )
}

export default About;