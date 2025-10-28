package com.github.dedinc.learnplay.data.model;

/**
 * Rating given by the user when reviewing a flashcard.
 * Used by SM-2 algorithm to calculate next review interval.
 */
public enum ReviewRating {
    /**
     * Complete blackout - didn't remember at all.
     * Resets the card to the beginning.
     */
    AGAIN(0, "Again", 0x0),

    /**
     * Incorrect response, but recognized the answer.
     * Reduces ease factor and interval.
     */
    HARD(1, "Hard", 0x1),

    /**
     * Correct response with hesitation.
     * Normal interval increase.
     */
    GOOD(2, "Good", 0x2),

    /**
     * Perfect response, no hesitation.
     * Increased interval and ease factor.
     */
    EASY(3, "Easy", 0x3);

    private final int value;
    private final String displayName;
    private final int quality; // SM-2 quality (0-5 scale, we use 0-3)

    ReviewRating(int value, String displayName, int quality) {
        this.value = value;
        this.displayName = displayName;
        this.quality = quality;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getQuality() {
        return quality;
    }

    /**
     * Get rating from integer value
     */
    public static ReviewRating fromValue(int value) {
        for (ReviewRating rating : values()) {
            if (rating.value == value) {
                return rating;
            }
        }
        return GOOD; // default fallback
    }

    /**
     * Check if this rating is considered a "pass" (card was remembered)
     */
    public boolean isPass() {
        return this != AGAIN;
    }

    /**
     * Get color for UI display (Minecraft color codes)
     */
    public int getColor() {
        switch (this) {
            case AGAIN:
                return 0xFF5555; // Red
            case HARD:
                return 0xFFAA00; // Orange
            case GOOD:
                return 0x55FF55; // Green
            case EASY:
                return 0x55FFFF; // Cyan
            default:
                return 0xFFFFFF; // White
        }
    }
}

