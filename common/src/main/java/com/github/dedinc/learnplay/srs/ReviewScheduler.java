package com.github.dedinc.learnplay.srs;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import com.github.dedinc.learnplay.player.PlayerProgressManager;
import com.github.dedinc.learnplay.storage.DeckManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * ReviewScheduler manages card selection for review sessions.
 * Implements SM-2 algorithm scheduling logic:
 * <p>
 * Priority Order:
 * 1. Due cards (sorted by nextReview timestamp - oldest first)
 * - This ensures weak cards (reviewed as "Again"/"Hard") appear sooner
 * - Strong cards (reviewed as "Easy") appear later
 * 2. New cards (never reviewed before)
 * <p>
 * This follows Phase 2.3 of plan.md:
 * - getCardsForReview() returns cards due today
 * - getNewCards() returns cards never reviewed
 * - scheduleReview() updates state after review
 */
public class ReviewScheduler {

    // Configuration (can be moved to config later)
    private static final int DEFAULT_MAX_REVIEWS_PER_SESSION = 20;
    private static final int DEFAULT_MAX_NEW_CARDS_PER_SESSION = 10;

    private final PlayerProgressManager progressManager;
    private final DeckManager deckManager;

    public ReviewScheduler() {
        this.progressManager = PlayerProgressManager.getInstance();
        this.deckManager = DeckManager.getInstance();
    }

    /**
     * Get the next card for review based on SM-2 scheduling.
     * <p>
     * Selection logic:
     * 1. Prioritize due cards (sorted by nextReview - oldest/weakest first)
     * 2. If no due cards, return new cards
     * 3. If no cards available, return null
     *
     * @param playerName Player name
     * @return Next card to review, or null if no cards available
     */
    public Flashcard getNextCardForReview(String playerName) {
        // Get all available cards from enabled decks only
        Collection<Deck> decks = deckManager.getEnabledDecks();
        if (decks.isEmpty()) {
            LearnPlay.LOGGER.warn("No enabled decks - cannot get cards for review");
            return null;
        }

        // Collect all cards from enabled decks
        List<Flashcard> allCards = new ArrayList<>();
        for (Deck deck : decks) {
            allCards.addAll(deck.getCards());
        }

        if (allCards.isEmpty()) {
            LearnPlay.LOGGER.warn("No cards in any deck - cannot get cards for review");
            return null;
        }

        // Initialize player progress if needed
        progressManager.initializePlayer(playerName);

        // Step 1: Get due cards (sorted by nextReview timestamp - oldest first)
        List<CardWithState> dueCards = getDueCardsWithState(playerName, allCards);
        if (!dueCards.isEmpty()) {
            // Sort by nextReview time - cards that are most overdue come first
            // This ensures weak cards (short intervals from "Again"/"Hard") appear before strong cards
            dueCards.sort(Comparator.comparingLong(cws -> cws.state.getNextReview()));

            CardWithState selected = dueCards.get(0);
            LearnPlay.LOGGER.info("Selected due card: {} (due at: {}, current: {})",
                    selected.card.getId(),
                    selected.state.getNextReview(),
                    System.currentTimeMillis());
            return selected.card;
        }

        // Step 2: No due cards, get new cards
        List<Flashcard> newCards = getNewCards(playerName, allCards);
        if (!newCards.isEmpty()) {
            Flashcard selected = newCards.get(0);
            LearnPlay.LOGGER.info("Selected new card: {}", selected.getId());
            return selected;
        }

        // No cards available for review
        LearnPlay.LOGGER.info("No cards available for review - all cards are scheduled for future review");
        return null;
    }

    /**
     * Get all due cards for a player (cards ready for review).
     * Returns cards sorted by nextReview timestamp (oldest first).
     *
     * @param playerName Player name
     * @param maxCards   Maximum number of cards to return
     * @return List of due cards with their SRS state
     */
    public List<CardWithState> getDueCardsForReview(String playerName, int maxCards) {
        Collection<Deck> decks = deckManager.getEnabledDecks();
        List<Flashcard> allCards = new ArrayList<>();
        for (Deck deck : decks) {
            allCards.addAll(deck.getCards());
        }

        List<CardWithState> dueCards = getDueCardsWithState(playerName, allCards);

        // Sort by nextReview timestamp (oldest/weakest first)
        dueCards.sort(Comparator.comparingLong(cws -> cws.state.getNextReview()));

        // Limit to maxCards
        if (dueCards.size() > maxCards) {
            return dueCards.subList(0, maxCards);
        }

        return dueCards;
    }

