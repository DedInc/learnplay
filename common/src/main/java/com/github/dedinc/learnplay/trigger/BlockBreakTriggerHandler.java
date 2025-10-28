package com.github.dedinc.learnplay.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.config.LearnPlayConfig;
import com.github.dedinc.learnplay.config.TriggerConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Block break trigger handler that tracks block breaks.
 * Supports filtering by block type through an exact-match whitelist.
 * <p>
 * Platform-specific implementations should call {@link #onBlockBroken(BlockState, PlayerEntity)}
 * when a block is broken by a player.
 */
public class BlockBreakTriggerHandler {

    private static final Set<String> whitelistedBlockIds = new HashSet<>();
    private static String lastWhitelistConfig = "";

    /**
     * Called when a block is broken. Platform-specific implementations should call this.
     *
     * @param state  The block state that was broken
     * @param player The player who broke the block
     */
    public static void onBlockBroken(BlockState state, PlayerEntity player) {
        // Check if trigger is enabled
        LearnPlayConfig config = LearnPlayConfig.getInstance();
        if (!config.triggers.enableBlockBreakTrigger) {
            return;
        }

        // Check if broken by a player
        if (player == null) {
            return;
        }

        // Update whitelist if config changed
        String currentWhitelist = config.triggers.blockBreakWhitelist;
        if (!currentWhitelist.equals(lastWhitelistConfig)) {
            updateWhitelist(currentWhitelist);
            lastWhitelistConfig = currentWhitelist;
        }

        // Get block ID
        Block block = state.getBlock();
        Identifier blockId = Registries.BLOCK.getId(block);
        if (blockId == null) {
            return;
        }

        String blockPath = blockId.getPath();

        // Check if block is whitelisted (or whitelist is empty = all blocks)
        if (!whitelistedBlockIds.isEmpty() && !whitelistedBlockIds.contains(blockPath)) {
            return; // Not in whitelist, ignore
        }

        LearnPlay.LOGGER.info("[BLOCK BREAK TRIGGER] Player broke: {}", blockPath);

        // Trigger the flashcard review
        ClientTriggerManager triggerManager = ClientTriggerManager.getInstance();
        triggerManager.attemptTrigger(TriggerConfig.TriggerType.BLOCK_BREAK);
    }

    /**
     * Update the block whitelist from config string.
     * Uses EXACT matching: "stone" matches only "stone", not "stone_bricks".
     */
    private static void updateWhitelist(String whitelistConfig) {
        whitelistedBlockIds.clear();

        if (whitelistConfig == null || whitelistConfig.trim().isEmpty()) {
            // Empty whitelist = track all blocks
            LearnPlay.LOGGER.info("[BLOCK BREAK TRIGGER] Whitelist empty - tracking all block breaks");
            return;
        }

        // Parse comma-separated list
        String[] blockNames = whitelistConfig.split(",");
        for (String blockName : blockNames) {
            String trimmed = blockName.trim();
            if (!trimmed.isEmpty()) {
                whitelistedBlockIds.add(trimmed);
            }
        }

        LearnPlay.LOGGER.info("[BLOCK BREAK TRIGGER] Whitelist updated: {} blocks tracked", whitelistedBlockIds.size());
        LearnPlay.LOGGER.debug("[BLOCK BREAK TRIGGER] Tracking blocks: {}", whitelistedBlockIds);
    }
}

