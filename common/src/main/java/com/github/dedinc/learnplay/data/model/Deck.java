package com.github.dedinc.learnplay.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a collection of flashcards (a deck).
 * Can be enabled/disabled for review sessions.
 * Decks are contained within categories (folders).
 */
public class Deck {
    private final String id;
    private String name;
    private String description;
    private boolean enabled;
    private String categoryId; // ID of the category this deck belongs to (null for uncategorized)
    private final List<Flashcard> cards;

    public Deck(String id, String name) {
        this(id, name, "", true, null, new ArrayList<>());
    }

    public Deck(String id, String name, String description, boolean enabled, List<Flashcard> cards) {
        this(id, name, description, enabled, null, cards);
    }

    public Deck(String id, String name, String description, boolean enabled, String categoryId, List<Flashcard> cards) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
        this.enabled = enabled;
        this.categoryId = categoryId;
        this.cards = new ArrayList<>(cards != null ? cards : new ArrayList<>());
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public List<Flashcard> getCards() {
        return new ArrayList<>(cards); // Return copy for safety
    }

    public int getCardCount() {
        return cards.size();
    }

    // Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    // Card management
    public void addCard(Flashcard card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add null card");
        }
        // Check for duplicate IDs
        for (Flashcard existing : cards) {
            if (existing.getId().equals(card.getId())) {
                throw new IllegalArgumentException("Card with ID " + card.getId() + " already exists in deck");
            }
        }
        cards.add(card);
    }

    public boolean removeCard(String cardId) {
        return cards.removeIf(card -> card.getId().equals(cardId));
    }

    public Flashcard getCard(String cardId) {
        for (Flashcard card : cards) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null;
    }

    public boolean hasCard(String cardId) {
        return getCard(cardId) != null;
    }

    // JSON Serialization
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("enabled", enabled);

        if (categoryId != null) {
            json.addProperty("categoryId", categoryId);
        }

        // Save cards
        if (!cards.isEmpty()) {
            JsonArray cardsArray = new JsonArray();
            for (Flashcard card : cards) {
                cardsArray.add(card.toJson());
            }
            json.add("cards", cardsArray);
        }

        return json;
    }

    public static Deck fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        String description = json.has("description") ? json.get("description").getAsString() : "";
        boolean enabled = json.has("enabled") ? json.get("enabled").getAsBoolean() : true;
        String categoryId = json.has("categoryId") ? json.get("categoryId").getAsString() : null;

        // Load cards
        List<Flashcard> cards = new ArrayList<>();
        if (json.has("cards")) {
            JsonArray cardsArray = json.getAsJsonArray("cards");
            for (int i = 0; i < cardsArray.size(); i++) {
                try {
                    Flashcard card = Flashcard.fromJson(cardsArray.get(i).getAsJsonObject());
                    cards.add(card);
                } catch (Exception e) {
                    // Log error but continue loading other cards
                    System.err.println("Failed to load card in deck " + id + ": " + e.getMessage());
                }
            }
        }

        return new Deck(id, name, description, enabled, categoryId, cards);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deck deck = (Deck) o;
        return Objects.equals(id, deck.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Deck{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cards=" + cards.size() +
                ", enabled=" + enabled +
                '}';
    }
}

