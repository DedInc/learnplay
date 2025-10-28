package com.github.dedinc.learnplay.fabriclike.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

/**
 * Fabric-like entity kill trigger event registration.
 * Uses ServerLivingEntityEvents.AFTER_DEATH to track entity kills.
 */
public class EntityKillTriggerHandler {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Call the common handler
            com.github.dedinc.learnplay.trigger.EntityKillTriggerHandler.onEntityKilled(entity, damageSource);
        });

        LearnPlay.LOGGER.info("Registered entity kill trigger handler (Fabric)");
    }
}

