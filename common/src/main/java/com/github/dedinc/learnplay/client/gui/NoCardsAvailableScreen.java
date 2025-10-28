package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.srs.ReviewScheduler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Screen shown when there are no cards available for review.
 * Displays statistics and helpful information to the user.
 * Uses dynamic layout that adapts to screen size.
 */
public class NoCardsAvailableScreen extends Screen {
    private final ReviewScheduler.ReviewStats stats;
    private GuiLayoutHelper layoutHelper;

    public NoCardsAvailableScreen(ReviewScheduler.ReviewStats stats) {
        super(Text.literal("No Cards Available"));
        this.stats = stats;
    }

    @Override
    protected void init() {
        super.init();

        // Initialize layout helper with textRenderer for font-aware spacing
        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);

        // Add "OK" button to close the screen
        int buttonWidth = Math.min(layoutHelper.getButtonWidth(), 120);
        int buttonHeight = layoutHelper.getButtonHeight();
        int buttonY = layoutHelper.getCenterY(0) + layoutHelper.getScaledHeight(0.15);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("OK"),
                button -> this.close()
        ).dimensions(layoutHelper.getCenterX(buttonWidth), buttonY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int lineHeight = layoutHelper.getLineHeight();
        int spacing = layoutHelper.getSpacing();
        int startY = layoutHelper.getCenterY(0) - layoutHelper.getScaledHeight(0.15);

        int currentY = startY;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, "No Cards Available", centerX, currentY, 0xFFFF55);
        currentY += lineHeight + spacing;

        // Main message
        context.drawCenteredTextWithShadow(this.textRenderer,
                "All cards are scheduled for future review!", centerX, currentY, 0xFFFFFF);
        currentY += lineHeight + spacing;

        // Statistics
        currentY += spacing;
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Total Cards: " + stats.totalCards, centerX, currentY, 0xAAAAAA);
        currentY += lineHeight + layoutHelper.getSmallSpacing();

        context.drawCenteredTextWithShadow(this.textRenderer,
                "Reviewed: " + stats.reviewedCards + " | Scheduled: " + stats.scheduledCards,
                centerX, currentY, 0xAAAAAA);
        currentY += lineHeight + spacing;

        // Helpful tip
        currentY += spacing;
        if (stats.scheduledCards > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    "Come back later to review your cards!", centerX, currentY, 0x55FF55);
        } else if (stats.totalCards == 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    "No decks loaded. Add flashcard decks to config/learnplay/decks/",
                    centerX, currentY, 0xFF5555);
        }
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}
