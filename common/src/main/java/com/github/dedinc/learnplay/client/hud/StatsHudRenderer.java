package com.github.dedinc.learnplay.client.hud;

import com.github.dedinc.learnplay.config.LearnPlayConfig;
import com.github.dedinc.learnplay.player.PlayerProgressManager;
import com.github.dedinc.learnplay.srs.ReviewScheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Renders HUD statistics overlay showing flashcard review progress.
 * Displays in the top-right corner of the screen when enabled in config.
 */
public class StatsHudRenderer {

    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int BORDER_COLOR = 0xFF555555; // Gray border

    // Lazy-initialized scheduler instance
    private static ReviewScheduler scheduler = null;

    /**
     * Render the HUD stats overlay.
     * Call this from the HUD render event.
     *
     * @param context Draw context
     * @param client  Minecraft client instance
     */
    public static void render(DrawContext context, MinecraftClient client) {
        LearnPlayConfig config = LearnPlayConfig.getInstance();

        // Check if HUD is enabled
        if (!config.showHudStats) {
            return;
        }

        // Don't show in GUI screens
        if (client.currentScreen != null) {
            return;
        }

        // Don't show if player is null
        if (client.player == null) {
            return;
        }

        String playerName = client.player.getName().getString();
        TextRenderer textRenderer = client.textRenderer;

        // Lazy-initialize scheduler
        if (scheduler == null) {
            scheduler = new ReviewScheduler();
        }

        // Get statistics
        PlayerProgressManager progressManager = PlayerProgressManager.getInstance();

        ReviewScheduler.ReviewStats reviewStats = scheduler.getReviewStats(playerName);
        PlayerProgressManager.PlayerStats playerStats = progressManager.getPlayerStats(playerName);

        // Prepare text lines with new terminology
        String[] lines = {
                "§6LearnPlay Stats",
                "§7Revise: §f" + reviewStats.reviseCards,
                "§7Learn: §f" + reviewStats.learnCards,
                "§7Weak: §f" + playerStats.weakCards,
                "§7Middle: §f" + playerStats.middleCards,
                "§7Strong: §f" + playerStats.strongCards,
                "§7Total: §f" + reviewStats.totalCards
        };

        // Calculate dimensions
        int maxWidth = 0;
        for (String line : lines) {
            int width = textRenderer.getWidth(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        int hudWidth = maxWidth + PADDING * 2;
        int hudHeight = (lines.length * LINE_HEIGHT) + PADDING * 2;

        // Position in top-right corner
        int screenWidth = context.getScaledWindowWidth();
        int x = screenWidth - hudWidth - 5;
        int y = 5;

        // Draw background
        context.fill(x, y, x + hudWidth, y + hudHeight, BACKGROUND_COLOR);

        // Draw border
        drawBorder(context, x, y, hudWidth, hudHeight);

        // Draw text lines
        int textY = y + PADDING;
        for (String line : lines) {
            context.drawText(textRenderer, line, x + PADDING, textY, 0xFFFFFF, true);
            textY += LINE_HEIGHT;
        }
    }

    /**
     * Draw a border around the HUD.
     */
    private static void drawBorder(DrawContext context, int x, int y, int width, int height) {
        // Top
        context.fill(x, y, x + width, y + 1, BORDER_COLOR);
        // Bottom
        context.fill(x, y + height - 1, x + width, y + height, BORDER_COLOR);
        // Left
        context.fill(x, y, x + 1, y + height, BORDER_COLOR);
        // Right
        context.fill(x + width - 1, y, x + width, y + height, BORDER_COLOR);
    }
}

