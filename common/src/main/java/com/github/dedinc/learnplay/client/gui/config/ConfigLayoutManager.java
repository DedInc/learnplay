package com.github.dedinc.learnplay.client.gui.config;

import com.github.dedinc.learnplay.client.gui.GuiLayoutHelper;

/**
 * Manages responsive layout for configuration screens.
 * Handles single vs. two-column layouts based on screen size.
 */
public class ConfigLayoutManager {

    private final GuiLayoutHelper layoutHelper;
    private final boolean useSingleColumn;
    private final int columnWidth;
    private final int leftColumnX;
    private final int rightColumnX;
    private final int screenWidth;

    public ConfigLayoutManager(GuiLayoutHelper layoutHelper, int screenWidth) {
        this.layoutHelper = layoutHelper;
        this.screenWidth = screenWidth;

        // Determine if we should use single column layout for small screens
        this.useSingleColumn = layoutHelper.getScreenSize() == GuiLayoutHelper.ScreenSize.TINY ||
                layoutHelper.getMaxContentWidth() < 400;

        // Calculate column positions
        int margin = layoutHelper.getMargin();
        int availableWidth = layoutHelper.getMaxContentWidth();
        int columnGap = layoutHelper.getSpacing();

        if (useSingleColumn) {
            // Single column layout for small screens
            this.columnWidth = Math.min(availableWidth, 300);
            this.leftColumnX = layoutHelper.getCenterX(columnWidth);
            this.rightColumnX = leftColumnX; // Same position for single column
        } else {
            // Two column layout
            int maxColumnWidth = Math.min(280, availableWidth / 2 - columnGap);
            int minColumnWidth = 180;
            this.columnWidth = Math.max(minColumnWidth, Math.min(maxColumnWidth, (availableWidth - columnGap) / 2));

            int totalColumnsWidth = columnWidth * 2 + columnGap;
            this.leftColumnX = (screenWidth - totalColumnsWidth) / 2;
            this.rightColumnX = leftColumnX + columnWidth + columnGap;
        }
    }

    public boolean isSingleColumn() {
        return useSingleColumn;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public int getLeftColumnX() {
        return leftColumnX;
    }

    public int getRightColumnX() {
        return rightColumnX;
    }

    public GuiLayoutHelper getLayoutHelper() {
        return layoutHelper;
    }
}

