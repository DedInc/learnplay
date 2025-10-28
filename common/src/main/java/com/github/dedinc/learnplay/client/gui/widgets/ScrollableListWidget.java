package com.github.dedinc.learnplay.client.gui.widgets;

import com.github.dedinc.learnplay.client.gui.GuiLayoutHelper;

/**
 * Helper for managing scrollable list behavior.
 */
public class ScrollableListWidget {

    private double scrollOffset = 0;
    private final int rowHeight;
    private final double scrollSpeed;
    private final GuiLayoutHelper layoutHelper;

    public ScrollableListWidget(GuiLayoutHelper layoutHelper, int rowHeight, double scrollSpeed) {
        this.layoutHelper = layoutHelper;
        this.rowHeight = rowHeight;
        this.scrollSpeed = scrollSpeed;
    }

    /**
     * Handle mouse scroll event.
     */
    public boolean handleScroll(double verticalAmount, int itemCount, int screenHeight, int reservedBottomSpace) {
        scrollOffset -= verticalAmount * scrollSpeed;

        // Calculate max scroll
        int maxScroll = Math.max(0, itemCount * (rowHeight + layoutHelper.getSmallSpacing()) - (screenHeight - reservedBottomSpace));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        return true;
    }

    /**
     * Get the current scroll offset.
     */
    public double getScrollOffset() {
        return scrollOffset;
    }

    /**
     * Reset scroll offset.
     */
    public void resetScroll() {
        scrollOffset = 0;
    }

    /**
     * Check if an item at the given Y position is visible.
     */
    public boolean isVisible(int adjustedY, int minY, int maxY) {
        return adjustedY >= minY && adjustedY <= maxY;
    }

    /**
     * Calculate adjusted Y position with scroll offset.
     */
    public int getAdjustedY(int baseY) {
        return (int) (baseY - scrollOffset);
    }
}

