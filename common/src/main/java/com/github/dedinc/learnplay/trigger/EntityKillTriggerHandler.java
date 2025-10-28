package com.github.dedinc.learnplay.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.config.LearnPlayConfig;
import com.github.dedinc.learnplay.config.TriggerConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity kill trigger handler that tracks entity kills using death events.
 * Supports filtering by entity type through an exact-match whitelist.
 * <p>
 * Platform-specific implementations should call {@link #onEntityKilled(LivingEntity, DamageSource)}
 * when an entity is killed by a player.
 */
public class EntityKillTriggerHandler {

    private static final Set<String> whitelistedEntityIds = new HashSet<>();
    private static String lastWhitelistConfig = "";

    /**
     * Called when an entity is killed. Platform-specific implementations should call this.
     *
     * @param killedEntity The entity that was killed
     * @param source       The damage source (used to check if player killed it)
     */
    public static void onEntityKilled(LivingEntity killedEntity, DamageSource source) {
        // Check if trigger is enabled
        LearnPlayConfig config = LearnPlayConfig.getInstance();
        if (!config.triggers.enableEntityKillTrigger) {
            return;
        }

        // Check if killed by a player
        if (source == null || !(source.getAttacker() instanceof PlayerEntity)) {
            return;
        }

        // Update whitelist if config changed
        String currentWhitelist = config.triggers.entityKillWhitelist;
        if (!currentWhitelist.equals(lastWhitelistConfig)) {
            updateWhitelist(currentWhitelist);
            lastWhitelistConfig = currentWhitelist;
        }

        // Get entity ID
        EntityType<?> entityType = killedEntity.getType();
        Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
        if (entityId == null) {
            return;
        }

        String entityPath = entityId.getPath();

        // Check if entity is whitelisted (or whitelist is empty = all entities)
        if (!whitelistedEntityIds.isEmpty() && !whitelistedEntityIds.contains(entityPath)) {
            return; // Not in whitelist, ignore
        }

        LearnPlay.LOGGER.info("[ENTITY KILL TRIGGER] Player killed: {}", entityPath);

        // Trigger the flashcard review
        ClientTriggerManager triggerManager = ClientTriggerManager.getInstance();
        triggerManager.attemptTrigger(TriggerConfig.TriggerType.ENTITY_KILL);
    }

    /**
     * Update the entity whitelist from config string.
     * Uses EXACT matching: "pig" matches only "pig", not "piglin".
     */
    private static void updateWhitelist(String whitelistConfig) {
        whitelistedEntityIds.clear();

        if (whitelistConfig == null || whitelistConfig.trim().isEmpty()) {
            // Empty whitelist = track all entities
            LearnPlay.LOGGER.info("[ENTITY KILL TRIGGER] Whitelist empty - tracking all entity kills");
            return;
        }

        String[] entityNames = whitelistConfig.toLowerCase().split(",");
        int matchedCount = 0;

        for (String searchTerm : entityNames) {
            searchTerm = searchTerm.trim();
            if (searchTerm.isEmpty()) {
                continue;
            }

            // Try exact match with minecraft namespace
            Identifier entityId = new Identifier("minecraft", searchTerm);
            if (Registries.ENTITY_TYPE.containsId(entityId)) {
                whitelistedEntityIds.add(searchTerm);
                matchedCount++;
                LearnPlay.LOGGER.info("[ENTITY KILL TRIGGER] ✓ Added '{}' to whitelist", searchTerm);
            } else {
                LearnPlay.LOGGER.warn("[ENTITY KILL TRIGGER] ✗ Unknown entity: '{}' (must be exact name like 'pig', 'zombie', 'player')", searchTerm);
            }
        }

        LearnPlay.LOGGER.info("[ENTITY KILL TRIGGER] Whitelist updated with {} entity type(s)", matchedCount);
    }

    /**
     * Reset state (e.g., when player disconnects).
     */
    public static void reset() {
        whitelistedEntityIds.clear();
        lastWhitelistConfig = "";
    }
}

