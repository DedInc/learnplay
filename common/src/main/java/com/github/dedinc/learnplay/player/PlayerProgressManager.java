package com.github.dedinc.learnplay.player;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.SRSState;
import com.google.gson.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages player learning progress in memory and persists to disk.
 * Stores SRS state for each card per player.
 * <p>
 * Progress is saved to: config/learnplay/progress/<playerName>.json
 * Uses player name instead of UUID to maintain progress across sessions.
 */
public class PlayerProgressManager {

    private static final String PROGRESS_PATH = "config/learnplay/progress/";

    // Singleton instance
    private static PlayerProgressManager instance;

    // Runtime cache: playerName -> (cardId -> SRSState)
    private final Map<String, Map<String, SRSState>> progressCache = new HashMap<>();

    private PlayerProgressManager() {
        // Private constructor for singleton
        ensureProgressDirectoryExists();
    }

    public static PlayerProgressManager getInstance() {
        if (instance == null) {
            instance = new PlayerProgressManager();
        }
        return instance;
    }

    /**
     * Ensure the progress directory exists.
     */
    private void ensureProgressDirectoryExists() {
        try {
            Path progressDir = Paths.get(PROGRESS_PATH);
            if (!Files.exists(progressDir)) {
                Files.createDirectories(progressDir);
                LearnPlay.LOGGER.info("Created progress directory: {}", progressDir.toAbsolutePath());
            }
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to create progress directory", e);
        }
    }

    /**
     * Initialize player progress (called when player joins).
     * Loads progress from disk if it exists.
     */
    public void initializePlayer(String playerName) {
        if (!progressCache.containsKey(playerName)) {
            loadPlayerProgress(playerName);
            LearnPlay.LOGGER.info("Initialized player progress: {}", playerName);
        }
    }

    /**
     * Load player progress from disk.
     */
    private void loadPlayerProgress(String playerName) {
        Path progressFile = Paths.get(PROGRESS_PATH + playerName + ".json");

        try {
            if (Files.exists(progressFile)) {
                String jsonData = Files.readString(progressFile, StandardCharsets.UTF_8);
                loadPlayerProgressFromJson(playerName, jsonData);
                LearnPlay.LOGGER.info("Loaded progress from disk for player {}", playerName);
            } else {
                progressCache.put(playerName, new HashMap<>());
                LearnPlay.LOGGER.info("No existing progress found for player {}", playerName);
            }
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to load progress for player {}", playerName, e);
            progressCache.put(playerName, new HashMap<>());
        }
    }

    /**
     * Save player progress to disk.
     */
    private void savePlayerProgress(String playerName) {
        Path progressFile = Paths.get(PROGRESS_PATH + playerName + ".json");

        try {
            ensureProgressDirectoryExists();
            String jsonData = savePlayerProgressToJson(playerName);
            Files.writeString(progressFile, jsonData, StandardCharsets.UTF_8);
            LearnPlay.LOGGER.debug("Saved progress to disk for player {}", playerName);
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to save progress for player {}", playerName, e);
        }
    }

    /**
     * Load player progress from JSON string.
     */
    public void loadPlayerProgressFromJson(String playerName, String jsonData) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            Map<String, SRSState> cardStates = new HashMap<>();

            if (json.has("cards") && json.get("cards").isJsonArray()) {
                JsonArray cardsArray = json.getAsJsonArray("cards");

                for (JsonElement element : cardsArray) {
                    if (element.isJsonObject()) {
                        SRSState state = deserializeSRSState(element.getAsJsonObject());
                        if (state != null) {
                            cardStates.put(state.getCardId(), state);
                        }
                    }
                }
            }

