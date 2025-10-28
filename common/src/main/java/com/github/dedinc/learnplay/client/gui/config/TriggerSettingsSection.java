package com.github.dedinc.learnplay.client.gui.config;

import com.github.dedinc.learnplay.config.LearnPlayConfig;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Builds the Trigger Settings section of the config screen.
 * Uses a tabbed interface to organize different trigger types:
 * - Kill/Death Triggers
 * - Break/Place Triggers
 * - Chat Triggers
 * - Timer Trigger
 */
public class TriggerSettingsSection {

    private final LearnPlayConfig config;
    private final ConfigSettingBuilder settingBuilder;

    // Tab state
    private TriggerTab currentTab = TriggerTab.KILL_DEATH;

    public enum TriggerTab {
        KILL_DEATH("Kill/Death"),
        BREAK_PLACE("Break/Place"),
        CHAT("Chat"),
        TIMER("Timer");

        private final String displayName;

        TriggerTab(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public TriggerSettingsSection(LearnPlayConfig config, ConfigSettingBuilder settingBuilder) {
        this.config = config;
        this.settingBuilder = settingBuilder;
    }

    public void setCurrentTab(TriggerTab tab) {
        this.currentTab = tab;
    }

    public TriggerTab getCurrentTab() {
        return currentTab;
    }

    /**
     * Build all trigger settings UI elements with tabs.
     *
     * @param widgetAdder    Consumer to add widgets to the screen
     * @param tabSwitcher    Consumer to handle tab switching (rebuilds the screen)
     * @param x              Column X position
     * @param startY         Starting Y position
     * @param columnWidth    Width of the column
     * @param buttonHeight   Height of buttons
     * @param spacing        Small spacing between elements
     * @param sectionSpacing Larger spacing between sections
     * @return Final Y position after all elements
     */
    public int build(Consumer<ClickableWidget> widgetAdder, Runnable tabSwitcher, int x, int startY, int columnWidth, int buttonHeight, int spacing, int sectionSpacing) {
        int currentY = startY;

        // Section header
        settingBuilder.addCenteredLabel("Trigger Settings", x + columnWidth / 2, currentY);
        currentY += buttonHeight + spacing;

        // Tab buttons
        currentY = buildTabButtons(widgetAdder, tabSwitcher, x, currentY, columnWidth, buttonHeight, spacing);
        currentY += sectionSpacing;

        // Content based on selected tab
        currentY = buildTabContent(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing, sectionSpacing);

        return currentY;
    }

    /**
     * Build tab buttons.
     */
    private int buildTabButtons(Consumer<ClickableWidget> widgetAdder, Runnable tabSwitcher, int x, int currentY, int columnWidth, int buttonHeight, int spacing) {
        int tabCount = TriggerTab.values().length;
        int tabWidth = (columnWidth - spacing * (tabCount - 1)) / tabCount;

        int tabX = x;
        for (TriggerTab tab : TriggerTab.values()) {
            boolean isSelected = tab == currentTab;
            String buttonText = (isSelected ? "§l" : "") + tab.getDisplayName();

            widgetAdder.accept(ButtonWidget.builder(
                    Text.literal(buttonText),
                    button -> {
                        if (currentTab != tab) {
                            currentTab = tab;
                            tabSwitcher.run(); // Rebuild the screen
                        }
                    }
            ).dimensions(tabX, currentY, tabWidth, buttonHeight).build());

            tabX += tabWidth + spacing;
        }

        return currentY + buttonHeight;
    }

    /**
     * Build content for the currently selected tab.
     */
    private int buildTabContent(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing, int sectionSpacing) {
        return switch (currentTab) {
            case KILL_DEATH ->
                    buildKillDeathTab(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing, sectionSpacing);
            case BREAK_PLACE ->
                    buildBreakPlaceTab(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing, sectionSpacing);
            case CHAT -> buildChatTab(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing, sectionSpacing);
            case TIMER -> buildTimerTab(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing, sectionSpacing);
        };
    }

    /**
     * Build Kill/Death triggers tab.
     */
    private int buildKillDeathTab(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing, int sectionSpacing) {
        // Death Trigger
        currentY = buildDeathTrigger(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing);
        currentY += sectionSpacing;

        // Entity Kill Trigger
        currentY = buildEntityKillTrigger(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing);

        return currentY;
    }

    /**
     * Build Break/Place triggers tab.
     */
    private int buildBreakPlaceTab(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing, int sectionSpacing) {
        // Block Break Trigger
        currentY = buildBlockBreakTrigger(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing);
        currentY += sectionSpacing;

        // Block Place Trigger
        currentY = buildBlockPlaceTrigger(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing);

        return currentY;
    }

    /**
     * Build Chat trigger tab.
     */
    private int buildChatTab(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing, int sectionSpacing) {
        return buildChatTrigger(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing);
    }

    /**
     * Build Timer trigger tab.
     */
    private int buildTimerTab(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing, int sectionSpacing) {
        return buildTimerTrigger(widgetAdder, x, currentY, columnWidth, buttonHeight, spacing);
    }

    private int buildDeathTrigger(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing) {
        // Death Trigger Toggle
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.triggers.enableDeathTrigger)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Death Trigger"),
                        (button, value) -> {
                            config.triggers.enableDeathTrigger = value;
                            config.save();
                        }));
        currentY += buttonHeight + spacing;

