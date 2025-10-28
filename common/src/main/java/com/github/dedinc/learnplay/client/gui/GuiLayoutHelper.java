package com.github.dedinc.learnplay.client.gui;

import net.minecraft.client.font.TextRenderer;

/**
 * Utility class for calculating dynamic, responsive GUI layouts.
 * Provides methods to calculate element sizes and positions based on screen dimensions.
 * <p>
 * This ensures GUI elements scale properly across different screen sizes and resolutions.
 */
public class GuiLayoutHelper {

    // Screen size categories
    private static final int TINY_SCREEN_WIDTH = 640;
    private static final int SMALL_SCREEN_WIDTH = 854;
    private static final int MEDIUM_SCREEN_WIDTH = 1280;
    private static final int LARGE_SCREEN_WIDTH = 1920;

    private static final int TINY_SCREEN_HEIGHT = 480;
    private static final int SMALL_SCREEN_HEIGHT = 600;
    private static final int MEDIUM_SCREEN_HEIGHT = 720;
    private static final int LARGE_SCREEN_HEIGHT = 1080;

    private final int screenWidth;
    private final int screenHeight;
    private final ScreenSize screenSize;
    private final TextRenderer textRenderer;

    public enum ScreenSize {
        TINY,    // < 640x480
        SMALL,   // < 854x600
        MEDIUM,  // < 1280x720
        LARGE,   // < 1920x1080
        XLARGE   // >= 1920x1080
    }

    public GuiLayoutHelper(int screenWidth, int screenHeight) {
        this(screenWidth, screenHeight, null);
    }

