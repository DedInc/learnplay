package com.github.dedinc.learnplay.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a subcategory within a category that contains flashcards.
 * Provides fine-grained organization for learning content.
 */
public class Subcategory {
    private final String id;
    private String name;
    private String description;
    private final List<Flashcard> cards;

    public Subcategory(String id, String name) {
        this(id, name, "", new ArrayList<>());
    }

    public Subcategory(String id, String name, String description, List<Flashcard> cards) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Subcategory ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subcategory name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
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

    public List<Flashcard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public int getCardCount() {
        return cards.size();
    }

    // Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subcategory name cannot be null or empty");
        }
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    // Card management
    public void addCard(Flashcard card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add null card");
        }
        // Check for duplicate IDs
        for (Flashcard existing : cards) {
            if (existing.getId().equals(card.getId())) {
                throw new IllegalArgumentException("Card with ID " + card.getId() + " already exists in subcategory");
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

        JsonArray cardsArray = new JsonArray();
        for (Flashcard card : cards) {
            cardsArray.add(card.toJson());
        }
        json.add("cards", cardsArray);

        return json;
    }

    public static Subcategory fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        String description = json.has("description") ? json.get("description").getAsString() : "";

        List<Flashcard> cards = new ArrayList<>();
        if (json.has("cards")) {
            JsonArray cardsArray = json.getAsJsonArray("cards");
            for (int i = 0; i < cardsArray.size(); i++) {
                try {
                    Flashcard card = Flashcard.fromJson(cardsArray.get(i).getAsJsonObject());
                    cards.add(card);
                } catch (Exception e) {
                    System.err.println("Failed to load card in subcategory " + id + ": " + e.getMessage());
                }
            }
        }

        return new Subcategory(id, name, description, cards);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subcategory that = (Subcategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Subcategory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cards=" + cards.size() +
                '}';
    }
}

