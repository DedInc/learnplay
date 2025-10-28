package com.github.dedinc.learnplay.storage.common;

import com.github.dedinc.learnplay.LearnPlay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generic utility for handling JSON file operations.
 * Provides common file I/O operations for saving, loading, and deleting JSON files.
 */
public class JsonFileHandler {

    private final String configPath;
    private final Gson gson;

    public JsonFileHandler(String configPath) {
        this.configPath = configPath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Ensure the config directory exists.
     */
    public void ensureDirectoryExists() throws IOException {
        Path configDir = Paths.get(configPath);
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
            LearnPlay.LOGGER.info("Created directory: {}", configDir.toAbsolutePath());
        }
    }

    /**
     * Save a JSON object to a file.
     */
    public boolean saveJson(String id, JsonObject json) {
        Path configDir = Paths.get(configPath);
        Path file = configDir.resolve(id + ".json");

        try {
            ensureDirectoryExists();
            String jsonContent = gson.toJson(json);
            Files.writeString(file, jsonContent, StandardCharsets.UTF_8);
            LearnPlay.LOGGER.info("Saved JSON to {}", file.toAbsolutePath());
            return true;
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to save JSON: {}", id, e);
            return false;
        }
    }

    /**
     * Delete a JSON file.
     */
    public boolean deleteFile(String id) {
        Path configDir = Paths.get(configPath);
        Path file = configDir.resolve(id + ".json");

        try {
            if (Files.exists(file)) {
                Files.delete(file);
                LearnPlay.LOGGER.info("Deleted file: {}", file.toAbsolutePath());
                return true;
            }
            return false;
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to delete file: {}", id, e);
            return false;
        }
    }

    /**
     * Check if a file exists.
     */
    public boolean fileExists(String id) {
        Path configDir = Paths.get(configPath);
        Path file = configDir.resolve(id + ".json");
        return Files.exists(file);
    }

    /**
     * Get the config directory path.
     */
    public Path getConfigDirectory() {
        return Paths.get(configPath);
    }
}

