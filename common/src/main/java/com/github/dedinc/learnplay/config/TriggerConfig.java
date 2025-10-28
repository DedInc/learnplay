package com.github.dedinc.learnplay.config;

import com.google.gson.JsonObject;

/**
 * Configuration for trigger system.
 * Controls when flashcard reviews are triggered in-game.
 */
public class TriggerConfig {

    // Trigger enable/disable flags
    public boolean enableDeathTrigger = true;
    public boolean enableTimerTrigger = false;
    public boolean enableAdvancementTrigger = false;
    public boolean enableBlockBreakTrigger = false;
    public boolean enableBlockPlaceTrigger = false;
    public boolean enableEntityKillTrigger = false;
    public boolean enableChatTrigger = false;

    // Timer trigger settings
    public int timerIntervalMinutes = 15; // Trigger review every N minutes

    // Death trigger settings
    public int deathTriggerEveryNDeaths = 2; // Trigger every N deaths (2 = every 2 deaths for testing)

    // Advancement trigger settings
    public int advancementTriggerCooldownSeconds = 60;

    // Block break trigger settings
    public int blockBreakTriggerThreshold = 100; // Trigger after breaking N blocks
    public String blockBreakWhitelist = "stone,dirt,oak_log,iron_ore,diamond_ore"; // Comma-separated list of block types to track (empty = all blocks)

    // Block place trigger settings
    public int blockPlaceTriggerThreshold = 50; // Trigger after placing N blocks
    public String blockPlaceWhitelist = ""; // Comma-separated list of block types to track (empty = all blocks)

    // Entity kill trigger settings
    public int entityKillTriggerThreshold = 10; // Trigger after killing N entities
    public String entityKillWhitelist = "zombie,skeleton,creeper,spider,enderman"; // Comma-separated list of entity types to track (empty = all entities)

    // Chat trigger settings
    public String chatTriggerPattern = "edit"; // Pattern to match in chat messages (case-insensitive contains)
    public int chatTriggerThreshold = 10; // Trigger every N chat matches

    // Global cooldown (minimum time between ANY triggers)
    public int globalCooldownSeconds = 10;

