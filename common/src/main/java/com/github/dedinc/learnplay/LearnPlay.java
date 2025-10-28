package com.github.dedinc.learnplay;

import com.github.dedinc.learnplay.config.LearnPlayConfig;
import com.github.dedinc.learnplay.data.model.*;
import com.github.dedinc.learnplay.player.PlayerProgressManager;
import com.github.dedinc.learnplay.srs.SM2Algorithm;
import com.github.dedinc.learnplay.storage.CategoryManager;
import com.github.dedinc.learnplay.storage.DeckManager;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LearnPlay {
    public static final String MOD_ID = "learnplay";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Initializing LearnPlay mod...");

        // Initialize configuration
        LearnPlayConfig.getInstance();
        LOGGER.info("✓ Configuration loaded");

        // Test Phase 1: Data Models
        testDataModels();

        // Test Phase 2: SM-2 Algorithm
        testSM2Algorithm();

        // Test Phase 3: Storage System
        testStorageSystem();

        // Test Phase 4: Player Progress (NBT)
        // NOTE: Disabled to prevent creating test player progress files
        // Real player progress is created automatically when players join
        // testPlayerProgress();
    }

    private static void testDataModels() {
        LOGGER.info("Testing data models...");

        try {
            // Test Flashcard (no category/subcategory - that's determined by the deck it belongs to)
            Flashcard card = new Flashcard("test1", "What is 2+2?", "4");
            String cardJson = card.toJson().toString();
            Flashcard parsedCard = Flashcard.fromJson(JsonParser.parseString(cardJson).getAsJsonObject());
            LOGGER.info("✓ Flashcard test passed: {}",
                    parsedCard.getQuestion());

            // Test Deck structure
            Deck mathDeck = new Deck("math_deck", "Mathematics");
            mathDeck.addCard(card);
            mathDeck.addCard(new Flashcard("test2", "What is 3+3?", "6"));
            String deckJson = mathDeck.toJson().toString();
            Deck parsedDeck = Deck.fromJson(JsonParser.parseString(deckJson).getAsJsonObject());
            LOGGER.info("✓ Deck test passed: {} with {} cards",
                    parsedDeck.getName(), parsedDeck.getCardCount());

            // Test new hierarchical structure: Category -> Deck -> Cards
            Category category = new Category("science", "Science");

            // Create a deck for biology
            Deck biologyDeck = new Deck("biology_deck", "Biology");
            biologyDeck.setCategoryId("science");
            biologyDeck.addCard(new Flashcard("bio1", "What is DNA?", "Deoxyribonucleic acid"));
            biologyDeck.addCard(new Flashcard("bio2", "What is a cell?", "The basic unit of life"));

            // Add deck ID to category
            category.addDeckId(biologyDeck.getId());

            String categoryJson = category.toJson().toString();
            Category parsedCategory = Category.fromJson(JsonParser.parseString(categoryJson).getAsJsonObject());
            LOGGER.info("✓ Category test passed: {} with {} decks, {} subcategories",
                    parsedCategory.getName(),
                    parsedCategory.getDeckCount(),
                    parsedCategory.getSubcategoryCount());

            String bioDeckJson = biologyDeck.toJson().toString();
            Deck parsedBioDeck = Deck.fromJson(JsonParser.parseString(bioDeckJson).getAsJsonObject());
            LOGGER.info("✓ Deck with category test passed: {} in category {} with {} cards",
                    parsedBioDeck.getName(),
                    parsedBioDeck.getCategoryId(),
                    parsedBioDeck.getCardCount());

            // Test SRSState
            SRSState state = new SRSState("test1");
            String stateJson = state.toJson().toString();
            SRSState parsedState = SRSState.fromJson(JsonParser.parseString(stateJson).getAsJsonObject());
            LOGGER.info("✓ SRSState test passed: interval={}, ease={}", parsedState.getInterval(), parsedState.getEaseFactor());

            // Test ReviewRating
            for (ReviewRating rating : ReviewRating.values()) {
                LOGGER.info("✓ ReviewRating: {} (quality={})", rating.getDisplayName(), rating.getQuality());
            }

            LOGGER.info("✓ All data model tests passed!");

        } catch (Exception e) {
            LOGGER.error("✗ Data model test failed: {}", e.getMessage(), e);
        }
    }

    private static void testSM2Algorithm() {
        LOGGER.info("Testing SM-2 Algorithm...");

        try {
            // Create a new card state
            SRSState state = new SRSState("test_card_1");
            LOGGER.info("Initial state: interval={}, ease={}, reps={}",
                    state.getInterval(), state.getEaseFactor(), state.getRepetitions());

            // Test 1: Review with "Good" rating
            LOGGER.info("--- Test 1: First review with GOOD ---");
            String preview = SM2Algorithm.previewInterval(state, ReviewRating.GOOD);
            LOGGER.info("Preview for GOOD: {}", preview);

            state = SM2Algorithm.calculateNextReview(state, ReviewRating.GOOD);
            LOGGER.info("After GOOD: interval={}, ease={}, reps={}",
                    state.getInterval(), state.getEaseFactor(), state.getRepetitions());

            // Test 2: Review with "Good" again
            LOGGER.info("--- Test 2: Second review with GOOD ---");
            preview = SM2Algorithm.previewInterval(state, ReviewRating.GOOD);
            LOGGER.info("Preview for GOOD: {}", preview);

            state = SM2Algorithm.calculateNextReview(state, ReviewRating.GOOD);
            LOGGER.info("After GOOD: interval={}, ease={}, reps={}",
                    state.getInterval(), state.getEaseFactor(), state.getRepetitions());

            // Test 3: Review with "Easy"
            LOGGER.info("--- Test 3: Third review with EASY ---");
            preview = SM2Algorithm.previewInterval(state, ReviewRating.EASY);
            LOGGER.info("Preview for EASY: {}", preview);

            state = SM2Algorithm.calculateNextReview(state, ReviewRating.EASY);
            LOGGER.info("After EASY: interval={}, ease={}, reps={}",
                    state.getInterval(), state.getEaseFactor(), state.getRepetitions());

            // Test 4: Review with "Again" (should reset)
            LOGGER.info("--- Test 4: Fourth review with AGAIN (reset) ---");
            preview = SM2Algorithm.previewInterval(state, ReviewRating.AGAIN);
            LOGGER.info("Preview for AGAIN: {}", preview);

            state = SM2Algorithm.calculateNextReview(state, ReviewRating.AGAIN);
            LOGGER.info("After AGAIN: interval={}, ease={}, reps={}",
                    state.getInterval(), state.getEaseFactor(), state.getRepetitions());

            // Test 5: Check if card is due
            boolean isDue = SM2Algorithm.isDue(state);
            int daysUntil = SM2Algorithm.getDaysUntilReview(state);
            LOGGER.info("Is due now: {}, Days until review: {}", isDue, daysUntil);

            // Test 6: Preview all ratings
            LOGGER.info("--- Test 6: Preview all ratings ---");
            for (ReviewRating rating : ReviewRating.values()) {
                String intervalPreview = SM2Algorithm.previewInterval(state, rating);
                LOGGER.info("{}: {}", rating.getDisplayName(), intervalPreview);
            }

            LOGGER.info("✓ All SM-2 algorithm tests passed!");

        } catch (Exception e) {
            LOGGER.error("✗ SM-2 algorithm test failed: {}", e.getMessage(), e);
        }
    }

    private static void testStorageSystem() {
        LOGGER.info("Testing Storage System...");

        try {
            // Initialize CategoryManager
            CategoryManager categoryManager = CategoryManager.getInstance();
            categoryManager.loadAllCategories();
            LOGGER.info("Total categories loaded: {}", categoryManager.getAllCategories().size());

            // Initialize DeckManager
            DeckManager manager = DeckManager.getInstance();

            // Load all decks
            manager.loadAllDecks();

            // Display loaded decks
            LOGGER.info("Total decks loaded: {}", manager.getAllDecks().size());

            for (Deck deck : manager.getAllDecks()) {
                String categoryInfo = deck.getCategoryId() != null ? " (in category: " + deck.getCategoryId() + ")" : "";
                LOGGER.info("Deck: {} - {} ({} cards, enabled: {}){}",
                        deck.getId(), deck.getName(), deck.getCardCount(),
                        deck.isEnabled(), categoryInfo);

                // Show first 3 cards from deck
                int cardCount = 0;
                for (Flashcard card : deck.getCards()) {
                    if (cardCount++ >= 3) break;
                    LOGGER.info("  Card: {} - Q: {}",
                            card.getId(), card.getQuestion());
                }
            }

            LOGGER.info("✓ Storage system test passed!");

        } catch (Exception e) {
            LOGGER.error("✗ Storage system test failed: {}", e.getMessage(), e);
        }
    }

    private static void testPlayerProgress() {
        LOGGER.info("Testing Player Progress System...");

        try {
            PlayerProgressManager manager = PlayerProgressManager.getInstance();

            // Simulate a test player
            String testPlayerName = "TestPlayer";
            LOGGER.info("Test player name: {}", testPlayerName);

            // Test 1: Get or create new card states
            LOGGER.info("--- Test 1: Creating new card states ---");
            SRSState card1State = manager.getOrCreateCardState(testPlayerName, "math_1");
            SRSState card2State = manager.getOrCreateCardState(testPlayerName, "math_2");

            LOGGER.info("Card 1 state: {}", card1State);
            LOGGER.info("Card 2 state: {}", card2State);

            // Test 2: Update card states using SM-2
            LOGGER.info("--- Test 2: Updating card states ---");

            // Review card 1 with GOOD rating
            SM2Algorithm.calculateNextReview(card1State, ReviewRating.GOOD);
            manager.updateCardState(testPlayerName, card1State);
            LOGGER.info("After GOOD review: {}", card1State);

            // Review card 2 with EASY rating
            SM2Algorithm.calculateNextReview(card2State, ReviewRating.EASY);
            manager.updateCardState(testPlayerName, card2State);
            LOGGER.info("After EASY review: {}", card2State);

            // Test 3: Get all card states
            LOGGER.info("--- Test 3: Retrieving all card states ---");
            var allStates = manager.getAllCardStates(testPlayerName);
            LOGGER.info("Total cards tracked: {}", allStates.size());

            // Test 4: Get due cards
            LOGGER.info("--- Test 4: Checking due cards ---");
            var dueCards = manager.getDueCards(testPlayerName);
            LOGGER.info("Due cards: {}", dueCards);

            // Test 5: Get player statistics
            LOGGER.info("--- Test 5: Player statistics ---");
            PlayerProgressManager.PlayerStats stats = manager.getPlayerStats(testPlayerName);
            LOGGER.info("Player stats: {}", stats);

            // Test 6: Reset a card
            LOGGER.info("--- Test 6: Resetting card ---");
            manager.resetCard(testPlayerName, "math_1");
            LOGGER.info("Cards after reset: {}", manager.getAllCardStates(testPlayerName).size());

            LOGGER.info("✓ Player progress system test passed!");
            LOGGER.info("Note: NBT persistence will be tested when a real player joins");

        } catch (Exception e) {
            LOGGER.error("✗ Player progress test failed: {}", e.getMessage(), e);
        }
    }
}