    /**
     * Get new cards (never reviewed before) for a player.
     *
     * @param playerName Player name
     * @param maxCards   Maximum number of new cards to return
     * @return List of new cards
     */
    public List<Flashcard> getNewCardsForReview(String playerName, int maxCards) {
        Collection<Deck> decks = deckManager.getEnabledDecks();
        List<Flashcard> allCards = new ArrayList<>();
        for (Deck deck : decks) {
            allCards.addAll(deck.getCards());
        }

        List<Flashcard> newCards = getNewCards(playerName, allCards);

        // Limit to maxCards
        if (newCards.size() > maxCards) {
            return newCards.subList(0, maxCards);
        }

        return newCards;
    }

    /**
     * Get cards that are due for review with their SRS state.
     * Internal helper method.
     */
    private List<CardWithState> getDueCardsWithState(String playerName, List<Flashcard> allCards) {
        List<CardWithState> dueCards = new ArrayList<>();

        for (Flashcard card : allCards) {
            SRSState state = progressManager.getCardState(playerName, card.getId());

            // Card has been reviewed and is due
            if (state != null && state.isDue()) {
                dueCards.add(new CardWithState(card, state));
            }
        }

        return dueCards;
    }

    /**
     * Get cards that have never been reviewed.
     * Internal helper method.
     */
    private List<Flashcard> getNewCards(String playerName, List<Flashcard> allCards) {
        List<Flashcard> newCards = new ArrayList<>();

        for (Flashcard card : allCards) {
            SRSState state = progressManager.getCardState(playerName, card.getId());

            // Card has never been reviewed
            if (state == null) {
                newCards.add(card);
            }
        }

        return newCards;
    }

    /**
     * Get statistics about available cards for review.
     *
     * @param playerName Player name
     * @return Review statistics
     */
    public ReviewStats getReviewStats(String playerName) {
        Collection<Deck> decks = deckManager.getEnabledDecks();
        List<Flashcard> allCards = new ArrayList<>();
        for (Deck deck : decks) {
            allCards.addAll(deck.getCards());
        }

        int totalCards = allCards.size();
        int reviseCards = getDueCardsWithState(playerName, allCards).size();
        int learnCards = getNewCards(playerName, allCards).size();
        int reviewedCards = totalCards - learnCards;
        int scheduledCards = reviewedCards - reviseCards;

        return new ReviewStats(totalCards, reviseCards, learnCards, reviewedCards, scheduledCards);
    }

    /**
     * Helper class to hold a card and its SRS state together.
     */
    public static class CardWithState {
        public final Flashcard card;
        public final SRSState state;

        public CardWithState(Flashcard card, SRSState state) {
            this.card = card;
            this.state = state;
        }
    }

    /**
     * Review statistics data class.
     * <p>
     * Terminology:
     * - Learn: Cards not yet learned (new cards)
     * - Revise: Cards ready to be reviewed (due cards)
     */
    public static class ReviewStats {
        public final int totalCards;
        public final int reviseCards;
        public final int learnCards;
        public final int reviewedCards;
        public final int scheduledCards;

        public ReviewStats(int totalCards, int reviseCards, int learnCards, int reviewedCards, int scheduledCards) {
            this.totalCards = totalCards;
            this.reviseCards = reviseCards;
            this.learnCards = learnCards;
            this.reviewedCards = reviewedCards;
            this.scheduledCards = scheduledCards;
        }

        @Override
        public String toString() {
            return String.format("ReviewStats{total=%d, revise=%d, learn=%d, reviewed=%d, scheduled=%d}",
                    totalCards, reviseCards, learnCards, reviewedCards, scheduledCards);
        }
    }
}
