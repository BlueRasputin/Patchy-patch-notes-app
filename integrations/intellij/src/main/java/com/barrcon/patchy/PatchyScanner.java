package com.barrcon.patchy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.concurrency.AppExecutorUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class PatchyScanner implements Disposable {
    private static final Logger LOG = Logger.getInstance(PatchyScanner.class);
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final Set<String> MANIFESTS = Set.of("package.json", "requirements.txt", "pyproject.toml", "Pipfile",
            "Cargo.toml", "go.mod", "Gemfile", "composer.json", "pom.xml", "build.gradle", "build.gradle.kts", "Dockerfile",
            "mix.exs", "gleam.toml", "pubspec.yaml", "build.sbt", "Project.toml", "Directory.Packages.props");

    private static boolean isManifest(String name) {
        return MANIFESTS.contains(name) || name.endsWith(".csproj") || name.endsWith(".fsproj");
    }
    private static final Set<String> SKIP_DIRS = Set.of("node_modules", ".git", "target", "build", "dist", "out",
            ".venv", "venv", "vendor", ".gradle", ".idea");
    private static final int MAX_DEPTH = 4;
    private static final int MAX_FILES = 25;
    private static final long MAX_BYTES = 512 * 1024;

    private final Project project;
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile ScheduledFuture<?> next;
    private volatile boolean disposed;

    public PatchyScanner(Project project) {
        this.project = project;
    }

    static PatchyScanner get(Project project) {
        return project.getService(PatchyScanner.class);
    }

    public static final class Startup implements ProjectActivity {
        @Override
        public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
            get(project).scan(false);
            return Unit.INSTANCE;
        }
    }

    void scan(boolean manual) {
        if (disposed || !running.compareAndSet(false, true)) {
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                doScan(manual);
            } catch (Exception e) {
                LOG.warn("Patchy scan failed", e);
            } finally {
                running.set(false);
                scheduleNext();
            }
        });
    }

    @Override
    public void dispose() {
        disposed = true;
        ScheduledFuture<?> pending = next;
        if (pending != null) {
            pending.cancel(false);
        }
    }

    private void scheduleNext() {
        ScheduledFuture<?> pending = next;
        if (pending != null) {
            pending.cancel(false);
        }
        if (!disposed) {
            next = AppExecutorUtil.getAppScheduledExecutorService()
                    .schedule(() -> scan(false), Math.max(1, PatchySettings.get().pollMinutes), TimeUnit.MINUTES);
        }
    }

    private void doScan(boolean manual) throws IOException {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }
        Path root = Path.of(basePath).toAbsolutePath();
        String projectKey = root.toString();
        Map<String, String> files = collectManifests(root);
        if (files.isEmpty()) {
            if (manual) {
                notify("Patchy", "No dependency manifests found in this project.", NotificationType.INFORMATION, List.of(), false);
            }
            return;
        }

        String apiUrl = PatchySettings.get().apiUrl;
        String token = PatchyStore.token();
        JsonObject request = new JsonObject();
        request.add("files", GSON.toJsonTree(files));
        JsonObject response;
        try {
            response = JsonParser.parseString(post(apiUrl + "/api/insights/project", request, token)).getAsJsonObject();
        } catch (Exception e) {
            LOG.info("Patchy API unreachable, using local notes: " + e.getMessage());
            if (manual) {
                List<JsonObject> cached = cachedNotes(projectKey);
                notify("Patchy API unreachable", cached.isEmpty()
                                ? "No saved patch notes for this project yet."
                                : "Showing saved patch notes for " + cached.size() + " techs.",
                        NotificationType.WARNING, cached, false);
            }
            return;
        }

        Map<String, JsonObject> notes = new LinkedHashMap<>();
        for (JsonElement match : array(response, "matches")) {
            JsonElement note = match.isJsonObject() ? match.getAsJsonObject().get("patchNote") : null;
            if (note != null && note.isJsonObject() && str(note.getAsJsonObject(), "techName") != null
                    && note.getAsJsonObject().get("id") != null) {
                notes.putIfAbsent(str(note.getAsJsonObject(), "techName"), note.getAsJsonObject());
            }
        }
        JsonArray techNames = array(response, "techNames");

        List<JsonObject> fresh = new ArrayList<>();
        boolean[] firstScan = {false};
        PatchyStore.update(PatchyStore.DATA, data -> {
            JsonObject stored = PatchyStore.child(data, "notes");
            JsonObject seen = PatchyStore.child(data, "seen");
            JsonObject projects = PatchyStore.child(data, "projects");
            firstScan[0] = !projects.has(projectKey);
            notes.forEach((tech, note) -> {
                stored.add(tech, note);
                if (!seen.has(tech)) {
                    seen.add(tech, note.get("id"));
                } else if (!seen.get(tech).equals(note.get("id"))) {
                    fresh.add(note);
                }
            });
            JsonObject entry = new JsonObject();
            entry.add("techNames", techNames);
            entry.addProperty("scannedAt", Instant.now().toString());
            projects.add(projectKey, entry);
        });

        List<JsonObject> all = List.copyOf(notes.values());
        if (firstScan[0] && !techNames.isEmpty()) {
            notify("Patchy", "Patchy is tracking " + techNames.size() + " techs in this project",
                    NotificationType.INFORMATION, all, false);
        }
        if (!fresh.isEmpty()) {
            List<String> breaking = fresh.stream().filter(PatchyScanner::isBreaking).map(n -> str(n, "techName")).toList();
            String summary = fresh.stream().map(PatchyScanner::label).collect(Collectors.joining(", "))
                    + (fresh.size() == 1 ? " has a new release" : " have new releases");
            notify(breaking.isEmpty() ? "Patchy" : "Breaking changes in " + String.join(", ", breaking), summary,
                    breaking.isEmpty() ? NotificationType.INFORMATION : NotificationType.WARNING, fresh, true);
        } else if (manual && !firstScan[0]) {
            notify("Patchy", "No new releases for the " + techNames.size() + " techs in this project.",
                    NotificationType.INFORMATION, all, false);
        }

        if (token != null && !techNames.isEmpty()) {
            JsonObject favorites = new JsonObject();
            favorites.add("techNames", techNames);
            try {
                post(apiUrl + "/api/me/favorites", favorites, token);
            } catch (Exception e) {
                LOG.info("Patchy favorites sync failed: " + e.getMessage());
            }
        }
    }

    private static Map<String, String> collectManifests(Path root) throws IOException {
        Map<String, String> files = new LinkedHashMap<>();
        Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), MAX_DEPTH, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return !dir.equals(root) && SKIP_DIRS.contains(dir.getFileName().toString())
                        ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && attrs.size() <= MAX_BYTES && isManifest(file.getFileName().toString())) {
                    try {
                        files.put(root.relativize(file).toString().replace('\\', '/'), Files.readString(file));
                    } catch (IOException e) {
                        LOG.info("Patchy skipped " + file + ": " + e.getMessage());
                    }
                }
                return files.size() >= MAX_FILES ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }

    private static String post(String url, JsonElement body, String token) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + response.statusCode() + " from " + url);
        }
        return response.body();
    }

    private static List<JsonObject> cachedNotes(String projectKey) {
        try {
            JsonObject data = PatchyStore.read(PatchyStore.DATA);
            JsonObject stored = PatchyStore.child(data, "notes");
            JsonObject entry = PatchyStore.child(PatchyStore.child(data, "projects"), projectKey);
            List<JsonObject> cached = new ArrayList<>();
            for (JsonElement tech : array(entry, "techNames")) {
                JsonElement note = stored.get(tech.getAsString());
                if (note != null && note.isJsonObject()) {
                    cached.add(note.getAsJsonObject());
                }
            }
            return cached;
        } catch (IOException e) {
            LOG.info("Patchy could not read local notes: " + e.getMessage());
            return List.of();
        }
    }

    private void notify(String title, String content, NotificationType type, List<JsonObject> show, boolean markSeen) {
        if (project.isDisposed()) {
            return;
        }
        Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("Patchy")
                .createNotification(title, StringUtil.escapeXmlEntities(content), type);
        if (!show.isEmpty()) {
            notification.addAction(NotificationAction.createSimpleExpiring("Show updates", () -> {
                openTab(show);
                if (markSeen) {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> markSeen(show));
                }
            }));
        }
        notification.notify(project);
    }

    private void openTab(List<JsonObject> notes) {
        LightVirtualFile file = new LightVirtualFile("Patchy updates.md", markdown(notes));
        file.setWritable(false);
        FileEditorManager.getInstance(project).openFile(file, true);
    }

    private static void markSeen(List<JsonObject> notes) {
        try {
            PatchyStore.update(PatchyStore.DATA, data -> {
                JsonObject seen = PatchyStore.child(data, "seen");
                notes.forEach(n -> seen.add(str(n, "techName"), n.get("id")));
            });
        } catch (IOException e) {
            LOG.warn("Patchy could not mark notes as seen", e);
        }
    }

    static String markdown(List<JsonObject> notes) {
        StringBuilder md = new StringBuilder("# Patchy updates\n");
        for (JsonObject note : notes) {
            md.append("\n## ").append(label(note)).append("\n\n");
            if (isBreaking(note)) {
                md.append("**⚠ Breaking / Security**\n\n");
            }
            String content = str(note, "content");
            if (content != null) {
                md.append(content.strip()).append("\n\n");
            }
            String url = str(note, "sourceUrl");
            if (url != null) {
                md.append("[Release notes](").append(url).append(")\n");
            }
        }
        return md.toString();
    }

    private static String label(JsonObject note) {
        String version = str(note, "releaseVersion");
        return str(note, "techName") + (version == null ? "" : " " + version);
    }

    private static boolean isBreaking(JsonObject note) {
        for (JsonElement category : array(note, "categories")) {
            String name = category.isJsonPrimitive() ? category.getAsString() : "";
            if (name.equals("Breaking Changes") || name.equals("Security")) {
                return true;
            }
        }
        return false;
    }

    private static JsonArray array(JsonObject json, String key) {
        JsonElement value = json.get(key);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();
    }

    private static String str(JsonObject json, String key) {
        JsonElement value = json.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }
}
