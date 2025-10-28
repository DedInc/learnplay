package com.github.dedinc.learnplay.client.gui.config;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for creating configuration setting rows with labels and input fields.
 */
public class ConfigSettingBuilder {

    private final TextRenderer textRenderer;
    private final List<LabelData> labels;

    public ConfigSettingBuilder(TextRenderer textRenderer, List<LabelData> labels) {
        this.textRenderer = textRenderer;
        this.labels = labels;
    }

    /**
     * Create a text field setting row (for numeric values).
     */
    public TextFieldWidget createTextFieldSetting(
            String labelText,
            int x,
            int y,
            int columnWidth,
            int height,
            String initialValue,
            Consumer<String> onChange
    ) {
        return createTextFieldSetting(labelText, x, y, columnWidth, height, initialValue, onChange, 5, columnWidth / 3);
    }

    /**
     * Create a text field setting row with custom max length and width.
     */
    public TextFieldWidget createTextFieldSetting(
            String labelText,
            int x,
            int y,
            int columnWidth,
            int height,
            String initialValue,
            Consumer<String> onChange,
            int maxLength,
            int fieldWidth
    ) {
        // Add label - vertically center it with the text field
        // Calculate vertical offset to center text within the button height
        int fontHeight = textRenderer.fontHeight;
        int labelY = y + (height - fontHeight) / 2;
        labels.add(new LabelData(labelText, x + 2, labelY, false));

        // Create text field
        int textFieldWidth = Math.min(fieldWidth, columnWidth / 2);
        TextFieldWidget textField = new TextFieldWidget(
                textRenderer,
                x + columnWidth - textFieldWidth,
                y,
                textFieldWidth,
                height,
                Text.literal(labelText)
        );
        textField.setMaxLength(maxLength);
        textField.setText(initialValue);
        textField.setChangedListener(onChange::accept);

        return textField;
    }

    /**
     * Add a centered label (section header).
     */
    public void addCenteredLabel(String text, int centerX, int y) {
        labels.add(new LabelData(text, centerX, y, true));
    }

    /**
     * Parse integer safely with bounds checking.
     */
    public static int parseIntSafe(String value, int defaultValue, int min, int max) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(min, Math.min(max, parsed));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Data class for storing label information.
     */
    public static class LabelData {
        public final String text;
        public final int x;
        public final int y;
        public final boolean isCentered;

        public LabelData(String text, int x, int y, boolean isCentered) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.isCentered = isCentered;
        }
    }
}

