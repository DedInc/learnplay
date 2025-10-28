package com.github.dedinc.learnplay.data.model;

import com.google.gson.JsonObject;

/**
 * Represents the Spaced Repetition System state for a single card.
 * Tracks learning progress using SM-2 algorithm parameters.
 */
public class SRSState {
    // SM-2 Algorithm Constants
    public static final double INITIAL_EASE_FACTOR = 2.5;
    public static final double MIN_EASE_FACTOR = 1.3;
    public static final int INITIAL_INTERVAL = 1; // days

    private final String cardId;
    private int interval; // days until next review
    private double easeFactor; // multiplier for interval calculation
    private int repetitions; // number of successful reviews
    private long lastReview; // timestamp of last review (milliseconds)
    private long nextReview; // timestamp when card is due (milliseconds)

    /**
     * Create a new SRS state for a card (never reviewed before)
     */
    public SRSState(String cardId) {
        this(cardId, INITIAL_INTERVAL, INITIAL_EASE_FACTOR, 0, 0, System.currentTimeMillis());
    }

    /**
     * Create SRS state with specific values (for loading from storage)
     */
    public SRSState(String cardId, int interval, double easeFactor, int repetitions, long lastReview, long nextReview) {
        if (cardId == null || cardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Card ID cannot be null or empty");
        }

        this.cardId = cardId;
        this.interval = Math.max(1, interval);
        this.easeFactor = Math.max(MIN_EASE_FACTOR, easeFactor);
        this.repetitions = Math.max(0, repetitions);
        this.lastReview = lastReview;
        this.nextReview = nextReview;
    }

    // Getters
    public String getCardId() {
        return cardId;
    }

    public int getInterval() {
        return interval;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public long getLastReview() {
        return lastReview;
    }

    public long getNextReview() {
        return nextReview;
    }

    // Convenience getters with different names for SM2Algorithm compatibility
    public long getNextReviewTime() {
        return nextReview;
    }

    public long getLastReviewTime() {
        return lastReview;
    }

    // Setters (public for SM2Algorithm access)
    public void setInterval(int interval) {
        this.interval = Math.max(1, interval);
    }

    public void setEaseFactor(double easeFactor) {
        this.easeFactor = Math.max(MIN_EASE_FACTOR, easeFactor);
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = Math.max(0, repetitions);
    }

    public void setLastReview(long lastReview) {
        this.lastReview = lastReview;
    }

    public void setNextReview(long nextReview) {
        this.nextReview = nextReview;
    }

    // Convenience setters with different names for SM2Algorithm compatibility
    public void setNextReviewTime(long nextReviewTime) {
        this.nextReview = nextReviewTime;
    }

    public void setLastReviewTime(long lastReviewTime) {
        this.lastReview = lastReviewTime;
    }

    /**
     * Check if this card is due for review
     */
    public boolean isDue() {
        return System.currentTimeMillis() >= nextReview;
    }

    /**
     * Check if this is a new card (never reviewed)
     */
    public boolean isNew() {
        return repetitions == 0 && lastReview == 0;
    }

    /**
     * Get days until next review (can be negative if overdue)
     */
    public int getDaysUntilReview() {
        long diff = nextReview - System.currentTimeMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    // JSON Serialization
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("cardId", cardId);
        json.addProperty("interval", interval);
        json.addProperty("easeFactor", easeFactor);
        json.addProperty("repetitions", repetitions);
        json.addProperty("lastReview", lastReview);
        json.addProperty("nextReview", nextReview);
        return json;
    }

    public static SRSState fromJson(JsonObject json) {
        String cardId = json.get("cardId").getAsString();
        int interval = json.has("interval") ? json.get("interval").getAsInt() : INITIAL_INTERVAL;
        double easeFactor = json.has("easeFactor") ? json.get("easeFactor").getAsDouble() : INITIAL_EASE_FACTOR;
        int repetitions = json.has("repetitions") ? json.get("repetitions").getAsInt() : 0;
        long lastReview = json.has("lastReview") ? json.get("lastReview").getAsLong() : 0;
        long nextReview = json.has("nextReview") ? json.get("nextReview").getAsLong() : System.currentTimeMillis();

        return new SRSState(cardId, interval, easeFactor, repetitions, lastReview, nextReview);
    }

    @Override
    public String toString() {
        return "SRSState{" +
                "cardId='" + cardId + '\'' +
                ", interval=" + interval +
                ", easeFactor=" + easeFactor +
                ", repetitions=" + repetitions +
                ", isDue=" + isDue() +
                '}';
    }

    /**
     * Create a copy of this state (for immutable updates)
     */
    public SRSState copy() {
        return new SRSState(cardId, interval, easeFactor, repetitions, lastReview, nextReview);
    }
}

