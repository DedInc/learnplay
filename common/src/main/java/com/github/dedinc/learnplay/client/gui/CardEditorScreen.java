package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.data.model.Flashcard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Screen for editing individual flashcards.
 * Cards belong to a Deck, which belongs to a Category - no need for redundant category fields.
 */
public class CardEditorScreen extends Screen {
    private final Screen parent;
    private final Deck deck;
    private final Flashcard card;
    private final boolean isNewCard;
    private GuiLayoutHelper layoutHelper;

    private TextFieldWidget idField;
    private TextFieldWidget questionField;
    private TextFieldWidget answerField;

    public CardEditorScreen(Screen parent, Deck deck, Flashcard card, boolean isNewCard) {
        super(Text.literal("Edit Card"));
        this.parent = parent;
        this.deck = deck;
        this.card = card;
        this.isNewCard = isNewCard;
    }

    @Override
    protected void init() {
        super.init();

        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);

        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSpacing();

        // Limit field width to reasonable size
        int maxFieldWidth = 500;
        int fieldWidth = Math.min(maxFieldWidth, this.width - margin * 2);

        int currentY = layoutHelper.getTitleY() + layoutHelper.getLineHeight() + spacing * 2;

        // Card ID label (rendered in render() method)
        currentY += layoutHelper.getLineHeight();

        // Card ID field
        idField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Card ID"));
        idField.setMaxLength(50);
        idField.setText(card.getId());
        idField.setEditable(isNewCard);
        addDrawableChild(idField);
        currentY += buttonHeight + spacing * 2;

        // Question label (rendered in render() method)
        currentY += layoutHelper.getLineHeight();

        // Question field (multiline simulation with larger field)
        questionField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight * 2, Text.literal("Question"));
        questionField.setMaxLength(500);
        questionField.setText(card.getQuestion());
        addDrawableChild(questionField);
        currentY += buttonHeight * 2 + spacing * 2;

        // Answer label (rendered in render() method)
        currentY += layoutHelper.getLineHeight();

        // Answer field (multiline simulation with larger field)
        answerField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight * 2, Text.literal("Answer"));
        answerField.setMaxLength(500);
        answerField.setText(card.getAnswer());
        addDrawableChild(answerField);
        currentY += buttonHeight * 2 + spacing;

        // Buttons at bottom using layout helper
        int buttonWidth = 120;
        int bottomY = layoutHelper.getBottomY(buttonHeight);
        GuiLayoutHelper.HorizontalRowBuilder bottomRow = layoutHelper.createLeftRow(bottomY);

        // Save button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Save"),
                button -> saveCard()
        ).dimensions(bottomRow.nextX(buttonWidth), bottomRow.getY(), buttonWidth, buttonHeight).build());

        // Cancel button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> close()
        ).dimensions(bottomRow.nextX(buttonWidth), bottomRow.getY(), buttonWidth, buttonHeight).build());
    }

    private void saveCard() {
        String newId = idField.getText().trim();
        String newQuestion = questionField.getText().trim();
        String newAnswer = answerField.getText().trim();

        if (newId.isEmpty() || newQuestion.isEmpty() || newAnswer.isEmpty()) {
            // Show error - for now just return
            return;
        }

        // Create new flashcard with updated values
        Flashcard updatedCard = new Flashcard(newId, newQuestion, newAnswer);

        if (isNewCard) {
            deck.addCard(updatedCard);
        } else {
            // Remove old and add new
            deck.removeCard(card.getId());
            deck.addCard(updatedCard);
        }

        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int margin = layoutHelper.getMargin();
        int centerX = this.width / 2;
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSpacing();

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit Flashcard", centerX, layoutHelper.getTitleY(), 0xFFFFFF);

        // Labels - positioned ABOVE the fields with proper spacing
        int currentY = layoutHelper.getTitleY() + layoutHelper.getLineHeight() + spacing * 2;

        // Card ID label
        context.drawTextWithShadow(this.textRenderer, "Card ID:", margin, currentY, 0xAAAAAA);
        currentY += layoutHelper.getLineHeight();
        currentY += buttonHeight + spacing * 2;

        // Question label
        context.drawTextWithShadow(this.textRenderer, "Question:", margin, currentY, 0xAAAAAA);
        currentY += layoutHelper.getLineHeight();
        currentY += buttonHeight * 2 + spacing * 2;

        // Answer label
        context.drawTextWithShadow(this.textRenderer, "Answer:", margin, currentY, 0xAAAAAA);

        // Help text
        String helpText = "Fill in all required fields and click Save";
        int helpTextWidth = this.textRenderer.getWidth(helpText);
        int helpTextY = layoutHelper.getBottomY(buttonHeight + 25);
        context.drawText(this.textRenderer, helpText,
                centerX - helpTextWidth / 2, helpTextY, 0x808080, false);
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

