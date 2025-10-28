package com.github.dedinc.learnplay.forgelike.trigger;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

/**
 * Forge-like entity kill trigger handler.
 * This is a wrapper that platform-specific implementations can call.
 * <p>
 * For Forge/NeoForge: Register LivingDeathEvent and call onEntityKilled()
 */
public class EntityKillTriggerHandler {

    /**
     * Called when an entity dies. Platform-specific code should call this from their death event.
     *
     * @param entity The entity that died
     * @param source The damage source
     */
    public static void onEntityKilled(LivingEntity entity, DamageSource source) {
        // Forward to common handler
        com.github.dedinc.learnplay.trigger.EntityKillTriggerHandler.onEntityKilled(entity, source);
    }
}

