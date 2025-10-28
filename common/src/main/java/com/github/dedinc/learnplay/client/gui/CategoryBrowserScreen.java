package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.client.gui.browser.*;
import com.github.dedinc.learnplay.client.gui.widgets.ScrollableListWidget;
import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.storage.CategoryManager;
import com.github.dedinc.learnplay.storage.DeckManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for browsing categories and decks in a hierarchical folder structure.
 * Categories act as folders that contain decks and subcategories.
 */
public class CategoryBrowserScreen extends Screen {
    private final Screen parent;
    private Category currentCategory; // null = root level
    private GuiLayoutHelper layoutHelper;
    private ScrollableListWidget scrollWidget;
    private BrowserItemRenderer itemRenderer;
    private BrowserButtonBuilder buttonBuilder;

    private static final int ROW_HEIGHT = 24;
    private static final double SCROLL_SPEED = 10.0;

    private final List<BrowserItemRow> itemRows = new ArrayList<>();

    public CategoryBrowserScreen(Screen parent) {
        this(parent, null);
    }

    public CategoryBrowserScreen(Screen parent, Category currentCategory) {
        super(Text.literal(currentCategory == null ? "Browse Categories" : currentCategory.getName()));
        this.parent = parent;
        this.currentCategory = currentCategory;
    }

    @Override
    protected void init() {
        super.init();

        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);
        scrollWidget = new ScrollableListWidget(layoutHelper, ROW_HEIGHT, SCROLL_SPEED);
        itemRenderer = new BrowserItemRenderer(layoutHelper, this.textRenderer);
        buttonBuilder = new BrowserButtonBuilder(layoutHelper);
        itemRows.clear();

        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSpacing();

        // Title area
        int currentY = margin + 20;

        // Get items to display
        BrowserDataLoader dataLoader = new BrowserDataLoader();
        BrowserDataLoader.BrowserData data = dataLoader.loadData(currentCategory);

        // Create rows for categories and decks
        int rowY = currentY + 30;
        for (Category category : data.categories) {
            itemRows.add(new BrowserItemRow(category, rowY));
            rowY += ROW_HEIGHT + spacing;
        }

        for (Deck deck : data.decks) {
            itemRows.add(new BrowserItemRow(deck, rowY));
            rowY += ROW_HEIGHT + spacing;
        }

        // Add bottom navigation buttons
        addBottomButtons(buttonHeight, spacing);

