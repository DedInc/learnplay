package com.github.dedinc.learnplay.forgelike.trigger;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Forge-like block place trigger handler.
 * This is a wrapper that platform-specific implementations can call.
 * <p>
 * For Forge/NeoForge: Register BlockEvent.PlaceEvent and call onBlockPlaced()
 */
public class BlockPlaceTriggerHandler {

    /**
     * Called when a block is placed. Platform-specific code should call this from their place event.
     *
     * @param state  The block state that was placed
     * @param player The player who placed the block
     */
    public static void onBlockPlaced(BlockState state, PlayerEntity player) {
        // Forward to common handler
        com.github.dedinc.learnplay.trigger.BlockPlaceTriggerHandler.onBlockPlaced(state, player);
    }
}