        // Death Every N Deaths
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Trigger every N deaths:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.triggers.deathTriggerEveryNDeaths),
                value -> {
                    config.triggers.deathTriggerEveryNDeaths = ConfigSettingBuilder.parseIntSafe(value, 1, 1, 100);
                    config.save();
                }));
        currentY += buttonHeight;

        return currentY;
    }

    private int buildTimerTrigger(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing) {
        // Timer Trigger Toggle
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.triggers.enableTimerTrigger)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Timer Trigger"),
                        (button, value) -> {
                            config.triggers.enableTimerTrigger = value;
                            config.save();
                        }));
        currentY += buttonHeight + spacing;

        // Timer Interval
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Timer interval (minutes):", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.triggers.timerIntervalMinutes),
                value -> {
                    config.triggers.timerIntervalMinutes = ConfigSettingBuilder.parseIntSafe(value, 15, 1, 120);
                    config.save();
                }));
        currentY += buttonHeight;

        return currentY;
    }

    private int buildEntityKillTrigger(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing) {
        // Entity Kill Trigger Toggle
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.triggers.enableEntityKillTrigger)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Entity Kill Trigger"),
                        (button, value) -> {
                            config.triggers.enableEntityKillTrigger = value;
                            config.save();
                        }));
        currentY += buttonHeight + spacing;

        // Entity Kill Threshold
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Trigger every N kills:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.triggers.entityKillTriggerThreshold),
                value -> {
                    config.triggers.entityKillTriggerThreshold = ConfigSettingBuilder.parseIntSafe(value, 10, 1, 1000);
                    config.save();
                }));
        currentY += buttonHeight + spacing;

        // Entity Kill Whitelist
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Entity whitelist (comma-separated):", x, currentY, columnWidth, buttonHeight,
                config.triggers.entityKillWhitelist,
                value -> {
                    config.triggers.entityKillWhitelist = value;
                    config.save();
                },
                200, // max length for whitelist
                (int) (columnWidth * 0.6) // wider field for entity list
        ));
        currentY += buttonHeight;

        return currentY;
    }

    private int buildBlockBreakTrigger(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing) {
        // Block Break Trigger Toggle
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.triggers.enableBlockBreakTrigger)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Block Break Trigger"),
                        (button, value) -> {
                            config.triggers.enableBlockBreakTrigger = value;
                            config.save();
                        }));
        currentY += buttonHeight + spacing;

        // Block Break Threshold
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Trigger every N breaks:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.triggers.blockBreakTriggerThreshold),
                value -> {
                    config.triggers.blockBreakTriggerThreshold = ConfigSettingBuilder.parseIntSafe(value, 100, 1, 10000);
                    config.save();
                }));
        currentY += buttonHeight + spacing;

        // Block Break Whitelist
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Block whitelist (comma-separated):", x, currentY, columnWidth, buttonHeight,
                config.triggers.blockBreakWhitelist,
                value -> {
                    config.triggers.blockBreakWhitelist = value;
                    config.save();
                },
                200, // max length for whitelist
                (int) (columnWidth * 0.6) // wider field for block list
        ));
        currentY += buttonHeight;

        return currentY;
    }

    private int buildBlockPlaceTrigger(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing) {
        // Block Place Trigger Toggle
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.triggers.enableBlockPlaceTrigger)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Block Place Trigger"),
                        (button, value) -> {
                            config.triggers.enableBlockPlaceTrigger = value;
                            config.save();
                        }));
        currentY += buttonHeight + spacing;

        // Block Place Threshold
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Trigger every N placements:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.triggers.blockPlaceTriggerThreshold),
                value -> {
                    config.triggers.blockPlaceTriggerThreshold = ConfigSettingBuilder.parseIntSafe(value, 50, 1, 10000);
                    config.save();
                }));
        currentY += buttonHeight + spacing;

        // Block Place Whitelist
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Block whitelist (comma-separated):", x, currentY, columnWidth, buttonHeight,
                config.triggers.blockPlaceWhitelist,
                value -> {
                    config.triggers.blockPlaceWhitelist = value;
                    config.save();
                },
                200, // max length for whitelist
                (int) (columnWidth * 0.6) // wider field for block list
        ));
        currentY += buttonHeight;

        return currentY;
    }

    private int buildChatTrigger(Consumer<ClickableWidget> widgetAdder, int x, int currentY, int columnWidth, int buttonHeight, int spacing) {
        // Chat Trigger Toggle
        widgetAdder.accept(CyclingButtonWidget.onOffBuilder(config.triggers.enableChatTrigger)
                .build(x, currentY, columnWidth, buttonHeight,
                        Text.literal("Chat Trigger"),
                        (button, value) -> {
                            config.triggers.enableChatTrigger = value;
                            config.save();
                        }));
        currentY += buttonHeight + spacing;

        // Chat Trigger Pattern
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Chat pattern (contains):", x, currentY, columnWidth, buttonHeight,
                config.triggers.chatTriggerPattern,
                value -> {
                    config.triggers.chatTriggerPattern = value;
                    config.save();
                },
                50, // max length for pattern
                columnWidth / 2 // wider field for text
        ));
        currentY += buttonHeight + spacing;

        // Chat Trigger Threshold
        widgetAdder.accept(settingBuilder.createTextFieldSetting(
                "Trigger every N matches:", x, currentY, columnWidth, buttonHeight,
                String.valueOf(config.triggers.chatTriggerThreshold),
                value -> {
                    config.triggers.chatTriggerThreshold = ConfigSettingBuilder.parseIntSafe(value, 10, 1, 1000);
                    config.save();
                }));
        currentY += buttonHeight;

        return currentY;
    }

}

