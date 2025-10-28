package com.github.dedinc.learnplay.client.gui.editor;

import com.github.dedinc.learnplay.client.gui.GuiLayoutHelper;
import com.github.dedinc.learnplay.data.model.Flashcard;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Panel for rendering and managing a list of flashcards.
 */
public class CardListPanel {

    private final GuiLayoutHelper layoutHelper;
    private final TextRenderer textRenderer;
    private final int rowHeight;

    public CardListPanel(GuiLayoutHelper layoutHelper, TextRenderer textRenderer, int rowHeight) {
        this.layoutHelper = layoutHelper;
        this.textRenderer = textRenderer;
        this.rowHeight = rowHeight;
    }

    /**
     * Build card list widgets.
     *
     * @param cards        List of cards to display
     * @param startY       Starting Y position
     * @param scrollOffset Current scroll offset
     * @param screenHeight Screen height for visibility check
     * @param onEdit       Callback when edit button is clicked (card, isNew)
     * @param onDelete     Callback when delete button is clicked
     * @return List of widgets to add to screen
     */
    public List<ButtonWidget> buildCardWidgets(
            List<Flashcard> cards,
            int startY,
            double scrollOffset,
            int screenHeight,
            BiConsumer<Flashcard, Boolean> onEdit,
            Runnable onDelete
    ) {
        List<ButtonWidget> widgets = new ArrayList<>();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSmallSpacing();
        int currentY = startY;

        for (Flashcard card : cards) {
            int adjustedY = (int) (currentY - scrollOffset);

            // Skip if not visible
            if (adjustedY < startY - 50 || adjustedY > screenHeight - 150) {
                currentY += rowHeight + spacing;
                continue;
            }

            // Use RowLayout to properly position buttons from right to left
            GuiLayoutHelper.RowLayout rowLayout = layoutHelper.createRowLayout(adjustedY);

            int deleteButtonWidth = 70;
            int deleteX = rowLayout.addButtonFromRight(deleteButtonWidth);

            int editButtonWidth = 60;
            int editX = rowLayout.addButtonFromRight(editButtonWidth);

            // Edit button
            widgets.add(ButtonWidget.builder(
                    Text.literal("Edit"),
                    btn -> onEdit.accept(card, false)
            ).dimensions(editX, adjustedY, editButtonWidth, buttonHeight).build());

            // Delete button
            widgets.add(ButtonWidget.builder(
                    Text.literal("Delete"),
                    btn -> onDelete.run()
            ).dimensions(deleteX, adjustedY, deleteButtonWidth, buttonHeight).build());

            currentY += rowHeight + spacing;
        }

        return widgets;
    }

    /**
     * Render card list text.
     *
     * @param context      Draw context
     * @param cards        List of cards to display
     * @param startY       Starting Y position
     * @param scrollOffset Current scroll offset
     * @param screenHeight Screen height for visibility check
     */
    public void renderCardList(
            DrawContext context,
            List<Flashcard> cards,
            int startY,
            double scrollOffset,
            int screenHeight
    ) {
        int margin = layoutHelper.getMargin();
        int spacing = layoutHelper.getSmallSpacing();
        int currentY = startY;

        for (Flashcard card : cards) {
            int adjustedY = (int) (currentY - scrollOffset);

            if (adjustedY >= startY - 50 && adjustedY <= screenHeight - 150) {
                // Calculate max text width to prevent overlap with buttons
                GuiLayoutHelper.RowLayout rowLayout = layoutHelper.createRowLayout(adjustedY);
                rowLayout.addButtonFromRight(70); // Delete button
                rowLayout.addButtonFromRight(60); // Edit button
                int maxTextWidth = rowLayout.getMaxTextWidth();

                String question = "📄 " + card.getQuestion();
                question = layoutHelper.truncateText(question, textRenderer, maxTextWidth);

                context.drawTextWithShadow(textRenderer, question, margin, adjustedY + 5, 0xFFFFFF);
            }

            currentY += rowHeight + spacing;
        }
    }
}

