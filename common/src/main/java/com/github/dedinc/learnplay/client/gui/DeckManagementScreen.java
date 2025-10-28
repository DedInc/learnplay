package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.storage.DeckManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Main deck management screen.
 * Allows viewing, enabling/disabling, and editing decks.
 */
public class DeckManagementScreen extends Screen {
    private final Screen parent;
    private GuiLayoutHelper layoutHelper;
    private double scrollOffset = 0;
    private static final int ROW_HEIGHT = 30;
    private static final int SCROLL_SPEED = 10;

    private List<DeckRow> deckRows = new ArrayList<>();

    public DeckManagementScreen(Screen parent) {
        super(Text.literal("Deck Management"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);
        deckRows.clear();

        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSpacing();

        // Title area
        int currentY = margin + 20;

        // Get all decks
        DeckManager manager = DeckManager.getInstance();
        List<Deck> decks = new ArrayList<>(manager.getAllDecks());

        // Create rows for each deck
        int rowY = currentY + 30;
        for (Deck deck : decks) {
            DeckRow row = new DeckRow(deck, rowY);
            deckRows.add(row);
            rowY += ROW_HEIGHT + spacing;
        }

        // Bottom button row - using layout helper for automatic positioning
        int addButtonWidth = 150;
        int bottomRowY = layoutHelper.getBottomY(buttonHeight * 2 + spacing);
        GuiLayoutHelper.HorizontalRowBuilder bottomRow = layoutHelper.createLeftRow(bottomRowY);

        // Add deck button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("+ Add New Deck"),
                button -> {
                    // Create a new empty deck
                    Deck newDeck = new Deck("new_deck_" + System.currentTimeMillis(), "New Deck");
                    MinecraftClient.getInstance().setScreen(new DeckEditorScreen(this, newDeck, true));
                }
        ).dimensions(bottomRow.nextX(addButtonWidth), bottomRow.getY(), addButtonWidth, buttonHeight).build());

        // Reload button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Reload Decks"),
                button -> {
                    DeckManager.getInstance().reload();
                    clearChildren();
                    init();
                }
        ).dimensions(bottomRow.nextX(addButtonWidth), bottomRow.getY(), addButtonWidth, buttonHeight).build());

        // Done button (centered)
        int doneButtonWidth = Math.min(layoutHelper.getButtonWidth(), 120);
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> close()
        ).dimensions(layoutHelper.getCenterX(doneButtonWidth), layoutHelper.getBottomY(buttonHeight), doneButtonWidth, buttonHeight).build());

        // Initialize deck row widgets
        updateDeckRowWidgets();
    }

    private void updateDeckRowWidgets() {
        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();

        for (DeckRow row : deckRows) {
            int adjustedY = (int) (row.y - scrollOffset);

            // Skip if not visible
            if (adjustedY < margin || adjustedY > this.height - 100) {
                continue;
            }

            // Use right-aligned row builder for automatic button positioning
            GuiLayoutHelper.RightAlignedRowBuilder rowBuilder = layoutHelper.createRightRow(adjustedY);

            // Delete button (rightmost)
            int deleteButtonWidth = 80;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Delete"),
                    button -> {
                        if (DeckManager.getInstance().deleteDeck(row.deck.getId())) {
                            clearChildren();
                            init();
                        }
                    }
            ).dimensions(rowBuilder.nextX(deleteButtonWidth), rowBuilder.getY(), deleteButtonWidth, buttonHeight).build());

            // Edit button
            int editButtonWidth = 60;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Edit"),
                    button -> MinecraftClient.getInstance().setScreen(new DeckEditorScreen(this, row.deck, false))
            ).dimensions(rowBuilder.nextX(editButtonWidth), rowBuilder.getY(), editButtonWidth, buttonHeight).build());

            // Enable/Disable toggle (leftmost of the action buttons)
            int toggleButtonWidth = 60;
            CyclingButtonWidget<Boolean> toggleButton = CyclingButtonWidget.onOffBuilder(row.deck.isEnabled())
                    .build(rowBuilder.nextX(toggleButtonWidth), rowBuilder.getY(), toggleButtonWidth, buttonHeight,
                            Text.literal(""),
                            (button, value) -> {
                                row.deck.setEnabled(value);
                                DeckManager.getInstance().saveDeck(row.deck);
                            });
            addDrawableChild(toggleButton);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int margin = layoutHelper.getMargin();
        int centerX = this.width / 2;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, "Deck Management", centerX, margin + 5, 0xFFFFFF);

        // Column headers - using layout helper for consistent positioning
        int headerY = margin + 25;
        GuiLayoutHelper.HorizontalRowBuilder headerRow = layoutHelper.createLeftRow(headerY);

        int nameColumnWidth = 200;
        int cardsColumnWidth = 80;
        int categoryColumnWidth = 100;

        context.drawTextWithShadow(this.textRenderer, "Deck Name", headerRow.nextX(0), headerRow.getY(), 0xAAAAAA);
        headerRow.nextX(nameColumnWidth); // Advance to next column

        context.drawTextWithShadow(this.textRenderer, "Cards", headerRow.nextX(0), headerRow.getY(), 0xAAAAAA);
        headerRow.nextX(cardsColumnWidth); // Advance to next column

        context.drawTextWithShadow(this.textRenderer, "Category", headerRow.nextX(0), headerRow.getY(), 0xAAAAAA);

        // Render deck rows
        for (DeckRow row : deckRows) {
            int adjustedY = (int) (row.y - scrollOffset);

            // Skip if not visible
            if (adjustedY < margin + 30 || adjustedY > this.height - 100) {
                continue;
            }

            // Create row builder for consistent column alignment
            GuiLayoutHelper.HorizontalRowBuilder dataRow = layoutHelper.createLeftRow(adjustedY + 5);

            // Deck name
            String deckName = row.deck.getName();
            if (deckName.length() > 25) {
                deckName = deckName.substring(0, 22) + "...";
            }
            int nameColor = row.deck.isEnabled() ? 0xFFFFFF : 0x808080;
            context.drawTextWithShadow(this.textRenderer, deckName, dataRow.nextX(0), dataRow.getY(), nameColor);
            dataRow.nextX(nameColumnWidth); // Advance to next column

            // Card count
            context.drawTextWithShadow(this.textRenderer, String.valueOf(row.deck.getCardCount()), dataRow.nextX(0), dataRow.getY(), 0xFFFFFF);
            dataRow.nextX(cardsColumnWidth); // Advance to next column

            // Category ID
            String categoryId = row.deck.getCategoryId() != null ? row.deck.getCategoryId() : "none";
            context.drawTextWithShadow(this.textRenderer, categoryId, dataRow.nextX(0), dataRow.getY(), 0xFFFFFF);
        }

        // Help text
        String helpText = "Enable/disable decks, edit content, or add new decks";
        int helpTextWidth = this.textRenderer.getWidth(helpText);
        int helpTextY = layoutHelper.getBottomY(layoutHelper.getButtonHeight() * 2 + layoutHelper.getSpacing() + 5);
        context.drawText(this.textRenderer, helpText,
                centerX - helpTextWidth / 2, helpTextY, 0x808080, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= verticalAmount * SCROLL_SPEED;

        // Calculate max scroll
        int maxScroll = Math.max(0, deckRows.size() * (ROW_HEIGHT + layoutHelper.getSpacing()) - (this.height - 150));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

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

    private static class DeckRow {
        final Deck deck;
        final int y;

        DeckRow(Deck deck, int y) {
            this.deck = deck;
            this.y = y;
        }
    }
}

