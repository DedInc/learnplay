package com.github.dedinc.learnplay.client.gui.browser;

import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.storage.CategoryManager;
import com.github.dedinc.learnplay.storage.DeckManager;

import java.util.List;

/**
 * Loads data for the browser screen (categories and decks).
 */
public class BrowserDataLoader {

    private final CategoryManager categoryManager;
    private final DeckManager deckManager;

    public BrowserDataLoader() {
        this.categoryManager = CategoryManager.getInstance();
        this.deckManager = DeckManager.getInstance();
    }

    /**
     * Load items for a given category (or root if null).
     */
    public BrowserData loadData(Category currentCategory) {
        List<Category> categories;
        List<Deck> decks;

        if (currentCategory == null) {
            // Root level - show top-level categories and uncategorized decks
            categories = categoryManager.getTopLevelCategories();
            decks = deckManager.getUncategorizedDecks();
        } else {
            // Inside a category - show subcategories and decks in this category
            categories = categoryManager.getSubcategories(currentCategory.getId());
            decks = deckManager.getDecksByCategory(currentCategory.getId());
        }

        return new BrowserData(categories, decks);
    }

    /**
     * Data container for browser items.
     */
    public static class BrowserData {
        public final List<Category> categories;
        public final List<Deck> decks;

        public BrowserData(List<Category> categories, List<Deck> decks) {
            this.categories = categories;
            this.decks = decks;
        }
    }
}

