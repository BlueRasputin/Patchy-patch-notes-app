import { Link } from 'react-router-dom';
import './About.css';

const REPO = 'https://github.com/BlueRasputin/Patchy-patch-notes-app';
const INSTALL = 'curl -fsSL https://raw.githubusercontent.com/BlueRasputin/Patchy-patch-notes-app/main/install.sh | sh';

function CopyableCommand({ command, label }) {
    return (
        <div className="command">
            <code aria-label={label}>{command}</code>
            <button type="button" onClick={() => navigator.clipboard.writeText(command)} aria-label={`Copy: ${label}`}>
                Copy
            </button>
        </div>
    );
}

function About() {
    return (
        <div className="aboutpage">
            <h1>Get Patchy</h1>
            <p className="about-lede">
                Patchy reads the manifests in your project (package.json, pom.xml, requirements.txt, go.mod and more)
                and keeps you current on every language, framework and library in them. When something you depend on
                ships a release, Patchy opens a tab in your editor with the release notes, and warns you first about
                breaking changes or known vulnerabilities in the versions you use.
            </p>
            <p className="about-lede">
                No account and no API key needed. Your project list stays on your machine in <code>~/.patchy</code>,
                and summaries come from the assistant you already use: Copilot, Claude, Codex or Gemini.
            </p>

            <section className="about-section" aria-labelledby="install-heading">
                <h2 id="install-heading">Install</h2>
                <p>macOS and Linux. Installs the VS Code extension (also Cursor, Windsurf and VSCodium if present):</p>
                <CopyableCommand command={INSTALL} label="Install command" />
                <p>
                    Add <code>-s -- --mcp</code> after <code>sh</code> to also install the AI assistant server.
                    On Windows, or to install by hand, download the files from the{' '}
                    <a href={`${REPO}/releases/latest`}>latest release</a>.
                </p>
            </section>

            <section className="about-section" aria-labelledby="editors-heading">
                <h2 id="editors-heading">Where Patchy shows up</h2>
                <dl className="about-list">
                    <dt>VS Code, Cursor, Windsurf</dt>
                    <dd>A status bar count, a notification plus an updates tab for new releases, and <code>@patchy</code> in Copilot Chat.</dd>
                    <dt>IntelliJ IDEA and other JetBrains IDEs</dt>
                    <dd>The same alerts and a Markdown updates tab. Install <code>patchy-intellij.zip</code> from the release with Settings → Plugins → Install Plugin from Disk.</dd>
                    <dt>Claude, Codex, Gemini, Copilot agent mode</dt>
                    <dd>Tools to list your project&apos;s releases and check for breaking changes before an upgrade. The VS Code extension connects them for you after sign-in, or run <code>claude mcp add patchy -- patchy-mcp</code>.</dd>
                    <dt>This website</dt>
                    <dd>Browse the <Link to="/Techs">tech catalog</Link>, follow techs in <Link to="/TheBay">The Bay</Link>, or paste a manifest into <Link to="/PackageInsights">Project Insights</Link>.</dd>
                </dl>
            </section>

            <section className="about-section" aria-labelledby="accounts-heading">
                <h2 id="accounts-heading">Do I need an account?</h2>
                <p>
                    No. Everything works without one. An account only syncs the techs you follow between this website
                    and your editors. <Link to="/register">Create one</Link> if you want that.
                </p>
            </section>
        </div>
    );
}

export default About;
