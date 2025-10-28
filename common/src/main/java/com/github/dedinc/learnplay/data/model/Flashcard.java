package com.github.dedinc.learnplay.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single flashcard with question, answer, and metadata.
 * Cards belong to a Deck, which belongs to a Category - no need for redundant category fields.
 * Immutable after creation for thread safety.
 */
public class Flashcard {
    private final String id;
    private final String question;
    private final String answer;
    private final List<String> tags;
    private final long createdAt;

    public Flashcard(String id, String question, String answer) {
        this(id, question, answer, new ArrayList<>(), System.currentTimeMillis());
    }

    public Flashcard(String id, String question, String answer, List<String> tags, long createdAt) {
        // Validation
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Flashcard ID cannot be null or empty");
        }
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null or empty");
        }
        if (answer == null || answer.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer cannot be null or empty");
        }

        this.id = id;
        this.question = question;
        this.answer = answer;
        this.tags = new ArrayList<>(tags != null ? tags : new ArrayList<>());
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags); // Return copy for immutability
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // JSON Serialization
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("question", question);
        json.addProperty("answer", answer);
        json.addProperty("createdAt", createdAt);

        if (!tags.isEmpty()) {
            JsonArray tagsArray = new JsonArray();
            for (String tag : tags) {
                tagsArray.add(tag);
            }
            json.add("tags", tagsArray);
        }

        return json;
    }

    public static Flashcard fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String question = json.get("question").getAsString();
        String answer = json.get("answer").getAsString();
        long createdAt = json.has("createdAt") ? json.get("createdAt").getAsLong() : System.currentTimeMillis();

        List<String> tags = new ArrayList<>();
        if (json.has("tags")) {
            JsonArray tagsArray = json.getAsJsonArray("tags");
            for (int i = 0; i < tagsArray.size(); i++) {
                tags.add(tagsArray.get(i).getAsString());
            }
        }

        return new Flashcard(id, question, answer, tags, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return Objects.equals(id, flashcard.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "id='" + id + '\'' +
                ", question='" + question + '\'' +
                '}';
    }
}

