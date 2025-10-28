package com.github.dedinc.learnplay.client;

import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * Platform-specific client methods.
 * Implementations are in fabric/forge/neoforge modules.
 */
public class LearnPlayPlatformClient {

    /**
     * Create a review screen for a flashcard.
     * Returns Object to avoid importing platform-specific Screen class.
     *
     * @param card       The flashcard to review
     * @param state      The SRS state for this card
     * @param playerName The player's name
     * @return Platform-specific Screen object
     */
    @ExpectPlatform
    public static Object createReviewScreen(Flashcard card, SRSState state, String playerName) {
        throw new AssertionError("Platform-specific implementation not found!");
    }

    /**
     * Open the review screen on the client.
     *
     * @param card       The flashcard to review
     * @param state      The SRS state for this card
     * @param playerName The player's name
     */
    @ExpectPlatform
    public static void openReviewScreen(Flashcard card, SRSState state, String playerName) {
        throw new AssertionError("Platform-specific implementation not found!");
    }
}

