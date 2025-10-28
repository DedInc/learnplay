package com.github.dedinc.learnplay.storage.deck;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.storage.common.JsonFileHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles file I/O operations for deck JSON files.
 */
public class DeckFileHandler {

    private static final String CONFIG_PATH = "config/learnplay/decks/";
    private final JsonFileHandler jsonFileHandler;

    public DeckFileHandler() {
        this.jsonFileHandler = new JsonFileHandler(CONFIG_PATH);
    }

    /**
     * Load all user decks from the config directory.
     */
    public List<Deck> loadUserDecks() {
        List<Deck> decks = new ArrayList<>();

        try {
            jsonFileHandler.ensureDirectoryExists();
            Path configDir = jsonFileHandler.getConfigDirectory();

            if (Files.exists(configDir) && Files.isDirectory(configDir)) {
                List<Path> jsonFiles = Files.walk(configDir)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .collect(Collectors.toList());

                for (Path jsonFile : jsonFiles) {
                    try {
                        Deck deck = loadDeckFromFile(jsonFile);
                        if (deck != null) {
                            decks.add(deck);
                            LearnPlay.LOGGER.info("Loaded user deck: {} ({} cards)",
                                    deck.getName(), deck.getCardCount());
                        }
                    } catch (Exception e) {
                        LearnPlay.LOGGER.error("Failed to load user deck: {}", jsonFile, e);
                    }
                }
            }
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to access config directory", e);
        }

        return decks;
    }

    /**
     * Load a deck from a file path.
     */
    public Deck loadDeckFromFile(Path filePath) throws IOException {
        try (InputStream stream = Files.newInputStream(filePath)) {
            return loadDeckFromStream(stream, filePath.getFileName().toString());
        }
    }

    /**
     * Load a deck from an input stream.
     */
    public Deck loadDeckFromStream(InputStream stream, String fileName) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);

            if (!element.isJsonObject()) {
                LearnPlay.LOGGER.error("Invalid deck file format: {}", fileName);
                return null;
            }

            JsonObject json = element.getAsJsonObject();

            // Ensure deck has an ID
            if (!json.has("id")) {
                json.addProperty("id", fileName.replace(".json", ""));
            }

            Deck deck = Deck.fromJson(json);

            // Log deck structure
            String categoryInfo = deck.getCategoryId() != null ?
                    " (in category: " + deck.getCategoryId() + ")" : "";
            LearnPlay.LOGGER.info("  Loaded deck with {} cards{}", deck.getCardCount(), categoryInfo);

            return deck;
        }
    }

    /**
     * Save a deck to a JSON file.
     */
    public boolean saveDeck(Deck deck) {
        JsonObject json = deck.toJson();
        boolean success = jsonFileHandler.saveJson(deck.getId(), json);

        if (success) {
            LearnPlay.LOGGER.info("Saved deck: {}", deck.getName());
        }

        return success;
    }

    /**
     * Delete a deck file.
     */
    public boolean deleteDeckFile(String deckId) {
        return jsonFileHandler.deleteFile(deckId);
    }

    /**
     * Check if a deck file exists.
     */
    public boolean deckFileExists(String deckId) {
        return jsonFileHandler.fileExists(deckId);
    }
}

