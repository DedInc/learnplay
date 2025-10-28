package com.github.dedinc.learnplay.client.gui.config;

import com.github.dedinc.learnplay.config.LearnPlayConfig;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Builds the UI Settings section of the config screen.
 * Handles pause game during review and show HUD stats settings.
 */
public class UISettingsSection {

    private final LearnPlayConfig config;
    private final ConfigSettingBuilder settingBuilder;

    public UISettingsSection(LearnPlayConfig config, ConfigSettingBuilder settingBuilder) {
        this.config = config;
        this.settingBuilder = settingBuilder;
    }

    /**
     * Build all UI settings UI elements.
     *
     * @param widgetAdder  Consumer to add widgets to the screen
     * @param x            Column X position
     * @param startY       Starting Y position
     * @param columnWidth  Width of the column
     * @param buttonHeight Height of buttons
     * @param spacing      Small spacing between elements
     * @return Final Y position after all elements
     */
    public int build(Consumer<ClickableWidget> widgetAdder, int x, int startY, int columnWidth, int buttonHeight, int spacing) {
        int currentY = startY;

        // Section header
        settingBuilder.addCenteredLabel("UI Settings", x + columnWidth / 2, currentY);
        currentY += buttonHeight + spacing;

        // Pause Game During Review
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.pauseGameDuringReview)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Pause game during review"),
                        (button, value) -> {
                            config.pauseGameDuringReview = value;
                            config.save();
                        }));
        currentY += buttonHeight + spacing;

        // Show HUD Stats
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.showHudStats)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Show HUD stats"),
                        (button, value) -> {
                            config.showHudStats = value;
                            config.save();
                        }));
        currentY += buttonHeight;

        return currentY;
    }
}

