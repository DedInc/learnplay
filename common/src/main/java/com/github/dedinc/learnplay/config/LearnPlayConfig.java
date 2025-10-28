package com.github.dedinc.learnplay.config;

import com.github.dedinc.learnplay.LearnPlay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main configuration class for LearnPlay mod.
 * Handles all mod settings and persists them to JSON.
 * <p>
 * Configuration is saved to: config/learnplay/config.json
 */
public class LearnPlayConfig {
    private static final String CONFIG_PATH = "config/learnplay/config.json";
    private static LearnPlayConfig instance;

    // Trigger Settings
    public TriggerConfig triggers = new TriggerConfig();

    // Review Settings
    public int maxCardsPerSession = 20;
    public int maxNewCardsPerDay = 10;
    public int maxReviewsPerDay = 100;

    // UI Settings
    public boolean pauseGameDuringReview = true;
    public boolean showHudStats = false;
    public int keybindCode = 73; // 'I' key (GLFW_KEY_I)

    private LearnPlayConfig() {
        // Private constructor for singleton
    }

    public static LearnPlayConfig getInstance() {
        if (instance == null) {
            instance = new LearnPlayConfig();
            instance.load();
        }
        return instance;
    }

    /**
     * Load configuration from JSON file.
     * Creates default config if file doesn't exist.
     */
    public void load() {
        Path configPath = Paths.get(CONFIG_PATH);

        try {
            // Create config directory if it doesn't exist
            Path configDir = configPath.getParent();
            if (configDir != null && !Files.exists(configDir)) {
                Files.createDirectories(configDir);
                LearnPlay.LOGGER.info("Created config directory: {}", configDir.toAbsolutePath());
            }

            // Load config if file exists
            if (Files.exists(configPath)) {
                String jsonContent = Files.readString(configPath, StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
                fromJson(json);
                LearnPlay.LOGGER.info("Loaded configuration from {}", configPath.toAbsolutePath());
            } else {
                // Create default config
                save();
                LearnPlay.LOGGER.info("Created default configuration at {}", configPath.toAbsolutePath());
            }
        } catch (Exception e) {
            LearnPlay.LOGGER.error("Failed to load configuration, using defaults", e);
        }
    }

    /**
     * Save configuration to JSON file.
     */
    public void save() {
        Path configPath = Paths.get(CONFIG_PATH);

        try {
            JsonObject json = toJson();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = gson.toJson(json);

            Files.writeString(configPath, jsonContent, StandardCharsets.UTF_8);
            LearnPlay.LOGGER.info("Saved configuration to {}", configPath.toAbsolutePath());
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to save configuration", e);
        }
    }

    /**
     * Convert config to JSON.
     */
    private JsonObject toJson() {
        JsonObject json = new JsonObject();

        // Trigger settings
        json.add("triggers", triggers.toJson());

        // Review settings
        JsonObject reviewSettings = new JsonObject();
        reviewSettings.addProperty("maxCardsPerSession", maxCardsPerSession);
        reviewSettings.addProperty("maxNewCardsPerDay", maxNewCardsPerDay);
        reviewSettings.addProperty("maxReviewsPerDay", maxReviewsPerDay);
        json.add("reviewSettings", reviewSettings);

        // UI settings
        JsonObject uiSettings = new JsonObject();
        uiSettings.addProperty("pauseGameDuringReview", pauseGameDuringReview);
        uiSettings.addProperty("showHudStats", showHudStats);
        uiSettings.addProperty("keybindCode", keybindCode);
        json.add("uiSettings", uiSettings);

        return json;
    }

    /**
     * Load config from JSON.
     */
    private void fromJson(JsonObject json) {
        // Trigger settings
        if (json.has("triggers")) {
            triggers.fromJson(json.getAsJsonObject("triggers"));
        }

        // Review settings
        if (json.has("reviewSettings")) {
            JsonObject reviewSettings = json.getAsJsonObject("reviewSettings");
            maxCardsPerSession = reviewSettings.has("maxCardsPerSession") ?
                    reviewSettings.get("maxCardsPerSession").getAsInt() : maxCardsPerSession;
            maxNewCardsPerDay = reviewSettings.has("maxNewCardsPerDay") ?
                    reviewSettings.get("maxNewCardsPerDay").getAsInt() : maxNewCardsPerDay;
            maxReviewsPerDay = reviewSettings.has("maxReviewsPerDay") ?
                    reviewSettings.get("maxReviewsPerDay").getAsInt() : maxReviewsPerDay;
        }

        // UI settings
        if (json.has("uiSettings")) {
            JsonObject uiSettings = json.getAsJsonObject("uiSettings");
            pauseGameDuringReview = uiSettings.has("pauseGameDuringReview") ?
                    uiSettings.get("pauseGameDuringReview").getAsBoolean() : pauseGameDuringReview;
            showHudStats = uiSettings.has("showHudStats") ?
                    uiSettings.get("showHudStats").getAsBoolean() : showHudStats;
            keybindCode = uiSettings.has("keybindCode") ?
                    uiSettings.get("keybindCode").getAsInt() : keybindCode;
        }
    }

    /**
     * Reload configuration from disk.
     */
    public void reload() {
        load();
        LearnPlay.LOGGER.info("Configuration reloaded");
    }
}
