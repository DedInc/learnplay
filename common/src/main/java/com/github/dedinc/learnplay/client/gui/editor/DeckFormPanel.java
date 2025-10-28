package com.github.dedinc.learnplay.client.gui.editor;

import com.github.dedinc.learnplay.client.gui.GuiLayoutHelper;
import com.github.dedinc.learnplay.data.model.Deck;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel for editing deck properties (ID, name, description).
 * Note: Enabled status is controlled via the ON/OFF toggle in the deck list.
 */
public class DeckFormPanel {

    private final TextRenderer textRenderer;
    private final GuiLayoutHelper layoutHelper;
    private final Deck deck;
    private final boolean isNewDeck;

    private TextFieldWidget idField;
    private TextFieldWidget nameField;
    private TextFieldWidget descriptionField;

    private final List<LabelData> labels = new ArrayList<>();

    public DeckFormPanel(TextRenderer textRenderer, GuiLayoutHelper layoutHelper, Deck deck, boolean isNewDeck) {
        this.textRenderer = textRenderer;
        this.layoutHelper = layoutHelper;
        this.deck = deck;
        this.isNewDeck = isNewDeck;
    }

    /**
     * Build form fields and return them as a list.
     *
     * @param startY     Starting Y position
     * @param fieldWidth Width of text fields
     * @param onToggle   Callback when toggle button is clicked
     * @return List of widgets to add to screen
     */
    public FormResult buildForm(int startY, int fieldWidth, Runnable onToggle) {
        labels.clear();
        List<Object> widgets = new ArrayList<>();

        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSpacing();
        int labelSpacing = 12;

        int currentY = startY;

        // Deck ID field
        labels.add(new LabelData("Deck ID:", margin, currentY));
        currentY += labelSpacing;

        idField = new TextFieldWidget(textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Deck ID"));
        idField.setMaxLength(50);
        idField.setText(deck.getId());
        idField.setEditable(isNewDeck);
        widgets.add(idField);
        currentY += buttonHeight + spacing;

        // Deck name field
        labels.add(new LabelData("Deck Name:", margin, currentY));
        currentY += labelSpacing;

        nameField = new TextFieldWidget(textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Deck Name"));
        nameField.setMaxLength(100);
        nameField.setText(deck.getName());
        widgets.add(nameField);
        currentY += buttonHeight + spacing;

        // Description field
        labels.add(new LabelData("Description:", margin, currentY));
        currentY += labelSpacing;

        descriptionField = new TextFieldWidget(textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Description"));
        descriptionField.setMaxLength(200);
        descriptionField.setText(deck.getDescription());
        widgets.add(descriptionField);
        currentY += buttonHeight + spacing * 2;

        // Cards label
        labels.add(new LabelData("Cards:", margin, currentY));
        currentY += labelSpacing + 8;

        return new FormResult(widgets, labels, currentY);
    }

    /**
     * Validate and get form data.
     *
     * @return FormData if valid, null otherwise
     */
    public FormData getFormData() {
        String newId = idField.getText().trim();
        String newName = nameField.getText().trim();
        String newDesc = descriptionField.getText().trim();

        if (newId.isEmpty() || newName.isEmpty()) {
            return null;
        }

        return new FormData(newId, newName, newDesc);
    }

    /**
     * Result of building the form.
     */
    public static class FormResult {
        public final List<Object> widgets;
        public final List<LabelData> labels;
        public final int nextY;

        public FormResult(List<Object> widgets, List<LabelData> labels, int nextY) {
            this.widgets = widgets;
            this.labels = labels;
            this.nextY = nextY;
        }
    }

    /**
     * Form data extracted from fields.
     */
    public static class FormData {
        public final String id;
        public final String name;
        public final String description;

        public FormData(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    /**
     * Label data for rendering.
     */
    public static class LabelData {
        public final String text;
        public final int x;
        public final int y;

        public LabelData(String text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }
}