        // Initialize item row widgets
        updateItemRowWidgets();
    }

    private void addBottomButtons(int buttonHeight, int spacing) {
        int bottomRowY = layoutHelper.getBottomY(buttonHeight * 2 + spacing);
        GuiLayoutHelper.HorizontalRowBuilder bottomRow = layoutHelper.createLeftRow(bottomRowY);

        // Add Category button
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Category"), button -> {
            Category newCategory = new Category("cat_" + System.currentTimeMillis(), "New Category");
            if (currentCategory != null) newCategory.setParentCategoryId(currentCategory.getId());
            MinecraftClient.getInstance().setScreen(new CategoryEditorScreen(this, newCategory, true));
        }).dimensions(bottomRow.nextX(120), bottomRow.getY(), 120, buttonHeight).build());

        // Add Deck button
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Deck"), button -> {
            Deck newDeck = new Deck("deck_" + System.currentTimeMillis(), "New Deck");
            if (currentCategory != null) newDeck.setCategoryId(currentCategory.getId());
            MinecraftClient.getInstance().setScreen(new DeckEditorScreen(this, newDeck, true));
        }).dimensions(bottomRow.nextX(120), bottomRow.getY(), 120, buttonHeight).build());

        // Reload button
        addDrawableChild(ButtonWidget.builder(Text.literal("🔄 Reload"), button -> {
            CategoryManager.getInstance().reloadCategories();
            DeckManager.getInstance().reload();
            clearChildren();
            init();
        }).dimensions(bottomRow.nextX(100), bottomRow.getY(), 100, buttonHeight).build());

        // Back/Done button
        String backButtonText = currentCategory == null ? "Done" : "Back";
        addDrawableChild(ButtonWidget.builder(Text.literal(backButtonText), button -> {
            if (currentCategory == null) {
                close();
            } else if (currentCategory.getParentCategoryId() != null) {
                MinecraftClient.getInstance().setScreen(new CategoryBrowserScreen(parent,
                        CategoryManager.getInstance().getCategory(currentCategory.getParentCategoryId())));
            } else {
                MinecraftClient.getInstance().setScreen(new CategoryBrowserScreen(parent, null));
            }
        }).dimensions(layoutHelper.getCenterX(100), layoutHelper.getBottomY(buttonHeight), 100, buttonHeight).build());
    }

    private void updateItemRowWidgets() {
        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();

        for (BrowserItemRow row : itemRows) {
            int adjustedY = scrollWidget.getAdjustedY(row.y);

            // Skip if not visible
            if (!scrollWidget.isVisible(adjustedY, margin + 30, this.height - 100)) {
                continue;
            }

            // Use RowLayout to properly position buttons from right to left
            GuiLayoutHelper.RowLayout rowLayout = layoutHelper.createRowLayout(adjustedY);

            if (row.category != null) {
                addCategoryButtons(row.category, adjustedY, buttonHeight, rowLayout);
            } else if (row.deck != null) {
                addDeckButtons(row.deck, adjustedY, buttonHeight, rowLayout);
            }
        }
    }

    private void addCategoryButtons(Category category, int y, int buttonHeight, GuiLayoutHelper.RowLayout rowLayout) {
        List<ButtonWidget> buttons = buttonBuilder.createCategoryButtons(
                category, y, buttonHeight, rowLayout,
                cat -> MinecraftClient.getInstance().setScreen(new CategoryBrowserScreen(parent, cat)),
                cat -> MinecraftClient.getInstance().setScreen(new CategoryEditorScreen(this, cat, false)),
                cat -> {
                    CategoryManager.getInstance().deleteCategory(cat.getId());
                    clearChildren();
                    init();
                }
        );
        buttons.forEach(this::addDrawableChild);
    }

    private void addDeckButtons(Deck deck, int y, int buttonHeight, GuiLayoutHelper.RowLayout rowLayout) {
        List<ButtonWidget> buttons = buttonBuilder.createDeckButtons(
                deck, y, buttonHeight, rowLayout,
                d -> {
                    d.setEnabled(!d.isEnabled());
                    DeckManager.getInstance().saveDeck(d);
                    clearChildren();
                    init();
                },
                d -> MinecraftClient.getInstance().setScreen(new DeckEditorScreen(this, d, false)),
                d -> {
                    DeckManager.getInstance().deleteDeck(d.getId());
                    clearChildren();
                    init();
                }
        );
        buttons.forEach(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int margin = layoutHelper.getMargin();
        int centerX = this.width / 2;

        // Title and breadcrumb
        String title = currentCategory == null ? "Browse Categories & Decks" : currentCategory.getName();
        context.drawCenteredTextWithShadow(this.textRenderer, title, centerX, margin + 5, 0xFFFFFF);
        if (currentCategory != null) {
            context.drawTextWithShadow(this.textRenderer, "📁 " + BreadcrumbBuilder.buildBreadcrumb(currentCategory),
                    margin, margin + 25, 0xAAAAAA);
        }

        // Render items
        int currentY = margin + 50;
        for (BrowserItemRow row : itemRows) {
            int adjustedY = scrollWidget.getAdjustedY(currentY);
            if (scrollWidget.isVisible(adjustedY, margin + 30, this.height - 100)) {
                int maxTextWidth = itemRenderer.calculateMaxTextWidth(adjustedY, 60, 60, 80);
                if (row.category != null) {
                    itemRenderer.renderCategory(context, row.category, margin, adjustedY + 5, maxTextWidth);
                } else if (row.deck != null) {
                    itemRenderer.renderDeck(context, row.deck, margin, adjustedY + 5, maxTextWidth);
                }
            }
            currentY += ROW_HEIGHT + layoutHelper.getSmallSpacing();
        }

        // Help text
        if (itemRows.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    "No items. Click '+ Category' or '+ Deck' to add.", centerX, this.height / 2, 0xAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollWidget.handleScroll(verticalAmount, itemRows.size(), this.height, 150);

        // Rebuild widgets with new scroll position
        clearChildren();
        init();

        return true;
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

