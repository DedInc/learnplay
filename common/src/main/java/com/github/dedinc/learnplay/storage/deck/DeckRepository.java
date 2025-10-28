package com.github.dedinc.learnplay.storage.deck;

import com.github.dedinc.learnplay.data.model.Deck;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for managing deck storage and queries.
 */
public class DeckRepository {

    private final Map<String, Deck> decks = new HashMap<>();

    /**
     * Add or update a deck in the repository.
     */
    public void put(String deckId, Deck deck) {
        decks.put(deckId, deck);
    }

    /**
     * Get a deck by ID.
     */
    public Deck get(String deckId) {
        return decks.get(deckId);
    }

    /**
     * Remove a deck from the repository.
     */
    public void remove(String deckId) {
        decks.remove(deckId);
    }

    /**
     * Check if a deck exists.
     */
    public boolean contains(String deckId) {
        return decks.containsKey(deckId);
    }

    /**
     * Get all decks.
     */
    public Collection<Deck> getAll() {
        return Collections.unmodifiableCollection(decks.values());
    }

    /**
     * Get all deck IDs.
     */
    public Set<String> getAllIds() {
        return Collections.unmodifiableSet(decks.keySet());
    }

    /**
     * Get only enabled decks.
     */
    public Collection<Deck> getEnabled() {
        return decks.values().stream()
                .filter(Deck::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * Get decks by category ID.
     */
    public List<Deck> getByCategory(String categoryId) {
        if (categoryId == null) {
            return getUncategorized();
        }
        return decks.values().stream()
                .filter(deck -> categoryId.equals(deck.getCategoryId()))
                .collect(Collectors.toList());
    }

    /**
     * Get all uncategorized decks.
     */
    public List<Deck> getUncategorized() {
        return decks.values().stream()
                .filter(deck -> deck.getCategoryId() == null)
                .collect(Collectors.toList());
    }

    /**
     * Get total number of cards across all decks.
     */
    public int getTotalCardCount() {
        return decks.values().stream()
                .mapToInt(Deck::getCardCount)
                .sum();
    }

    /**
     * Clear all decks from the repository.
     */
    public void clear() {
        decks.clear();
    }

    /**
     * Get the number of decks in the repository.
     */
    public int size() {
        return decks.size();
    }
}

