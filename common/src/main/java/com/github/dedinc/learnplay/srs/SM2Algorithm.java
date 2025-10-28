package com.github.dedinc.learnplay.srs;

import com.github.dedinc.learnplay.data.model.ReviewRating;
import com.github.dedinc.learnplay.data.model.SRSState;

/**
 * SM-2 (SuperMemo 2) Algorithm Implementation
 * <p>
 * This is the classic spaced repetition algorithm developed by Piotr Wozniak in 1987.
 * It's simpler than FSRS but proven effective for flashcard learning.
 * <p>
 * Algorithm Overview:
 * - EF (Easiness Factor): Starts at 2.5, adjusted based on review quality
 * - Interval: Days until next review
 * - Repetitions: Number of consecutive successful reviews
 * <p>
 * Quality Ratings (0-5 in original, we use 0-3):
 * - 0 (Again): Complete blackout, restart
 * - 1 (Hard): Incorrect but remembered
 * - 2 (Good): Correct with effort
 * - 3 (Easy): Perfect recall
 */
public class SM2Algorithm {

    // SM-2 Constants
    private static final double MIN_EASE_FACTOR = 1.3;
    private static final double INITIAL_EASE_FACTOR = 2.5;
    private static final int INITIAL_INTERVAL = 1;

    /**
     * Calculate the next review state based on the current state and rating.
     *
     * @param currentState The current SRS state
     * @param rating       The user's rating of their recall
     * @return Updated SRS state with new interval, ease factor, and next review time
     */
    public static SRSState calculateNextReview(SRSState currentState, ReviewRating rating) {
        int quality = rating.getQuality();

        // Get current values
        double easeFactor = currentState.getEaseFactor();
        int repetitions = currentState.getRepetitions();
        int interval = currentState.getInterval();

        // Calculate new ease factor
        // Formula: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        // Simplified for our 0-3 scale: EF' = EF + (0.1 - (3 - q) * (0.08 + (3 - q) * 0.02))
        double newEaseFactor = easeFactor + (0.1 - (3 - quality) * (0.08 + (3 - quality) * 0.02));

        // Ensure ease factor doesn't go below minimum
        if (newEaseFactor < MIN_EASE_FACTOR) {
            newEaseFactor = MIN_EASE_FACTOR;
        }

        int newRepetitions;
        int newInterval;

        // If quality < 2 (Again or Hard), reset the card
        if (quality < 2) {
            newRepetitions = 0;
            newInterval = INITIAL_INTERVAL;
        } else {
            // Successful review
            newRepetitions = repetitions + 1;

            // Calculate new interval based on repetition count
            if (newRepetitions == 1) {
                newInterval = 1; // First review: 1 day
            } else if (newRepetitions == 2) {
                newInterval = 6; // Second review: 6 days
            } else {
                // Subsequent reviews: multiply previous interval by ease factor
                newInterval = (int) Math.round(interval * newEaseFactor);
            }
        }

        // Update the state
        currentState.setEaseFactor(newEaseFactor);
        currentState.setRepetitions(newRepetitions);
        currentState.setInterval(newInterval);

        // Calculate next review time (current time + interval in milliseconds)
        long nextReviewTime = System.currentTimeMillis() + (newInterval * 24L * 60L * 60L * 1000L);
        currentState.setNextReviewTime(nextReviewTime);
        currentState.setLastReviewTime(System.currentTimeMillis());

        return currentState;
    }

    /**
     * Check if a card is due for review.
     *
     * @param state The SRS state to check
     * @return true if the card should be reviewed now
     */
    public static boolean isDue(SRSState state) {
        return System.currentTimeMillis() >= state.getNextReviewTime();
    }

    /**
     * Get the number of days until the next review.
     *
     * @param state The SRS state
     * @return Days until next review (negative if overdue)
     */
    public static int getDaysUntilReview(SRSState state) {
        long millisUntilReview = state.getNextReviewTime() - System.currentTimeMillis();
        return (int) (millisUntilReview / (24L * 60L * 60L * 1000L));
    }

    /**
     * Reset a card to initial state (for "Again" rating or manual reset).
     *
     * @param state The SRS state to reset
     * @return The reset state
     */
    public static SRSState resetCard(SRSState state) {
        state.setEaseFactor(INITIAL_EASE_FACTOR);
        state.setRepetitions(0);
        state.setInterval(INITIAL_INTERVAL);
        state.setNextReviewTime(System.currentTimeMillis());
        return state;
    }

    /**
     * Get a human-readable description of the next interval based on rating.
     * This is useful for showing users what will happen if they choose a rating.
     *
     * @param currentState Current SRS state
     * @param rating       The rating to preview
     * @return Human-readable interval (e.g., "1 day", "6 days", "2 weeks")
     */
    public static String previewInterval(SRSState currentState, ReviewRating rating) {
        // Create a copy to avoid modifying the original
        SRSState tempState = new SRSState(currentState.getCardId());
        tempState.setEaseFactor(currentState.getEaseFactor());
        tempState.setRepetitions(currentState.getRepetitions());
        tempState.setInterval(currentState.getInterval());

        // Calculate what the interval would be
        SRSState result = calculateNextReview(tempState, rating);
        int interval = result.getInterval();

        // Format the interval nicely
        if (interval < 1) {
            return "< 1 day";
        } else if (interval == 1) {
            return "1 day";
        } else if (interval < 7) {
            return interval + " days";
        } else if (interval < 30) {
            int weeks = interval / 7;
            return weeks + (weeks == 1 ? " week" : " weeks");
        } else if (interval < 365) {
            int months = interval / 30;
            return months + (months == 1 ? " month" : " months");
        } else {
            int years = interval / 365;
            return years + (years == 1 ? " year" : " years");
        }
    }
}

