package com.github.dedinc.learnplay.fabriclike.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric-like block place trigger event registration.
 * Uses UseBlockCallback to detect potential placements, then verifies on next tick.
 * This ensures we only count actual successful placements.
 */
public class BlockPlaceTriggerHandler {

    // List for pending placement checks (to verify placement after the action)
    private static final List<PendingPlacementCheck> pendingChecks = new ArrayList<>();

    public static void register() {
        // Register UseBlockCallback to detect potential block placements
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Only track main hand to avoid double-counting
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            // Only process on server side
            if (player.isSpectator() || !(world instanceof ServerWorld serverWorld)) {
                return ActionResult.PASS;
            }

            // Check if player is holding a block item
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block expectedBlock = blockItem.getBlock();

                // Calculate the position where the block would be placed
                BlockPos pos = hitResult.getBlockPos().offset(hitResult.getSide());

                // Check if the position is replaceable (likely to allow placement)
                if (world.getBlockState(pos).isReplaceable()) {
                    // Add to pending checks to verify after placement
                    pendingChecks.add(new PendingPlacementCheck(serverWorld, pos, expectedBlock, player.getUuid()));
                }
            }

            return ActionResult.PASS; // Don't cancel the action
        });

        // Register server tick event to check pending placements on the next tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Process all pending checks
            List<PendingPlacementCheck> toRemove = new ArrayList<>();
            long currentTick = server.getTicks();

            for (PendingPlacementCheck check : pendingChecks) {
                // Check if the block at the position is now the expected block
                if (check.world.getBlockState(check.pos).getBlock() == check.expectedBlock) {
                    // Verify placement was successful, call the common handler
                    var blockState = check.world.getBlockState(check.pos);
                    var player = check.world.getPlayerByUuid(check.playerUuid);
                    if (player != null) {
                        com.github.dedinc.learnplay.trigger.BlockPlaceTriggerHandler.onBlockPlaced(blockState, player);
                    }
                    toRemove.add(check);
                } else if (check.isExpired(currentTick)) {
                    // Placement failed or was cancelled, remove from pending
                    toRemove.add(check);
                }
            }
            // Remove completed/expired checks (avoid concurrent modification)
            pendingChecks.removeAll(toRemove);
        });

        LearnPlay.LOGGER.info("Registered block place trigger handler (Fabric)");
    }

    /**
     * Helper class for pending placement checks.
     */
    private static class PendingPlacementCheck {
        final ServerWorld world;
        final BlockPos pos;
        final Block expectedBlock;
        final java.util.UUID playerUuid;
        final long tickCreated;

        PendingPlacementCheck(ServerWorld world, BlockPos pos, Block expectedBlock, java.util.UUID playerUuid) {
            this.world = world;
            this.pos = pos;
            this.expectedBlock = expectedBlock;
            this.playerUuid = playerUuid;
            this.tickCreated = world.getServer().getTicks();
        }

        /**
         * Check if this pending check is too old (more than 5 ticks = ~250ms).
         * If placement hasn't happened by then, it likely failed.
         */
        boolean isExpired(long currentTick) {
            return (currentTick - tickCreated) > 5;
        }
    }
}

