package com.github.dedinc.learnplay.client.gui.browser;

import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.data.model.Deck;

/**
 * Represents a row in the browser screen (either a category or a deck).
 */
public class BrowserItemRow {
    public final Category category;
    public final Deck deck;
    public final int y;

    public BrowserItemRow(Category category, int y) {
        this.category = category;
        this.deck = null;
        this.y = y;
    }

    public BrowserItemRow(Deck deck, int y) {
        this.category = null;
        this.deck = deck;
        this.y = y;
    }

    public boolean isCategory() {
        return category != null;
    }

    public boolean isDeck() {
        return deck != null;
    }
}

