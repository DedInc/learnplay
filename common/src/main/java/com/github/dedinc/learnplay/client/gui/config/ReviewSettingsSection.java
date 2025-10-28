package com.github.dedinc.learnplay.client.gui.config;

import com.github.dedinc.learnplay.config.LearnPlayConfig;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.function.Consumer;

/**
 * Builds the Review Settings section of the config screen.
 * Handles max cards per session, new cards per day, and reviews per day.
 */
public class ReviewSettingsSection {

    private final LearnPlayConfig config;
    private final ConfigSettingBuilder settingBuilder;

    public ReviewSettingsSection(LearnPlayConfig config, ConfigSettingBuilder settingBuilder) {
        this.config = config;
        this.settingBuilder = settingBuilder;
    }

    /**
     * Build all review settings UI elements.
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
        settingBuilder.addCenteredLabel("Review Settings", x + columnWidth / 2, currentY);
        currentY += buttonHeight + spacing;

        // Max Cards Per Session
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Max cards per session:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.maxCardsPerSession),
                value -> {
                    config.maxCardsPerSession = ConfigSettingBuilder.parseIntSafe(value, 20, 1, 100);
                    config.save();
                }));
        currentY += buttonHeight + spacing;

        // Max New Cards Per Day
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Max new cards per day:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.maxNewCardsPerDay),
                value -> {
                    config.maxNewCardsPerDay = ConfigSettingBuilder.parseIntSafe(value, 10, 1, 50);
                    config.save();
                }));
        currentY += buttonHeight + spacing;

        // Max Reviews Per Day
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Max reviews per day:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.maxReviewsPerDay),
                value -> {
                    config.maxReviewsPerDay = ConfigSettingBuilder.parseIntSafe(value, 100, 1, 500);
                    config.save();
                }));
        currentY += buttonHeight;

        return currentY;
    }
}

