package com.github.dedinc.learnplay.forgelike.trigger;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Forge-like block break trigger handler.
 * This is a wrapper that platform-specific implementations can call.
 * <p>
 * For Forge/NeoForge: Register BlockEvent.BreakEvent and call onBlockBroken()
 */
public class BlockBreakTriggerHandler {

    /**
     * Called when a block is broken. Platform-specific code should call this from their break event.
     *
     * @param state  The block state that was broken
     * @param player The player who broke the block
     */
    public static void onBlockBroken(BlockState state, PlayerEntity player) {
        // Forward to common handler
        com.github.dedinc.learnplay.trigger.BlockBreakTriggerHandler.onBlockBroken(state, player);
    }
}

