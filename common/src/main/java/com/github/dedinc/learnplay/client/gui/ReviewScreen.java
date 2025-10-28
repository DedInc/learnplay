package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import com.github.dedinc.learnplay.player.PlayerProgressManager;
import com.github.dedinc.learnplay.srs.SimpleIntervalAlgorithm;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Flashcard review screen with simplified 2-button system.
 * Uses fixed interval progression: 10min, 20min, 1d, 3d, 7d, 14d, 30d, 60d, 120d, 240d.
 * <p>
 * Two buttons:
 * - Forgot: Resets to 10 minutes
 * - Remember: Advances to next interval
 */
public class ReviewScreen extends Screen {
    private final Flashcard card;
    private final SRSState state;
    private final String playerName;
    private boolean showAnswer = false;
    private GuiLayoutHelper layoutHelper;

    public ReviewScreen(Flashcard card, SRSState state, String playerName) {
        super(Text.literal("Flashcard Review"));
        this.card = card;
        this.state = state;
        this.playerName = playerName;
    }

    @Override
    protected void init() {
        super.init();

        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);

        if (!showAnswer) {
            // Show "Reveal Answer" button
            int buttonWidth = layoutHelper.getButtonWidth();
            int buttonHeight = layoutHelper.getButtonHeight();
            int centerX = layoutHelper.getCenterX(buttonWidth);
            int centerY = layoutHelper.getCenterY(0) + layoutHelper.getScaledHeight(0.08);

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Reveal Answer"),
                    button -> {
                        showAnswer = true;
                        clearChildren();
                        init();
                    }
            ).dimensions(centerX, centerY, buttonWidth, buttonHeight).build());

        } else {
            // Show "Forgot" and "Remember" buttons
            int buttonWidth = layoutHelper.getButtonWidth();
            int buttonHeight = layoutHelper.getButtonHeight();
            int spacing = layoutHelper.getSpacing();
            int totalWidth = layoutHelper.getTotalWidth(buttonWidth, 2, spacing);
            int startX = layoutHelper.getCenterX(totalWidth);
            int centerY = layoutHelper.getCenterY(0) + layoutHelper.getScaledHeight(0.12);

            // "Forgot" button - resets to 10 minutes
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Forgot"),
                    button -> onForgot()
            ).dimensions(startX, centerY, buttonWidth, buttonHeight).build());

            // "Remember" button - advances to next interval
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Remember"),
                    button -> onRemember()
            ).dimensions(startX + buttonWidth + spacing, centerY, buttonWidth, buttonHeight).build());
        }

        // Close button (always visible)
        int closeButtonWidth = Math.min(layoutHelper.getSmallButtonWidth(), 80);
        int closeButtonHeight = layoutHelper.getButtonHeight();
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(layoutHelper.getCenterX(closeButtonWidth), layoutHelper.getBottomY(closeButtonHeight), closeButtonWidth, closeButtonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int lineHeight = layoutHelper.getLineHeight();
        int startY = layoutHelper.getContentStartY();

        context.drawCenteredTextWithShadow(this.textRenderer, "Flashcard Review", centerX, layoutHelper.getTitleY(), 0xFFFFFF);

        int currentY = startY;

        currentY += layoutHelper.getSpacing();
        context.drawCenteredTextWithShadow(this.textRenderer, "Question:", centerX, currentY, 0xFFFF55);
        currentY += lineHeight + layoutHelper.getSmallSpacing();
        context.drawCenteredTextWithShadow(this.textRenderer, card.getQuestion(), centerX, currentY, 0xFFFFFF);

        if (showAnswer) {
            currentY += lineHeight + layoutHelper.getSpacing() * 2;
            context.drawCenteredTextWithShadow(this.textRenderer, "Answer:", centerX, currentY, 0x55FF55);
            currentY += lineHeight + layoutHelper.getSmallSpacing();
            context.drawCenteredTextWithShadow(this.textRenderer, card.getAnswer(), centerX, currentY, 0xFFFFFF);

            // Show interval preview
            int previewY = layoutHelper.getCenterY(0) + layoutHelper.getScaledHeight(0.18);
            String forgotInterval = SimpleIntervalAlgorithm.previewForgotInterval();
            String rememberInterval = SimpleIntervalAlgorithm.previewRememberInterval(state);

            String preview = String.format("Forgot: %s | Remember: %s", forgotInterval, rememberInterval);
            context.drawCenteredTextWithShadow(this.textRenderer, preview, centerX, previewY, 0xCCCCCC);

            // Show current progress level
            int level = SimpleIntervalAlgorithm.getCurrentLevel(state);
            int maxLevel = SimpleIntervalAlgorithm.getMaxLevel();
            String levelText = String.format("Progress: Level %d/%d", level, maxLevel);
            context.drawCenteredTextWithShadow(this.textRenderer, levelText, centerX, previewY + lineHeight, 0xAAAAAA);
        }
    }

    private void onForgot() {
        LearnPlay.LOGGER.info("Player forgot card {}", card.getId());

        SimpleIntervalAlgorithm.forgot(state);

        PlayerProgressManager manager = PlayerProgressManager.getInstance();
        manager.updateCardState(playerName, state);

        LearnPlay.LOGGER.info("Card reset to 10 minutes: {}", state);

        this.close();
    }

    private void onRemember() {
        LearnPlay.LOGGER.info("Player remembered card {}", card.getId());

        SimpleIntervalAlgorithm.remember(state);

        PlayerProgressManager manager = PlayerProgressManager.getInstance();
        manager.updateCardState(playerName, state);

        LearnPlay.LOGGER.info("Card advanced to next interval: {}", state);

        this.close();
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}

