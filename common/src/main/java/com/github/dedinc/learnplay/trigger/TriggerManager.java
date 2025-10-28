package com.github.dedinc.learnplay.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.config.LearnPlayConfig;
import com.github.dedinc.learnplay.config.TriggerConfig;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.srs.ReviewScheduler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages trigger events for flashcard reviews.
 * Handles cooldowns, trigger logic, and coordination between different trigger types.
 * <p>
 * This is platform-agnostic - the common module defines the logic,
 * platform-specific modules register the actual event listeners.
 */
public class TriggerManager {
    private static TriggerManager instance;

    private final LearnPlayConfig config;
    private final ReviewScheduler scheduler;

    // Cooldown tracking: playerName -> (triggerType -> lastTriggerTime)
    private final Map<String, Map<TriggerConfig.TriggerType, Long>> cooldowns = new HashMap<>();

    // Global cooldown tracking: playerName -> lastAnyTriggerTime
    private final Map<String, Long> globalCooldowns = new HashMap<>();

    // Timer tracking: playerName -> lastTimerTriggerTime
    private final Map<String, Long> timerTracking = new HashMap<>();

    // Counter tracking for threshold-based triggers
    private final Map<String, Map<TriggerConfig.TriggerType, Integer>> counters = new HashMap<>();

    private TriggerManager() {
        this.config = LearnPlayConfig.getInstance();
        this.scheduler = new ReviewScheduler();
    }

    public static TriggerManager getInstance() {
        if (instance == null) {
            instance = new TriggerManager();
        }
        return instance;
    }

    /**
     * Check if a trigger can fire for a player.
     * Checks both trigger-specific cooldown and global cooldown.
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
                LearnPlay.LOGGER.debug("Global cooldown active for player {} ({}ms remaining)",
                        playerName, globalCooldownMillis - (currentTime - lastGlobalTrigger));
                return false;
            }
        }

        // Check trigger-specific cooldown
        Map<TriggerConfig.TriggerType, Long> playerCooldowns = cooldowns.get(playerName);
        if (playerCooldowns != null && playerCooldowns.containsKey(triggerType)) {
            long lastTrigger = playerCooldowns.get(triggerType);
            long cooldownMillis = config.triggers.getCooldownMillis(triggerType);
            if (currentTime - lastTrigger < cooldownMillis) {
                LearnPlay.LOGGER.debug("Trigger cooldown active for player {} type {} ({}ms remaining)",
                        playerName, triggerType, cooldownMillis - (currentTime - lastTrigger));
                return false;
            }
        }

        return true;
    }

    /**
     * Trigger a review for a player.
     * This is called by platform-specific trigger handlers.
     *
     * @param player      The player to trigger review for
     * @param triggerType The type of trigger that fired
     * @return true if review was triggered, false otherwise
     */
    public boolean triggerReview(ServerPlayerEntity player, TriggerConfig.TriggerType triggerType) {
        String playerName = player.getName().getString();

        if (!canTrigger(playerName, triggerType)) {
            return false;
        }

        // Get next card for review
        Flashcard card = scheduler.getNextCardForReview(playerName);

        if (card == null) {
            LearnPlay.LOGGER.info("Trigger {} fired for player {}, but no cards available",
                    triggerType, playerName);
            return false;
        }

        // Update cooldowns
        recordTrigger(playerName, triggerType);

        // Log the trigger
        LearnPlay.LOGGER.info("Trigger {} fired for player {} - opening review for card {}",
                triggerType, playerName, card.getId());

        // Platform-specific code will handle actually opening the screen
        // We'll use a callback system for this
        onTriggerFired(player, card, triggerType);

        return true;
    }

    /**
     * Record that a trigger has fired.
     * Updates both trigger-specific and global cooldowns.
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
     * Increment a counter for threshold-based triggers.
     * Returns true if threshold is reached.
     */
    public boolean incrementCounter(String playerName, TriggerConfig.TriggerType triggerType, int threshold) {
        Map<TriggerConfig.TriggerType, Integer> playerCounters =
                counters.computeIfAbsent(playerName, k -> new HashMap<>());

        int count = playerCounters.getOrDefault(triggerType, 0) + 1;
        playerCounters.put(triggerType, count);

        if (count >= threshold) {
            // Reset counter
            playerCounters.put(triggerType, 0);
            return true;
        }

        return false;
    }

    /**
     * Check if timer trigger should fire for a player.
     * Updates timer tracking internally.
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
     * Platform-specific callback when a trigger fires.
     * Platform modules will override this to open the review screen.
     */
    protected void onTriggerFired(ServerPlayerEntity player, Flashcard card, TriggerConfig.TriggerType triggerType) {
        // Platform-specific implementations will handle this
        // For now, we just log it
        LearnPlay.LOGGER.info("Trigger fired but no platform handler registered");
    }

    /**
     * Clear all cooldowns for a player (e.g., when they disconnect).
     */
    public void clearPlayerData(String playerName) {
        cooldowns.remove(playerName);
        globalCooldowns.remove(playerName);
        timerTracking.remove(playerName);
        counters.remove(playerName);
    }
}
