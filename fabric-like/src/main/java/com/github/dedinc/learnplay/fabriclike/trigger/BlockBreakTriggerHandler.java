package com.github.dedinc.learnplay.fabriclike.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

/**
 * Fabric-like block break trigger event registration.
 * Uses PlayerBlockBreakEvents.AFTER to track block breaks.
 */
public class BlockBreakTriggerHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            // Call the common handler
            com.github.dedinc.learnplay.trigger.BlockBreakTriggerHandler.onBlockBroken(state, player);
        });

        LearnPlay.LOGGER.info("Registered block break trigger handler (Fabric)");
    }
}

