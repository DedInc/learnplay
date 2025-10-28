package com.github.dedinc.learnplay.client.gui.browser;

import com.github.dedinc.learnplay.client.gui.GuiLayoutHelper;
import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.storage.CategoryManager;
import com.github.dedinc.learnplay.storage.DeckManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Handles rendering of category and deck items in the browser.
 */
public class BrowserItemRenderer {

    private final GuiLayoutHelper layoutHelper;
    private final TextRenderer textRenderer;

    public BrowserItemRenderer(GuiLayoutHelper layoutHelper, TextRenderer textRenderer) {
        this.layoutHelper = layoutHelper;
        this.textRenderer = textRenderer;
    }

    /**
     * Render a category item.
     */
    public void renderCategory(DrawContext context, Category category, int x, int y, int maxTextWidth) {
        CategoryManager categoryManager = CategoryManager.getInstance();
        DeckManager deckManager = DeckManager.getInstance();

        int deckCount = deckManager.getDecksByCategory(category.getId()).size();
        int subcategoryCount = categoryManager.getSubcategories(category.getId()).size();
        String text = "📁 " + category.getName() + " (" + deckCount + " decks, " + subcategoryCount + " subcategories)";

        // Truncate text if needed
        text = layoutHelper.truncateText(text, textRenderer, maxTextWidth);
        context.drawTextWithShadow(textRenderer, text, x, y, 0xFFFF55);
    }

    /**
     * Render a deck item.
     */
    public void renderDeck(DrawContext context, Deck deck, int x, int y, int maxTextWidth) {
        String text = "📚 " + deck.getName() + " (" + deck.getCardCount() + " cards)";

        // Truncate text if needed
        text = layoutHelper.truncateText(text, textRenderer, maxTextWidth);
        int color = deck.isEnabled() ? 0xFFFFFF : 0x888888;
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    /**
     * Calculate max text width for a row with buttons.
     */
    public int calculateMaxTextWidth(int adjustedY, int... buttonWidths) {
        GuiLayoutHelper.RowLayout rowLayout = layoutHelper.createRowLayout(adjustedY);
        for (int width : buttonWidths) {
            rowLayout.addButtonFromRight(width);
        }
        return rowLayout.getMaxTextWidth();
    }
}