    /**
     * Convert to JSON.
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        // Enable flags
        json.addProperty("enableDeathTrigger", enableDeathTrigger);
        json.addProperty("enableTimerTrigger", enableTimerTrigger);
        json.addProperty("enableAdvancementTrigger", enableAdvancementTrigger);
        json.addProperty("enableBlockBreakTrigger", enableBlockBreakTrigger);
        json.addProperty("enableBlockPlaceTrigger", enableBlockPlaceTrigger);
        json.addProperty("enableEntityKillTrigger", enableEntityKillTrigger);
        json.addProperty("enableChatTrigger", enableChatTrigger);

        // Timer settings
        json.addProperty("timerIntervalMinutes", timerIntervalMinutes);

        // Death settings
        json.addProperty("deathTriggerEveryNDeaths", deathTriggerEveryNDeaths);

        // Advancement settings
        json.addProperty("advancementTriggerCooldownSeconds", advancementTriggerCooldownSeconds);

        // Block break settings
        json.addProperty("blockBreakTriggerThreshold", blockBreakTriggerThreshold);
        json.addProperty("blockBreakWhitelist", blockBreakWhitelist);

        // Block place settings
        json.addProperty("blockPlaceTriggerThreshold", blockPlaceTriggerThreshold);
        json.addProperty("blockPlaceWhitelist", blockPlaceWhitelist);

        // Entity kill settings
        json.addProperty("entityKillTriggerThreshold", entityKillTriggerThreshold);
        json.addProperty("entityKillWhitelist", entityKillWhitelist);

        // Chat settings
        json.addProperty("chatTriggerPattern", chatTriggerPattern);
        json.addProperty("chatTriggerThreshold", chatTriggerThreshold);

        // Global cooldown
        json.addProperty("globalCooldownSeconds", globalCooldownSeconds);

        return json;
    }

    /**
     * Load from JSON.
     */
    public void fromJson(JsonObject json) {
        // Enable flags
        if (json.has("enableDeathTrigger"))
            enableDeathTrigger = json.get("enableDeathTrigger").getAsBoolean();
        if (json.has("enableTimerTrigger"))
            enableTimerTrigger = json.get("enableTimerTrigger").getAsBoolean();
        if (json.has("enableAdvancementTrigger"))
            enableAdvancementTrigger = json.get("enableAdvancementTrigger").getAsBoolean();
        if (json.has("enableBlockBreakTrigger"))
            enableBlockBreakTrigger = json.get("enableBlockBreakTrigger").getAsBoolean();
        if (json.has("enableBlockPlaceTrigger"))
            enableBlockPlaceTrigger = json.get("enableBlockPlaceTrigger").getAsBoolean();
        // Backward compatibility: support old "enableMobKillTrigger" name
        if (json.has("enableMobKillTrigger"))
            enableEntityKillTrigger = json.get("enableMobKillTrigger").getAsBoolean();
        if (json.has("enableEntityKillTrigger"))
            enableEntityKillTrigger = json.get("enableEntityKillTrigger").getAsBoolean();
        if (json.has("enableChatTrigger"))
            enableChatTrigger = json.get("enableChatTrigger").getAsBoolean();

        // Timer settings
        if (json.has("timerIntervalMinutes"))
            timerIntervalMinutes = json.get("timerIntervalMinutes").getAsInt();

        // Death settings
        // Ignore old deathTriggerCooldownSeconds for backward compatibility
        if (json.has("deathTriggerEveryNDeaths"))
            deathTriggerEveryNDeaths = json.get("deathTriggerEveryNDeaths").getAsInt();

        // Advancement settings
        if (json.has("advancementTriggerCooldownSeconds"))
            advancementTriggerCooldownSeconds = json.get("advancementTriggerCooldownSeconds").getAsInt();

        // Block break settings
        if (json.has("blockBreakTriggerThreshold"))
            blockBreakTriggerThreshold = json.get("blockBreakTriggerThreshold").getAsInt();
        if (json.has("blockBreakWhitelist"))
            blockBreakWhitelist = json.get("blockBreakWhitelist").getAsString();
        // Backward compatibility: ignore old cooldown setting
        // if (json.has("blockBreakTriggerCooldownSeconds")) - removed, block break uses threshold only

        // Block place settings
        if (json.has("blockPlaceTriggerThreshold"))
            blockPlaceTriggerThreshold = json.get("blockPlaceTriggerThreshold").getAsInt();
        if (json.has("blockPlaceWhitelist"))
            blockPlaceWhitelist = json.get("blockPlaceWhitelist").getAsString();

        // Entity kill settings (backward compatibility with old "mobKill" names)
        if (json.has("mobKillTriggerThreshold"))
            entityKillTriggerThreshold = json.get("mobKillTriggerThreshold").getAsInt();
        if (json.has("entityKillTriggerThreshold"))
            entityKillTriggerThreshold = json.get("entityKillTriggerThreshold").getAsInt();
        // Note: entityKillTriggerCooldownSeconds removed - entity kill trigger doesn't use cooldowns
        if (json.has("entityKillWhitelist"))
            entityKillWhitelist = json.get("entityKillWhitelist").getAsString();
        if (json.has("mobKillWhitelist"))
            entityKillWhitelist = json.get("mobKillWhitelist").getAsString();

        // Chat settings
        if (json.has("chatTriggerPattern"))
            chatTriggerPattern = json.get("chatTriggerPattern").getAsString();
        // Ignore old chatTriggerCooldownSeconds for backward compatibility
        if (json.has("chatTriggerThreshold"))
            chatTriggerThreshold = json.get("chatTriggerThreshold").getAsInt();

        // Global cooldown
        if (json.has("globalCooldownSeconds"))
            globalCooldownSeconds = json.get("globalCooldownSeconds").getAsInt();
    }

    /**
     * Check if trigger type is enabled.
     */
    public boolean isTriggerEnabled(TriggerType type) {
        return switch (type) {
            case DEATH -> enableDeathTrigger;
            case TIMER -> enableTimerTrigger;
            case ADVANCEMENT -> enableAdvancementTrigger;
            case BLOCK_BREAK -> enableBlockBreakTrigger;
            case BLOCK_PLACE -> enableBlockPlaceTrigger;
            case ENTITY_KILL -> enableEntityKillTrigger;
            case CHAT -> enableChatTrigger;
        };
    }

    /**
     * Get cooldown for trigger type (in milliseconds).
     */
    public long getCooldownMillis(TriggerType type) {
        int seconds = switch (type) {
            case DEATH -> 0; // Death trigger uses threshold counting only
            case ADVANCEMENT -> advancementTriggerCooldownSeconds;
            case BLOCK_BREAK -> 0; // Block break trigger doesn't use cooldowns (uses threshold counting only)
            case BLOCK_PLACE -> 0; // Block place trigger doesn't use cooldowns (uses threshold counting only)
            case ENTITY_KILL -> 0; // Entity kill trigger doesn't use cooldowns (uses threshold counting only)
            case CHAT -> 0; // Chat trigger uses threshold counting only
            case TIMER -> 0; // Timer has its own interval
        };
        return seconds * 1000L;
    }

    /**
     * Trigger types enum.
     */
    public enum TriggerType {
        DEATH,
        TIMER,
        ADVANCEMENT,
        BLOCK_BREAK,
        BLOCK_PLACE,
        ENTITY_KILL,
        CHAT
    }
}
