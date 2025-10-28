package com.github.dedinc.learnplay.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.client.gui.NoCardsAvailableScreen;
import com.github.dedinc.learnplay.client.gui.ReviewScreen;
import com.github.dedinc.learnplay.config.LearnPlayConfig;
import com.github.dedinc.learnplay.config.TriggerConfig;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import com.github.dedinc.learnplay.player.PlayerProgressManager;
import com.github.dedinc.learnplay.srs.ReviewScheduler;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side trigger manager that directly opens review screens.
 * This works for singleplayer and client-side without networking.
 * <p>
 * Tracks:
 * - Cooldowns (per-trigger and global)
 * - Counters for "every N times" triggers (deaths, kills, blocks)
 * - Timer intervals
 */
public class ClientTriggerManager {
    private static ClientTriggerManager instance;

    private final LearnPlayConfig config;
    private final ReviewScheduler scheduler;
    private final PlayerProgressManager progressManager;

    // Cooldown tracking: playerName -> (triggerType -> lastTriggerTime)
    private final Map<String, Map<TriggerConfig.TriggerType, Long>> cooldowns = new HashMap<>();

    // Global cooldown tracking: playerName -> lastAnyTriggerTime
    private final Map<String, Long> globalCooldowns = new HashMap<>();

    // Timer tracking: playerName -> lastTimerTriggerTime
    private final Map<String, Long> timerTracking = new HashMap<>();

    // Counter tracking for "every N times" triggers
    private final Map<String, Map<TriggerConfig.TriggerType, Integer>> counters = new HashMap<>();

    private ClientTriggerManager() {
        this.config = LearnPlayConfig.getInstance();
        this.scheduler = new ReviewScheduler();
        this.progressManager = PlayerProgressManager.getInstance();
    }

    public static ClientTriggerManager getInstance() {
        if (instance == null) {
            instance = new ClientTriggerManager();
        }
        return instance;
    }

    /**
     * Check if a trigger can fire for the current player.
     */
    public boolean canTrigger(String playerName, TriggerConfig.TriggerType triggerType) {
        long currentTime = System.currentTimeMillis();

        // Check if trigger is enabled in config
        if (!config.triggers.isTriggerEnabled(triggerType)) {
            return false;
        }

        // Check global cooldown
        if (globalCooldowns.containsKey(playerName)) {
            long lastGlobalTrigger = globalCooldowns.get(playerName);
            long globalCooldownMillis = config.triggers.globalCooldownSeconds * 1000L;
            if (currentTime - lastGlobalTrigger < globalCooldownMillis) {
                return false;
            }
        }

        // Check trigger-specific cooldown
        Map<TriggerConfig.TriggerType, Long> playerCooldowns = cooldowns.get(playerName);
        if (playerCooldowns != null && playerCooldowns.containsKey(triggerType)) {
            long lastTrigger = playerCooldowns.get(triggerType);
            long cooldownMillis = config.triggers.getCooldownMillis(triggerType);
            if (currentTime - lastTrigger < cooldownMillis) {
                return false;
            }
        }

        return true;
    }

    /**
     * Attempt to trigger a review.
     * Handles counters for "every N times" triggers.
     *
     * @return true if review was triggered, false if counter not reached or cooldown active
     */
    public boolean attemptTrigger(TriggerConfig.TriggerType triggerType) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        String playerName = client.player.getName().getString();

        // Check if we need to count (for "every N times" triggers)
        int threshold = getThreshold(triggerType);
        if (threshold > 1) {
            // Increment counter
            Map<TriggerConfig.TriggerType, Integer> playerCounters =
                    counters.computeIfAbsent(playerName, k -> new HashMap<>());

            int count = playerCounters.getOrDefault(triggerType, 0) + 1;
            playerCounters.put(triggerType, count);

            LearnPlay.LOGGER.info("[TRIGGER] {} counter: {}/{}", triggerType, count, threshold);

            // Check if threshold reached
            if (count < threshold) {
                LearnPlay.LOGGER.info("[TRIGGER] {} threshold not reached yet, waiting for {} more",
                        triggerType, threshold - count);
                return false; // Not yet, keep counting
            }

            // Reset counter
            playerCounters.put(triggerType, 0);
            LearnPlay.LOGGER.info("[TRIGGER] ✅ {} threshold reached! Attempting to open review...", triggerType);
        }

        // Check cooldowns
        if (!canTrigger(playerName, triggerType)) {
            LearnPlay.LOGGER.info("[TRIGGER] {} cooldown still active, skipping", triggerType);
            return false;
        }

        // Trigger the review
        return triggerReview(playerName, triggerType);
    }

    /**
     * Get the threshold for a trigger type (how many times before it fires).
     */
    private int getThreshold(TriggerConfig.TriggerType triggerType) {
        return switch (triggerType) {
            case DEATH -> config.triggers.deathTriggerEveryNDeaths;
            case ENTITY_KILL -> config.triggers.entityKillTriggerThreshold;
            case BLOCK_BREAK -> config.triggers.blockBreakTriggerThreshold;
            case BLOCK_PLACE -> config.triggers.blockPlaceTriggerThreshold;
            case CHAT -> config.triggers.chatTriggerThreshold;
            default -> 1; // TIMER and ADVANCEMENT trigger every time
        };
    }

    /**
     * Actually trigger the review - get card and open screen.
     */
    private boolean triggerReview(String playerName, TriggerConfig.TriggerType triggerType) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Get next card for review
        Flashcard card = scheduler.getNextCardForReview(playerName);

        if (card == null) {
            // No cards available - show info screen
            ReviewScheduler.ReviewStats stats = scheduler.getReviewStats(playerName);
            LearnPlay.LOGGER.info("Trigger {} fired, but no cards available", triggerType);
            client.execute(() -> client.setScreen(new NoCardsAvailableScreen(stats)));
            return false;
        }

        // Get or create SRS state
        SRSState state = progressManager.getOrCreateCardState(playerName, card.getId());

        // Update cooldowns
        recordTrigger(playerName, triggerType);

        // Log the trigger
        ReviewScheduler.ReviewStats stats = scheduler.getReviewStats(playerName);
        LearnPlay.LOGGER.info("✨ Trigger {} activated! Opening review for card: {} | {}",
                triggerType, card.getId(), stats);

        // Open review screen on main thread
        client.execute(() -> client.setScreen(new ReviewScreen(card, state, playerName)));

        return true;
    }

    /**
     * Record that a trigger has fired (for cooldown tracking).
     */
    private void recordTrigger(String playerName, TriggerConfig.TriggerType triggerType) {
        long currentTime = System.currentTimeMillis();

        // Record trigger-specific cooldown
        cooldowns.computeIfAbsent(playerName, k -> new HashMap<>())
                .put(triggerType, currentTime);

        // Record global cooldown
        globalCooldowns.put(playerName, currentTime);
    }

    /**
     * Check if timer trigger should fire for the current player.
     */
    public boolean shouldTimerTrigger(String playerName) {
        if (!config.triggers.isTriggerEnabled(TriggerConfig.TriggerType.TIMER)) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long lastTrigger = timerTracking.getOrDefault(playerName, 0L);
        long intervalMillis = config.triggers.timerIntervalMinutes * 60L * 1000L;

        if (currentTime - lastTrigger >= intervalMillis) {
            timerTracking.put(playerName, currentTime);
            return true;
        }

        return false;
    }

    /**
     * Clear all data for a player.
     */
    public void clearPlayerData(String playerName) {
        cooldowns.remove(playerName);
        globalCooldowns.remove(playerName);
        timerTracking.remove(playerName);
        counters.remove(playerName);
    }
}