            progressCache.put(playerName, cardStates);
            LearnPlay.LOGGER.info("Loaded progress for {}: {} cards", playerName, cardStates.size());

        } catch (Exception e) {
            LearnPlay.LOGGER.error("Failed to load player progress from JSON", e);
            progressCache.put(playerName, new HashMap<>());
        }
    }

    /**
     * Save player progress to JSON string.
     */
    public String savePlayerProgressToJson(String playerName) {
        Map<String, SRSState> cardStates = progressCache.get(playerName);

        if (cardStates == null || cardStates.isEmpty()) {
            return "{}";
        }

        JsonObject json = new JsonObject();
        JsonArray cardsArray = new JsonArray();

        for (SRSState state : cardStates.values()) {
            cardsArray.add(serializeSRSState(state));
        }

        json.add("cards", cardsArray);

        // Pretty print for readability
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    /**
     * Get SRS state for a specific card.
     */
    public SRSState getCardState(String playerName, String cardId) {
        Map<String, SRSState> playerProgress = progressCache.get(playerName);

        if (playerProgress == null) {
            return null;
        }

        return playerProgress.get(cardId);
    }

    /**
     * Get or create SRS state for a card (creates new state if doesn't exist).
     */
    public SRSState getOrCreateCardState(String playerName, String cardId) {
        Map<String, SRSState> playerProgress = progressCache.computeIfAbsent(playerName, k -> new HashMap<>());

        return playerProgress.computeIfAbsent(cardId, k -> {
            LearnPlay.LOGGER.debug("Created new SRS state for player {} card {}", playerName, cardId);
            return new SRSState(cardId);
        });
    }

    /**
     * Update SRS state for a card.
     */
    public void updateCardState(String playerName, SRSState state) {
        Map<String, SRSState> playerProgress = progressCache.computeIfAbsent(playerName, k -> new HashMap<>());
        playerProgress.put(state.getCardId(), state);

        // Trigger auto-save
        autoSave(playerName);
    }

    /**
     * Auto-save player progress to disk.
     * This is called automatically when progress changes.
     */
    protected void autoSave(String playerName) {
        savePlayerProgress(playerName);
    }

    /**
     * Get all card states for a player.
     */
    public Map<String, SRSState> getAllCardStates(String playerName) {
        Map<String, SRSState> playerProgress = progressCache.get(playerName);

        if (playerProgress == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(playerProgress);
    }

    /**
     * Get cards that are due for review.
     */
    public List<String> getDueCards(String playerName) {
        Map<String, SRSState> playerProgress = progressCache.get(playerName);

        if (playerProgress == null) {
            return Collections.emptyList();
        }

        List<String> dueCards = new ArrayList<>();
        for (SRSState state : playerProgress.values()) {
            if (state.isDue()) {
                dueCards.add(state.getCardId());
            }
        }

        return dueCards;
    }

    /**
     * Get new cards (never reviewed).
     */
    public List<String> getNewCards(String playerName, Collection<String> allCardIds) {
        Map<String, SRSState> playerProgress = progressCache.get(playerName);

        List<String> newCards = new ArrayList<>();
        for (String cardId : allCardIds) {
            if (playerProgress == null || !playerProgress.containsKey(cardId)) {
                newCards.add(cardId);
            }
        }

        return newCards;
    }

    /**
     * Reset progress for a specific card.
     */
    public void resetCard(String playerName, String cardId) {
        Map<String, SRSState> playerProgress = progressCache.get(playerName);

        if (playerProgress != null) {
            playerProgress.remove(cardId);
            LearnPlay.LOGGER.info("Reset card {} for player {}", cardId, playerName);
        }
    }

    /**
     * Clear all progress for a player.
     */
    public void clearPlayerProgress(String playerName) {
        progressCache.remove(playerName);
        LearnPlay.LOGGER.info("Cleared all progress for player {}", playerName);
    }

    /**
     * Serialize SRSState to JSON.
     */
    private JsonObject serializeSRSState(SRSState state) {
        JsonObject json = new JsonObject();
        json.addProperty("cardId", state.getCardId());
        json.addProperty("interval", state.getInterval());
        json.addProperty("easeFactor", state.getEaseFactor());
        json.addProperty("repetitions", state.getRepetitions());
        json.addProperty("lastReview", state.getLastReview());
        json.addProperty("nextReview", state.getNextReview());
        return json;
    }

    /**
     * Deserialize SRSState from JSON.
     */
    private SRSState deserializeSRSState(JsonObject json) {
        try {
            String cardId = json.get("cardId").getAsString();
            int interval = json.get("interval").getAsInt();
            double easeFactor = json.get("easeFactor").getAsDouble();
            int repetitions = json.get("repetitions").getAsInt();
            long lastReview = json.get("lastReview").getAsLong();
            long nextReview = json.get("nextReview").getAsLong();

            return new SRSState(cardId, interval, easeFactor, repetitions, lastReview, nextReview);
        } catch (Exception e) {
            LearnPlay.LOGGER.error("Failed to deserialize SRS state from JSON", e);
            return null;
        }
    }

    /**
     * Get statistics for a player.
     */
    public PlayerStats getPlayerStats(String playerName) {
        Map<String, SRSState> playerProgress = progressCache.get(playerName);

        if (playerProgress == null || playerProgress.isEmpty()) {
            return new PlayerStats(0, 0, 0, 0, 0, 0);
        }

        int totalCards = playerProgress.size();
        int reviseCards = 0;  // Cards ready to be reviewed (due)
        int learnCards = 0;   // Cards not yet learned (new)
        int weakCards = 0;    // Level < 3 (repetitions 0-2)
        int middleCards = 0;  // Level < 5 (repetitions 3-4)
        int strongCards = 0;  // Level >= 5 (repetitions 5+)

        for (SRSState state : playerProgress.values()) {
            // Count Learn cards (not yet learned)
            if (state.isNew()) {
                learnCards++;
            }

            // Count Revise cards (ready to be reviewed)
            if (state.isDue()) {
                reviseCards++;
            }

            // Count by strength level based on repetitions
            int repetitions = state.getRepetitions();
            if (repetitions < 3) {
                weakCards++;
            } else if (repetitions < 5) {
                middleCards++;
            } else {
                strongCards++;
            }
        }

        return new PlayerStats(totalCards, reviseCards, learnCards, weakCards, middleCards, strongCards);
    }

    /**
     * Player statistics data class.
     * <p>
     * Terminology:
     * - Learn: Cards not yet learned (new cards)
     * - Revise: Cards ready to be reviewed (due cards)
     * - Weak: Cards with level < 3 (repetitions 0-2: 10min, 20min, 1day)
     * - Middle: Cards with level < 5 (repetitions 3-4: 3days, 7days)
     * - Strong: Cards with level >= 5 (repetitions 5+: 14days, 30days, 60days, 120days, 240days)
     */
    public static class PlayerStats {
        public final int totalCards;
        public final int reviseCards;
        public final int learnCards;
        public final int weakCards;
        public final int middleCards;
        public final int strongCards;

        public PlayerStats(int totalCards, int reviseCards, int learnCards, int weakCards, int middleCards, int strongCards) {
            this.totalCards = totalCards;
            this.reviseCards = reviseCards;
            this.learnCards = learnCards;
            this.weakCards = weakCards;
            this.middleCards = middleCards;
            this.strongCards = strongCards;
        }

        @Override
        public String toString() {
            return String.format("Stats{total=%d, revise=%d, learn=%d, weak=%d, middle=%d, strong=%d}",
                    totalCards, reviseCards, learnCards, weakCards, middleCards, strongCards);
        }
    }
}

