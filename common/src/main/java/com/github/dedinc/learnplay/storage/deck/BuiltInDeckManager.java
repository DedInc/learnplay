package com.github.dedinc.learnplay.storage.deck;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.Deck;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages built-in decks from mod resources and their deletion markers.
 */
public class BuiltInDeckManager {

    private static final String RESOURCE_PATH = "/assets/learnplay/flashcards/";
    private static final String CONFIG_PATH = "config/learnplay/decks/";
    private static final String[] BUILT_IN_DECK_IDS = {"example_hierarchical"};

    private final DeckFileHandler fileHandler;

    public BuiltInDeckManager(DeckFileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    /**
     * Load all built-in decks from resources.
     */
    public List<Deck> loadBuiltInDecks() {
        List<Deck> decks = new ArrayList<>();
        String[] builtInFiles = {"example.json"};

        for (String deckFile : builtInFiles) {
            try {
                InputStream stream = getClass().getResourceAsStream(RESOURCE_PATH + deckFile);
                if (stream != null) {
                    Deck deck = fileHandler.loadDeckFromStream(stream, deckFile);
                    if (deck != null && !isMarkedAsDeleted(deck.getId())) {
                        decks.add(deck);
                        LearnPlay.LOGGER.info("Loaded built-in deck: {} ({} cards)",
                                deck.getName(), deck.getCardCount());
                    } else if (deck != null) {
                        LearnPlay.LOGGER.info("Skipping deleted built-in deck: {}", deck.getName());
                    }
                }
            } catch (Exception e) {
                LearnPlay.LOGGER.error("Failed to load built-in deck: {}", deckFile, e);
            }
        }

        return decks;
    }

    /**
     * Check if a deck ID corresponds to a built-in deck.
     */
    public boolean isBuiltInDeck(String deckId) {
        for (String builtInId : BUILT_IN_DECK_IDS) {
            if (builtInId.equals(deckId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a built-in deck has been marked as deleted.
     */
    public boolean isMarkedAsDeleted(String deckId) {
        Path deletedMarker = getDeletedMarkerPath(deckId);
        return Files.exists(deletedMarker);
    }

    /**
     * Mark a built-in deck as deleted.
     */
    public void markAsDeleted(String deckId) {
        Path deletedMarker = getDeletedMarkerPath(deckId);

        try {
            Path configDir = Paths.get(CONFIG_PATH);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Files.write(deletedMarker, new byte[0]);
            LearnPlay.LOGGER.info("Marked built-in deck as deleted: {}", deckId);
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to create deletion marker for deck: {}", deckId, e);
        }
    }

    /**
     * Unmark a built-in deck as deleted.
     */
    public void unmarkAsDeleted(String deckId) {
        Path deletedMarker = getDeletedMarkerPath(deckId);

        try {
            if (Files.exists(deletedMarker)) {
                Files.delete(deletedMarker);
                LearnPlay.LOGGER.info("Unmarked built-in deck as deleted: {}", deckId);
            }
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to remove deletion marker for deck: {}", deckId, e);
        }
    }

    private Path getDeletedMarkerPath(String deckId) {
        Path configDir = Paths.get(CONFIG_PATH);
        return configDir.resolve(".deleted_" + deckId);
    }
}