    public GuiLayoutHelper(int screenWidth, int screenHeight, TextRenderer textRenderer) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.textRenderer = textRenderer;
        this.screenSize = determineScreenSize();
    }

    private ScreenSize determineScreenSize() {
        if (screenWidth < TINY_SCREEN_WIDTH || screenHeight < TINY_SCREEN_HEIGHT) {
            return ScreenSize.TINY;
        } else if (screenWidth < SMALL_SCREEN_WIDTH || screenHeight < SMALL_SCREEN_HEIGHT) {
            return ScreenSize.SMALL;
        } else if (screenWidth < MEDIUM_SCREEN_WIDTH || screenHeight < MEDIUM_SCREEN_HEIGHT) {
            return ScreenSize.MEDIUM;
        } else if (screenWidth < LARGE_SCREEN_WIDTH || screenHeight < LARGE_SCREEN_HEIGHT) {
            return ScreenSize.LARGE;
        } else {
            return ScreenSize.XLARGE;
        }
    }

    public ScreenSize getScreenSize() {
        return screenSize;
    }

    /**
     * Get a scaled value based on screen width percentage.
     *
     * @param percentage Percentage of screen width (0.0 to 1.0)
     * @return Scaled width value
     */
    public int getScaledWidth(double percentage) {
        return (int) (screenWidth * Math.max(0.0, Math.min(1.0, percentage)));
    }

    /**
     * Get a scaled value based on screen height percentage.
     *
     * @param percentage Percentage of screen height (0.0 to 1.0)
     * @return Scaled height value
     */
    public int getScaledHeight(double percentage) {
        return (int) (screenHeight * Math.max(0.0, Math.min(1.0, percentage)));
    }

    /**
     * Get button width based on screen size.
     *
     * @return Appropriate button width
     */
    public int getButtonWidth() {
        switch (screenSize) {
            case TINY:
                return Math.min(120, getScaledWidth(0.3));
            case SMALL:
                return Math.min(150, getScaledWidth(0.25));
            case MEDIUM:
                return Math.min(180, getScaledWidth(0.2));
            case LARGE:
            case XLARGE:
            default:
                return Math.min(200, getScaledWidth(0.15));
        }
    }

    /**
     * Get button height based on screen size.
     *
     * @return Appropriate button height
     */
    public int getButtonHeight() {
        switch (screenSize) {
            case TINY:
                return 18;
            case SMALL:
                return 20;
            case MEDIUM:
            case LARGE:
            case XLARGE:
            default:
                return 20;
        }
    }

    /**
     * Get small button width (for rating buttons, etc.)
     *
     * @return Appropriate small button width
     */
    public int getSmallButtonWidth() {
        switch (screenSize) {
            case TINY:
                return Math.min(60, getScaledWidth(0.12));
            case SMALL:
                return Math.min(70, getScaledWidth(0.1));
            case MEDIUM:
                return Math.min(80, getScaledWidth(0.08));
            case LARGE:
            case XLARGE:
            default:
                return Math.min(90, getScaledWidth(0.06));
        }
    }

    /**
     * Get spacing between elements based on screen size.
     *
     * @return Appropriate spacing
     */
    public int getSpacing() {
        switch (screenSize) {
            case TINY:
                return 6;
            case SMALL:
                return 8;
            case MEDIUM:
                return 10;
            case LARGE:
            case XLARGE:
            default:
                return 12;
        }
    }

    /**
     * Get small spacing between elements.
     *
     * @return Appropriate small spacing
     */
    public int getSmallSpacing() {
        return Math.max(4, getSpacing() / 2);
    }

    /**
     * Get margin from screen edges.
     *
     * @return Appropriate margin
     */
    public int getMargin() {
        switch (screenSize) {
            case TINY:
                return 8;
            case SMALL:
                return 10;
            case MEDIUM:
                return 15;
            case LARGE:
            case XLARGE:
            default:
                return 20;
        }
    }

    /**
     * Get vertical offset from top of screen for content start.
     *
     * @return Y position for content start
     */
    public int getContentStartY() {
        switch (screenSize) {
            case TINY:
                return getScaledHeight(0.08);
            case SMALL:
                return getScaledHeight(0.1);
            case MEDIUM:
            case LARGE:
            case XLARGE:
            default:
                return getScaledHeight(0.12);
        }
    }

    /**
     * Get title Y position.
     *
     * @return Y position for title
     */
    public int getTitleY() {
        return Math.max(10, getScaledHeight(0.04));
    }

    /**
     * Get centered X position for an element.
     *
     * @param elementWidth Width of the element
     * @return Centered X position
     */
    public int getCenterX(int elementWidth) {
        return (screenWidth - elementWidth) / 2;
    }

    /**
     * Get centered Y position for an element.
     *
     * @param elementHeight Height of the element
     * @return Centered Y position
     */
    public int getCenterY(int elementHeight) {
        return (screenHeight - elementHeight) / 2;
    }

    /**
     * Get Y position for bottom-aligned element.
     *
     * @param elementHeight Height of the element
     * @return Y position for bottom alignment
     */
    public int getBottomY(int elementHeight) {
        return screenHeight - elementHeight - getMargin();
    }

    /**
     * Calculate total width needed for multiple elements with spacing.
     *
     * @param elementWidth Width of each element
     * @param count        Number of elements
     * @param spacing      Spacing between elements
     * @return Total width needed
     */
    public int getTotalWidth(int elementWidth, int count, int spacing) {
        return (elementWidth * count) + (spacing * (count - 1));
    }

    /**
     * Get line height for text based on actual font height.
     * Falls back to screen-size-based values if textRenderer is not available.
     *
     * @return Line height
     */
    public int getLineHeight() {
        if (textRenderer != null) {
            // Use actual font height + small padding
            return textRenderer.fontHeight + 2;
        }

        // Fallback to screen-size-based values
        switch (screenSize) {
            case TINY:
                return 12;
            case SMALL:
                return 14;
            case MEDIUM:
            case LARGE:
            case XLARGE:
            default:
                return 16;
        }
    }

    /**
     * Get text scale factor based on screen size.
     *
     * @return Scale factor for text
     */
    public float getTextScale() {
        switch (screenSize) {
            case TINY:
                return 0.8f;
            case SMALL:
                return 0.9f;
            case MEDIUM:
            case LARGE:
            case XLARGE:
            default:
                return 1.0f;
        }
    }

    /**
     * Check if text fits within a given width.
     *
     * @param text         Text to check
     * @param textRenderer Text renderer
     * @param maxWidth     Maximum width
     * @return True if text fits
     */
    public boolean textFits(String text, TextRenderer textRenderer, int maxWidth) {
        return textRenderer.getWidth(text) <= maxWidth;
    }

    /**
     * Get maximum content width (screen width minus margins).
     *
     * @return Maximum content width
     */
    public int getMaxContentWidth() {
        return screenWidth - (getMargin() * 2);
    }

    /**
     * Get maximum content height (screen height minus margins and title area).
     *
     * @return Maximum content height
     */
    public int getMaxContentHeight() {
        return screenHeight - getContentStartY() - getMargin();
    }

    /**
     * Helper class for building horizontal button rows with automatic positioning.
     * Eliminates hardcoded position calculations.
     */
    public static class HorizontalRowBuilder {
        private final int startX;
        private final int y;
        private final int spacing;
        private int currentX;

        /**
         * Create a new horizontal row builder.
         *
         * @param startX  Starting X position
         * @param y       Y position for all elements in this row
         * @param spacing Spacing between elements
         */
        public HorizontalRowBuilder(int startX, int y, int spacing) {
            this.startX = startX;
            this.y = y;
            this.spacing = spacing;
            this.currentX = startX;
        }

        /**
         * Get the X position for the next element and advance the cursor.
         *
         * @param elementWidth Width of the element to place
         * @return X position for this element
         */
        public int nextX(int elementWidth) {
            int x = currentX;
            currentX += elementWidth + spacing;
            return x;
        }

        /**
         * Get the Y position for elements in this row.
         *
         * @return Y position
         */
        public int getY() {
            return y;
        }

        /**
         * Get the current X position (where the next element would be placed).
         *
         * @return Current X position
         */
        public int getCurrentX() {
            return currentX;
        }

        /**
         * Reset the cursor to the start position.
         */
        public void reset() {
            currentX = startX;
        }
    }

    /**
     * Helper class for building right-aligned button rows.
     * Calculates positions from right to left.
     */
    public static class RightAlignedRowBuilder {
        private final int endX;
        private final int y;
        private final int spacing;
        private int currentX;

        /**
         * Create a new right-aligned row builder.
         *
         * @param endX    Ending X position (right edge)
         * @param y       Y position for all elements in this row
         * @param spacing Spacing between elements
         */
        public RightAlignedRowBuilder(int endX, int y, int spacing) {
            this.endX = endX;
            this.y = y;
            this.spacing = spacing;
            this.currentX = endX;
        }

        /**
         * Get the X position for the next element (from right to left) and advance the cursor.
         *
         * @param elementWidth Width of the element to place
         * @return X position for this element
         */
        public int nextX(int elementWidth) {
            currentX -= elementWidth;
            int x = currentX;
            currentX -= spacing;
            return x;
        }

        /**
         * Get the Y position for elements in this row.
         *
         * @return Y position
         */
        public int getY() {
            return y;
        }

        /**
         * Get the current X position (where the next element would be placed).
         *
         * @return Current X position
         */
        public int getCurrentX() {
            return currentX;
        }

        /**
         * Reset the cursor to the end position.
         */
        public void reset() {
            currentX = endX;
        }
    }

    /**
     * Create a horizontal row builder starting from the left margin.
     *
     * @param y Y position for the row
     * @return HorizontalRowBuilder instance
     */
    public HorizontalRowBuilder createLeftRow(int y) {
        return new HorizontalRowBuilder(getMargin(), y, getSmallSpacing());
    }

    /**
     * Create a horizontal row builder starting from a custom X position.
     *
     * @param startX Starting X position
     * @param y      Y position for the row
     * @return HorizontalRowBuilder instance
     */
    public HorizontalRowBuilder createRow(int startX, int y) {
        return new HorizontalRowBuilder(startX, y, getSmallSpacing());
    }

    /**
     * Create a right-aligned row builder ending at the right margin.
     *
     * @param y Y position for the row
     * @return RightAlignedRowBuilder instance
     */
    public RightAlignedRowBuilder createRightRow(int y) {
        return new RightAlignedRowBuilder(screenWidth - getMargin(), y, getSmallSpacing());
    }

    /**
     * Create a right-aligned row builder ending at a custom X position.
     *
     * @param endX Ending X position
     * @param y    Y position for the row
     * @return RightAlignedRowBuilder instance
     */
    public RightAlignedRowBuilder createRightRow(int endX, int y) {
        return new RightAlignedRowBuilder(endX, y, getSmallSpacing());
    }

    /**
     * Helper class for building complete rows with text on left and buttons on right.
     * Automatically calculates safe positions to prevent overlaps.
     */
    public static class RowLayout {
        private final int y;
        private final int leftX;
        private final int spacing;
        private int currentRightX;

        /**
         * Create a new row layout.
         *
         * @param leftX   Left edge X position (for text)
         * @param rightX  Right edge X position (for buttons)
         * @param y       Y position for this row
         * @param spacing Spacing between elements
         */
        public RowLayout(int leftX, int rightX, int y, int spacing) {
            this.leftX = leftX;
            this.y = y;
            this.spacing = spacing;
            this.currentRightX = rightX;
        }

        /**
         * Get the Y position for this row.
         *
         * @return Y position
         */
        public int getY() {
            return y;
        }

        /**
         * Get the left X position for text.
         *
         * @return Left X position
         */
        public int getLeftX() {
            return leftX;
        }

        /**
         * Add a button from right to left and return its X position.
         *
         * @param buttonWidth Width of the button
         * @return X position for this button
         */
        public int addButtonFromRight(int buttonWidth) {
            currentRightX -= buttonWidth;
            int x = currentRightX;
            currentRightX -= spacing;
            return x;
        }

        /**
         * Get the maximum width available for text (left side).
         * Call this after adding all buttons to get safe text width.
         *
         * @return Maximum text width that won't overlap with buttons
         */
        public int getMaxTextWidth() {
            return Math.max(0, currentRightX - leftX - spacing);
        }

        /**
         * Get the current right X position (where next button would start).
         *
         * @return Current right X
         */
        public int getCurrentRightX() {
            return currentRightX;
        }
    }

    /**
     * Create a row layout for text + buttons pattern.
     *
     * @param y Y position for the row
     * @return RowLayout instance
     */
    public RowLayout createRowLayout(int y) {
        return new RowLayout(getMargin(), screenWidth - getMargin(), y, getSmallSpacing());
    }

    /**
     * Create a row layout with custom left and right bounds.
     *
     * @param leftX  Left edge X position
     * @param rightX Right edge X position
     * @param y      Y position for the row
     * @return RowLayout instance
     */
    public RowLayout createRowLayout(int leftX, int rightX, int y) {
        return new RowLayout(leftX, rightX, y, getSmallSpacing());
    }

    /**
     * Truncate text to fit within a maximum width.
     *
     * @param text         Text to truncate
     * @param textRenderer Text renderer
     * @param maxWidth     Maximum width
     * @return Truncated text with "..." if needed
     */
    public String truncateText(String text, TextRenderer textRenderer, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = textRenderer.getWidth(ellipsis);
        int availableWidth = maxWidth - ellipsisWidth;

        if (availableWidth <= 0) {
            return ellipsis;
        }

        // Binary search for the right length
        int left = 0;
        int right = text.length();
        int bestLength = 0;

        while (left <= right) {
            int mid = (left + right) / 2;
            String substring = text.substring(0, mid);
            int width = textRenderer.getWidth(substring);

            if (width <= availableWidth) {
                bestLength = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return text.substring(0, bestLength) + ellipsis;
    }

    /**
     * Get the width of text as rendered.
     *
     * @param text         Text to measure
     * @param textRenderer Text renderer
     * @return Width in pixels
     */
    public int getTextWidth(String text, TextRenderer textRenderer) {
        return textRenderer.getWidth(text);
    }

    /**
     * Get spacing between label and field (vertical).
     *
     * @return Label-to-field spacing
     */
    public int getLabelFieldSpacing() {
        switch (screenSize) {
            case TINY:
                return 8;
            case SMALL:
                return 10;
            case MEDIUM:
            case LARGE:
            case XLARGE:
            default:
                return 12;
        }
    }

    /**
     * Helper class for building vertical form layouts with labels and fields.
     * Automatically calculates proper spacing to prevent overlaps.
     */
    public static class VerticalFormBuilder {
        private final int leftX;
        private final int maxWidth;
        private final int labelFieldSpacing;
        private final int fieldSpacing;
        private final int buttonHeight;
        private int currentY;

        /**
         * Create a new vertical form builder.
         *
         * @param leftX             Left X position for all elements
         * @param startY            Starting Y position
         * @param maxWidth          Maximum width for fields
         * @param labelFieldSpacing Spacing between label and field
         * @param fieldSpacing      Spacing between fields
         * @param buttonHeight      Height of buttons/fields
         */
        public VerticalFormBuilder(int leftX, int startY, int maxWidth, int labelFieldSpacing, int fieldSpacing, int buttonHeight) {
            this.leftX = leftX;
            this.currentY = startY;
            this.maxWidth = maxWidth;
            this.labelFieldSpacing = labelFieldSpacing;
            this.fieldSpacing = fieldSpacing;
            this.buttonHeight = buttonHeight;
        }

        /**
         * Get the X position for elements.
         *
         * @return X position
         */
        public int getX() {
            return leftX;
        }

        /**
         * Get the current Y position and advance for a label.
         *
         * @return Y position for the label
         */
        public int nextLabelY() {
            int y = currentY;
            currentY += labelFieldSpacing;
            return y;
        }

        /**
         * Get the current Y position and advance for a field.
         *
         * @param fieldHeight Height of the field (default is buttonHeight)
         * @return Y position for the field
         */
        public int nextFieldY(int fieldHeight) {
            int y = currentY;
            currentY += fieldHeight + fieldSpacing;
            return y;
        }

        /**
         * Get the current Y position and advance for a standard field.
         *
         * @return Y position for the field
         */
        public int nextFieldY() {
            return nextFieldY(buttonHeight);
        }

        /**
         * Get the maximum width for fields.
         *
         * @return Maximum field width
         */
        public int getMaxWidth() {
            return maxWidth;
        }

        /**
         * Get the current Y position.
         *
         * @return Current Y position
         */
        public int getCurrentY() {
            return currentY;
        }

        /**
         * Add extra spacing.
         *
         * @param spacing Amount of spacing to add
         */
        public void addSpacing(int spacing) {
            currentY += spacing;
        }
    }

    /**
     * Create a vertical form builder for label+field layouts.
     *
     * @param startY Starting Y position
     * @return VerticalFormBuilder instance
     */
    public VerticalFormBuilder createFormBuilder(int startY) {
        int maxFieldWidth = Math.min(500, getMaxContentWidth());
        return new VerticalFormBuilder(
                getMargin(),
                startY,
                maxFieldWidth,
                getLabelFieldSpacing(),
                getSpacing(),
                getButtonHeight()
        );
    }
}

