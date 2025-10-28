package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.client.gui.config.*;
import com.github.dedinc.learnplay.config.LearnPlayConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game configuration screen for LearnPlay mod.
 * Allows players to configure triggers, review settings, and UI options.
 * Uses responsive 2-column layout (or single column on small screens) with scrolling support.
 * Adapts to different screen sizes dynamically.
 */
public class ConfigScreen extends Screen {
    private final Screen parent;
    private final LearnPlayConfig config;

    private double scrollOffset = 0;
    private GuiLayoutHelper layoutHelper;
    private ConfigLayoutManager layoutManager;
    private ConfigSettingBuilder settingBuilder;

    // Keep trigger section as field to preserve tab state
    private TriggerSettingsSection triggerSection;

    private List<ConfigSettingBuilder.LabelData> labels = new ArrayList<>();

    public ConfigScreen(Screen parent) {
        super(Text.literal("LearnPlay Settings"));
        this.parent = parent;
        this.config = LearnPlayConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        labels.clear();

        // Initialize layout helpers - pass textRenderer for font-aware spacing
        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);
        layoutManager = new ConfigLayoutManager(layoutHelper, this.width);
        settingBuilder = new ConfigSettingBuilder(this.textRenderer, labels);

        // Initialize section builders (preserve trigger section to keep tab state)
        if (triggerSection == null) {
            triggerSection = new TriggerSettingsSection(config, settingBuilder);
        }
        ReviewSettingsSection reviewSection = new ReviewSettingsSection(config, settingBuilder);
        UISettingsSection uiSection = new UISettingsSection(config, settingBuilder);

        int columnWidth = layoutManager.getColumnWidth();
        int leftColumnX = layoutManager.getLeftColumnX();
        int rightColumnX = layoutManager.getRightColumnX();

        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSmallSpacing();
        int sectionSpacing = layoutHelper.getSpacing();
        int startY = layoutHelper.getContentStartY() - (int) scrollOffset;

        // Left Column - Trigger Settings (with tab switching callback)
        triggerSection.build(this::addDrawableChild, this::rebuildScreen, leftColumnX, startY, columnWidth, buttonHeight, spacing, sectionSpacing);

        // Right Column - Review & UI Settings (or continue in single column)
        int rightStartY = layoutManager.isSingleColumn() ? startY + sectionSpacing * 2 : startY;

        // Review Settings
        int currentY = reviewSection.build(this::addDrawableChild, rightColumnX, rightStartY, columnWidth, buttonHeight, spacing);
        currentY += sectionSpacing;

        // UI Settings
        uiSection.build(this::addDrawableChild, rightColumnX, currentY, columnWidth, buttonHeight, spacing);

        // Bottom buttons
        buildBottomButtons(buttonHeight);
    }

    /**
     * Rebuild the screen (used when switching tabs).
     */
    private void rebuildScreen() {
        this.clearChildren();
        this.init();
    }

    /**
     * Build the bottom navigation buttons (Browse Categories & Done).
     */
    private void buildBottomButtons(int buttonHeight) {
        int margin = layoutHelper.getMargin();

        // Browse Categories & Decks Button (positioned above Done button)
        String browseButtonText = "Browse Categories & Decks";
        int minBrowseButtonWidth = this.textRenderer.getWidth(browseButtonText) + 20;
        int manageDecksButtonWidth = Math.max(minBrowseButtonWidth, Math.min(250, this.width - margin * 2));
        int manageDecksButtonHeight = buttonHeight;
        addDrawableChild(ButtonWidget.builder(
                Text.literal(browseButtonText),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new CategoryBrowserScreen(this));
                    }
                }
        ).dimensions(
                layoutHelper.getCenterX(manageDecksButtonWidth),
                layoutHelper.getBottomY(manageDecksButtonHeight * 2 + layoutHelper.getSpacing()),
                manageDecksButtonWidth,
                manageDecksButtonHeight
        ).build());

        // Done Button (positioned dynamically with responsive width)
        int doneButtonWidth = Math.min(layoutHelper.getButtonWidth(), this.width - margin * 2);
        int doneButtonHeight = buttonHeight;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> close()
        ).dimensions(
                layoutHelper.getCenterX(doneButtonWidth),
                layoutHelper.getBottomY(doneButtonHeight),
                doneButtonWidth,
                doneButtonHeight
        ).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, layoutHelper.getTitleY(), 0xFFFFFF);

        // Render all labels
        int visibleTop = layoutHelper.getContentStartY() - layoutHelper.getSpacing();
        int visibleBottom = this.height - layoutHelper.getMargin() * 3;

        for (ConfigSettingBuilder.LabelData label : labels) {
            if (label.y >= visibleTop && label.y <= visibleBottom) { // Only render visible labels
                if (label.isCentered) {
                    // Section headers
                    context.drawCenteredTextWithShadow(this.textRenderer, label.text, label.x, label.y, 0xFFFF55);
                } else {
                    // Setting labels
                    context.drawTextWithShadow(this.textRenderer, label.text, label.x, label.y, 0xFFFFFF);
                }
            }
        }

        // Help text - positioned above the Browse button (3 button heights from bottom)
        String helpText = "Type values or use ON/OFF buttons | Press O to close";
        int helpTextWidth = this.textRenderer.getWidth(helpText);
        int browseButtonY = layoutHelper.getBottomY(layoutHelper.getButtonHeight() * 2 + layoutHelper.getSpacing());
        int helpTextY = browseButtonY - layoutHelper.getLineHeight() - layoutHelper.getSpacing();
        context.drawText(this.textRenderer, helpText,
                this.width / 2 - helpTextWidth / 2, helpTextY, 0x808080, false);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}
