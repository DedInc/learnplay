package com.github.dedinc.learnplay.storage;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.storage.deck.BuiltInDeckManager;
import com.github.dedinc.learnplay.storage.deck.DeckFileHandler;
import com.github.dedinc.learnplay.storage.deck.DeckRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Facade for managing flashcard decks.
 * Coordinates between DeckRepository, DeckFileHandler, and BuiltInDeckManager.
 * <p>
 * Supports two sources:
 * 1. Built-in decks from mod resources (assets/learnplay/flashcards/)
 * 2. User-created decks from config folder (config/learnplay/decks/)
 */
public class DeckManager {

    private final DeckRepository repository;
    private final DeckFileHandler fileHandler;
    private final BuiltInDeckManager builtInManager;

    // Singleton instance
    private static DeckManager instance;

    private DeckManager() {
        this.repository = new DeckRepository();
        this.fileHandler = new DeckFileHandler();
        this.builtInManager = new BuiltInDeckManager(fileHandler);
    }

    public static DeckManager getInstance() {
        if (instance == null) {
            instance = new DeckManager();
        }
        return instance;
    }

    /**
     * Load all decks from both resources and config folder.
     * User decks (from config folder) take precedence over built-in decks with the same ID.
     */
    public void loadAllDecks() {
        LearnPlay.LOGGER.info("Loading flashcard decks...");

        // Load built-in decks
        List<Deck> builtInDecks = builtInManager.loadBuiltInDecks();
        for (Deck deck : builtInDecks) {
            repository.put(deck.getId(), deck);
        }

        // Load user decks (these override built-in decks with same ID)
        List<Deck> userDecks = fileHandler.loadUserDecks();
        for (Deck deck : userDecks) {
            if (repository.contains(deck.getId())) {
                LearnPlay.LOGGER.info("User deck overrides built-in deck: {} ({} cards)",
                        deck.getName(), deck.getCardCount());
            }
            repository.put(deck.getId(), deck);
        }

        LearnPlay.LOGGER.info("Loaded {} deck(s) with {} total cards",
                repository.size(), repository.getTotalCardCount());
    }


    /**
     * Get a deck by ID.
     */
    public Deck getDeck(String deckId) {
        return repository.get(deckId);
    }

    /**
     * Get all loaded decks.
     */
    public Collection<Deck> getAllDecks() {
        return repository.getAll();
    }

    /**
     * Get only enabled decks.
     */
    public Collection<Deck> getEnabledDecks() {
        return repository.getEnabled();
    }

    /**
     * Get all deck IDs.
     */
    public Set<String> getDeckIds() {
        return repository.getAllIds();
    }

    /**
     * Get total number of cards across all decks.
     */
    public int getTotalCardCount() {
        return repository.getTotalCardCount();
    }

    /**
     * Check if a deck exists.
     */
    public boolean hasDeck(String deckId) {
        return repository.contains(deckId);
    }

    /**
     * Get decks by category ID.
     */
    public List<Deck> getDecksByCategory(String categoryId) {
        return repository.getByCategory(categoryId);
    }

    /**
     * Get all uncategorized decks (decks not in any category).
     */
    public List<Deck> getUncategorizedDecks() {
        return repository.getUncategorized();
    }

    /**
     * Reload all decks (useful for config changes).
     */
    public void reload() {
        repository.clear();
        loadAllDecks();
    }

    /**
     * Save a deck to the config folder.
     *
     * @param deck The deck to save
     * @return true if save was successful
     */
    public boolean saveDeck(Deck deck) {
        return fileHandler.saveDeck(deck);
    }

    /**
     * Delete a deck file from the config folder and remove from memory.
     * For built-in decks, creates a deletion marker so they won't reload.
     * For user decks, deletes the file.
     *
     * @param deckId The ID of the deck to delete
     * @return true if deletion was successful
     */
    public boolean deleteDeck(String deckId) {
        if (!repository.contains(deckId)) {
            LearnPlay.LOGGER.warn("Cannot delete deck {}: not found in memory", deckId);
            return false;
        }

        // Check if this is a built-in deck
        if (builtInManager.isBuiltInDeck(deckId)) {
            // For built-in decks, create a deletion marker
            builtInManager.markAsDeleted(deckId);

            // Also delete the user override file if it exists
            if (fileHandler.deckFileExists(deckId)) {
                fileHandler.deleteDeckFile(deckId);
                LearnPlay.LOGGER.info("Deleted user override for built-in deck: {}", deckId);
            }

            LearnPlay.LOGGER.info("Marked built-in deck as deleted: {}", deckId);
        } else {
            // For user-created decks, just delete the file
            fileHandler.deleteDeckFile(deckId);
        }

        // Always remove from memory
        repository.remove(deckId);
        LearnPlay.LOGGER.info("Removed deck from memory: {}", deckId);
        return true;
    }

    /**
     * Toggle a deck's enabled status and save it.
     *
     * @param deckId The ID of the deck to toggle
     * @return true if toggle was successful
     */
    public boolean toggleDeckEnabled(String deckId) {
        Deck deck = repository.get(deckId);
        if (deck == null) {
            LearnPlay.LOGGER.warn("Cannot toggle deck {}: not found", deckId);
            return false;
        }

        deck.setEnabled(!deck.isEnabled());
        return saveDeck(deck);
    }

    /**
     * Add a new deck to the manager and save it.
     *
     * @param deck The deck to add
     * @return true if add was successful
     */
    public boolean addDeck(Deck deck) {
        if (repository.contains(deck.getId())) {
            LearnPlay.LOGGER.warn("Cannot add deck {}: already exists", deck.getId());
            return false;
        }

        repository.put(deck.getId(), deck);
        return saveDeck(deck);
    }

    /**
     * Update an existing deck and save it.
     *
     * @param deck The deck to update
     * @return true if update was successful
     */
    public boolean updateDeck(Deck deck) {
        if (!repository.contains(deck.getId())) {
            LearnPlay.LOGGER.warn("Cannot update deck {}: not found", deck.getId());
            return false;
        }

        repository.put(deck.getId(), deck);
        return saveDeck(deck);
    }
}

