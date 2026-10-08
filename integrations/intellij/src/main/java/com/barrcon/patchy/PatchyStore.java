package com.barrcon.patchy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.function.Consumer;

/** ~/.patchy files shared with the VS Code extension and the MCP server. */
final class PatchyStore {
    static final String DATA = "data.json";
    static final String CREDENTIALS = "credentials.json";

    private static final Path DIR = Path.of(System.getProperty("user.home"), ".patchy");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private PatchyStore() {
    }

    static synchronized JsonObject read(String name) throws IOException {
        Path file = DIR.resolve(name);
        if (!Files.exists(file)) {
            return new JsonObject();
        }
        try {
            JsonElement json = JsonParser.parseString(Files.readString(file));
            if (json.isJsonObject()) {
                return json.getAsJsonObject();
            }
        } catch (JsonParseException ignored) {
        }
        // Refuse to overwrite a file we can't parse, it may belong to another client.
        throw new IOException(file + " is not a JSON object");
    }

    static synchronized void update(String name, Consumer<JsonObject> change) throws IOException {
        JsonObject json = read(name);
        change.accept(json);
        Files.createDirectories(DIR);
        restrict(DIR, "rwx------");
        Path tmp = Files.createTempFile(DIR, name, ".tmp");
        restrict(tmp, "rw-------");
        Files.writeString(tmp, GSON.toJson(json));
        Files.move(tmp, DIR.resolve(name), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    static JsonObject child(JsonObject parent, String key) {
        JsonElement value = parent.get(key);
        if (value != null && value.isJsonObject()) {
            return value.getAsJsonObject();
        }
        JsonObject created = new JsonObject();
        parent.add(key, created);
        return created;
    }

    static String token() {
        String env = System.getenv("PATCHY_TOKEN");
        if (env != null && !env.isBlank()) {
            return env.strip();
        }
        try {
            JsonElement token = read(CREDENTIALS).get("token");
            return token != null && token.isJsonPrimitive() && !token.getAsString().isBlank() ? token.getAsString() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void restrict(Path path, String perms) throws IOException {
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(perms));
        } catch (UnsupportedOperationException ignored) {
            // Windows: no POSIX permissions, the user profile directory is already private.
        }
    }
}
