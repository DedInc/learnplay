package com.github.dedinc.learnplay.srs;

import com.github.dedinc.learnplay.data.model.SRSState;

/**
 * Simple Fixed Interval Algorithm
 * <p>
 * A straightforward spaced repetition system with fixed interval progression.
 * Uses only two buttons: Forgot and Remember.
 * <p>
 * Interval Progression:
 * - 10 minutes
 * - 20 minutes
 * - 1 day
 * - 3 days
 * - 7 days
 * - 14 days
 * - 30 days
 * - 60 days
 * - 120 days
 * - 240 days (max)
 * <p>
 * If "Forgot" is selected, the card resets to 10 minutes.
 * If "Remember" is selected, the card advances to the next interval.
 */
public class SimpleIntervalAlgorithm {

    // Fixed interval progression in minutes
    private static final int[] INTERVALS_MINUTES = {
            10,      // 10 minutes
            20,      // 20 minutes
            1440,    // 1 day (24 * 60)
            4320,    // 3 days (3 * 24 * 60)
            10080,   // 7 days (7 * 24 * 60)
            20160,   // 14 days (14 * 24 * 60)
            43200,   // 30 days (30 * 24 * 60)
            86400,   // 60 days (60 * 24 * 60)
            172800,  // 120 days (120 * 24 * 60)
            345600   // 240 days (240 * 24 * 60)
    };

    /**
     * Calculate the next review state when the user remembers the card.
     * Advances to the next interval in the progression.
     *
     * @param currentState The current SRS state
     * @return Updated SRS state with new interval and next review time
     */
    public static SRSState remember(SRSState currentState) {
        int currentRepetitions = currentState.getRepetitions();
        int nextRepetitions = Math.min(currentRepetitions + 1, INTERVALS_MINUTES.length - 1);

        int intervalMinutes = INTERVALS_MINUTES[nextRepetitions];

        currentState.setRepetitions(nextRepetitions);
        currentState.setInterval(intervalMinutes / 1440); // Store as days for compatibility

        // Calculate next review time
        long nextReviewTime = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);
        currentState.setNextReviewTime(nextReviewTime);
        currentState.setLastReviewTime(System.currentTimeMillis());

        return currentState;
    }

    /**
     * Calculate the next review state when the user forgets the card.
     * Resets to the first interval (10 minutes).
     *
     * @param currentState The current SRS state
     * @return Updated SRS state reset to first interval
     */
    public static SRSState forgot(SRSState currentState) {
        int intervalMinutes = INTERVALS_MINUTES[0]; // Reset to 10 minutes

        currentState.setRepetitions(0);
        currentState.setInterval(intervalMinutes / 1440); // Store as days for compatibility

        // Calculate next review time
        long nextReviewTime = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);
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
     * Get a human-readable description of the next interval if the user remembers.
     *
     * @param currentState Current SRS state
     * @return Human-readable interval (e.g., "10 min", "1 day", "3 days")
     */
    public static String previewRememberInterval(SRSState currentState) {
        int currentRepetitions = currentState.getRepetitions();
        int nextRepetitions = Math.min(currentRepetitions + 1, INTERVALS_MINUTES.length - 1);
        int intervalMinutes = INTERVALS_MINUTES[nextRepetitions];

        return formatInterval(intervalMinutes);
    }

    /**
     * Get a human-readable description of the interval if the user forgets.
     * Always returns "10 min" since forgot resets to the first interval.
     *
     * @return Human-readable interval
     */
    public static String previewForgotInterval() {
        return formatInterval(INTERVALS_MINUTES[0]);
    }

    /**
     * Format an interval in minutes to a human-readable string.
     *
     * @param minutes The interval in minutes
     * @return Formatted string (e.g., "10 min", "1 day", "3 days")
     */
    private static String formatInterval(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else if (minutes < 1440) {
            int hours = minutes / 60;
            return hours + (hours == 1 ? " hour" : " hours");
        } else {
            int days = minutes / 1440;
            return days + (days == 1 ? " day" : " days");
        }
    }

    /**
     * Get the current interval level (0-9) for display purposes.
     *
     * @param state The SRS state
     * @return The current level in the interval progression
     */
    public static int getCurrentLevel(SRSState state) {
        return Math.min(state.getRepetitions(), INTERVALS_MINUTES.length - 1);
    }

    /**
     * Get the maximum level in the interval progression.
     *
     * @return The maximum level (9 for 240 days)
     */
    public static int getMaxLevel() {
        return INTERVALS_MINUTES.length - 1;
    }
}

