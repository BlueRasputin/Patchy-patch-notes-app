package com.barrcon.patchy.services;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ManifestParserTest {

    @Test
    void parsesEachEcosystem() {
        var npm = ManifestParser.parse("web/package.json",
                "{\"dependencies\":{\"react\":\"^19\"},\"devDependencies\":{\"vite\":\"^7\"}}");
        assertEquals("npm", npm.ecosystem());
        assertEquals(Map.of("react", "^19", "vite", "^7"), npm.dependencies());

        var pip = ManifestParser.parse("requirements.txt", "# web\nDjango>=5.0\nrequests[socks]==2.32 ; python_version>'3'\n-r dev.txt\n");
        assertEquals(List.of("Django", "requests"), List.copyOf(pip.dependencies().keySet()));

        var pyproject = ManifestParser.parse("pyproject.toml", """
                [project]
                dependencies = [
                  "fastapi>=0.110",
                  "httpx",
                ]
                [tool.poetry.dependencies]
                python = "^3.12"
                pydantic = "^2"
                """);
        assertEquals(List.of("fastapi", "httpx", "pydantic"), List.copyOf(pyproject.dependencies().keySet()));

        var cargo = ManifestParser.parse("Cargo.toml", "[package]\nname = \"app\"\n[dependencies]\nserde = { version = \"1\" }\ntokio = \"1.40\"\n");
        assertEquals(List.of("serde", "tokio"), List.copyOf(cargo.dependencies().keySet()));

        var go = ManifestParser.parse("go.mod", "module github.com/me/app\n\ngo 1.22\n\nrequire (\n\tgithub.com/gin-gonic/gin v1.10.0\n)\nrequire golang.org/x/net v0.30.0\n");
        assertEquals(Map.of("github.com/gin-gonic/gin", "v1.10.0", "golang.org/x/net", "v0.30.0"), go.dependencies());

        var pom = ManifestParser.parse("pom.xml", "<project><artifactId>me</artifactId><dependencies><dependency><groupId>org.json</groupId><artifactId>json</artifactId><version>2023</version></dependency></dependencies></project>");
        assertEquals(Map.of("json", "2023"), pom.dependencies());

        var gradle = ManifestParser.parse("build.gradle.kts", "plugins { kotlin(\"jvm\") }\ndependencies { implementation(\"io.ktor:ktor-server-core:2.3.0\") }");
        assertEquals(List.of("java", "kotlin"), gradle.languages());
        assertEquals(Map.of("ktor-server-core", "2.3.0"), gradle.dependencies());

        var gems = ManifestParser.parse("Gemfile", "source 'https://rubygems.org'\ngem 'rails', '~> 7.1'\ngem \"puma\"\n");
        assertEquals(Map.of("rails", "~> 7.1", "puma", ""), gems.dependencies());

        var composer = ManifestParser.parse("composer.json", "{\"require\":{\"php\":\"^8.2\",\"laravel/framework\":\"^11\"}}");
        assertEquals(Map.of("laravel/framework", "^11"), composer.dependencies());

        var mix = ManifestParser.parse("mix.exs", "defp deps do\n  [\n    {:phoenix, \"~> 1.7.14\"},\n    {:jason, \"~> 1.4\"},\n    {:heroicons, github: \"tailwindlabs/heroicons\"}\n  ]\nend");
        assertEquals(List.of("elixir"), mix.languages());
        assertEquals(Map.of("phoenix", "~> 1.7.14", "jason", "~> 1.4", "heroicons", ""), mix.dependencies());

        var pubspec = ManifestParser.parse("pubspec.yaml", """
                name: app
                environment:
                  sdk: ">=3.4.0 <4.0.0"
                dependencies:
                  flutter:
                    sdk: flutter
                  http: ^1.2.0
                  provider: ">=6.0.0 <7.0.0"
                dev_dependencies:
                  flutter_lints: ^4.0.0
                flutter:
                  uses-material-design: true
                """);
        assertEquals("pub", pubspec.ecosystem());
        assertEquals(List.of("flutter", "http", "provider", "flutter_lints"), List.copyOf(pubspec.dependencies().keySet()));
        assertEquals("^1.2.0", pubspec.dependencies().get("http"));

        var sbt = ManifestParser.parse("build.sbt", "scalaVersion := \"3.5.0\"\nlibraryDependencies ++= Seq(\n  \"org.typelevel\" %% \"cats-core\" % \"2.12.0\",\n  \"com.lihaoyi\" %% \"upickle\" % \"4.0.2\"\n)");
        assertEquals(List.of("scala"), sbt.languages());
        assertEquals(Map.of("cats-core", "2.12.0", "upickle", "4.0.2"), sbt.dependencies());

        var julia = ManifestParser.parse("Project.toml", "name = \"App\"\n[deps]\nHTTP = \"cd3eb016-35fb-5094-929b-558a96fad6f3\"\nJSON3 = \"0f8b85d8-7281-11e9-16c2-39a750bddbf1\"\n[compat]\njulia = \"1.10\"\n");
        assertEquals(Map.of("HTTP", "", "JSON3", ""), julia.dependencies());

        var gleam = ManifestParser.parse("gleam.toml", "name = \"app\"\n[dependencies]\ngleam_stdlib = \">= 0.44.0 and < 2.0.0\"\nwisp = \">= 1.0.0 and < 2.0.0\"\n");
        assertEquals("hex", gleam.ecosystem());
        assertEquals(List.of("gleam_stdlib", "wisp"), List.copyOf(gleam.dependencies().keySet()));

        var csproj = ManifestParser.parse("src/Api/Api.csproj", "<Project><ItemGroup><PackageReference Include=\"Serilog\" Version=\"4.0.1\" /><PackageReference Include=\"Dapper\"><Version>2.1</Version></PackageReference></ItemGroup></Project>");
        assertEquals("nuget", csproj.ecosystem());
        assertEquals("4.0.1", csproj.dependencies().get("Serilog"));
        assertTrue(csproj.dependencies().containsKey("Dapper"));

        assertNull(ManifestParser.parse("README.md", "# hi"));
    }

    @Test
    void readsTheNewestReleaseFromGithubsAtomFeed() {
        var feed = org.jsoup.Jsoup.parse("""
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry><title>v2.0.0</title><link rel="alternate" href="https://github.com/o/r/releases/tag/v2.0.0"/>
                    <content type="html">&lt;h2&gt;Breaking&lt;/h2&gt;&lt;ul&gt;&lt;li&gt;Dropped Node 18&lt;/li&gt;&lt;/ul&gt;</content></entry>
                  <entry><title>v1.9.0</title><link rel="alternate" href="https://github.com/o/r/releases/tag/v1.9.0"/></entry>
                </feed>""", "", org.jsoup.parser.Parser.xmlParser());
        var release = TechDiscoveryService.parseReleaseFeed(feed);
        assertEquals("v2.0.0", release.getString("tag_name"));
        assertTrue(release.getString("body").contains("Dropped Node 18"));
        assertFalse(release.getString("body").contains("<li>"));
    }

    @Test
    void rejectsMonorepoReleasesForOtherPackages() {
        assertTrue(TechDiscoveryService.releaseBelongsTo("v1.19.0", "gleam"));
        assertTrue(TechDiscoveryService.releaseBelongsTo("3.9.0", "scala"));
        assertTrue(TechDiscoveryService.releaseBelongsTo("go_router-v14.2.0", "go_router"));
        assertTrue(TechDiscoveryService.releaseBelongsTo("@babel/core@7.26.0", "@babel/core"));
        assertFalse(TechDiscoveryService.releaseBelongsTo("http2-v3.1.0", "http"));
    }

    @Test
    void extractsGithubRepoFromRegistryUrls() {
        assertEquals("axios/axios", TechDiscoveryService.extractGithubRepo("git+https://github.com/axios/axios.git"));
        assertEquals("psf/requests", TechDiscoveryService.extractGithubRepo("{Source=https://github.com/psf/requests} "));
        assertEquals("gin-gonic/gin", TechDiscoveryService.extractGithubRepo("github.com/gin-gonic/gin"));
        assertNull(TechDiscoveryService.extractGithubRepo("https://gitlab.com/a/b"));
        assertNull(TechDiscoveryService.extractGithubRepo(null));
    }

    @Test
    void readsLowestVersionFromRangesAndCapsLongNotes() {
        assertEquals("19.0.0", AdvisoryService.baseVersion("^19.0.0"));
        assertEquals("2.32", AdvisoryService.baseVersion("[socks]==2.32 ; python_version>'3'"));
        assertEquals("1.10.0", AdvisoryService.baseVersion("v1.10.0"));
        assertNull(AdvisoryService.baseVersion("latest"));

        String longNotes = "## Changes\n" + "- fixed a thing\n".repeat(1000);
        String excerpt = PatchNoteService.excerpt(longNotes);
        assertTrue(excerpt.length() < PatchNoteService.MAX_NOTE_CHARS + 100);
        assertTrue(excerpt.endsWith("via the source link.*"));
        assertEquals("short", PatchNoteService.excerpt("  short  "));
    }
}
