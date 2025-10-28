package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.client.gui.editor.CardListPanel;
import com.github.dedinc.learnplay.client.gui.editor.DeckFormPanel;
import com.github.dedinc.learnplay.client.gui.widgets.ScrollableListWidget;
import com.github.dedinc.learnplay.data.model.Deck;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.storage.DeckManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Screen for editing deck properties and managing categories/cards.
 */
public class DeckEditorScreen extends Screen {
    private final Screen parent;
    private final Deck deck;
    private final boolean isNewDeck;
    private GuiLayoutHelper layoutHelper;

    private DeckFormPanel formPanel;
    private CardListPanel cardListPanel;
    private ScrollableListWidget scrollWidget;

    private static final int ROW_HEIGHT = 25;
    private static final int SCROLL_SPEED = 10;

    private int cardsStartY;
    private DeckFormPanel.FormResult formResult; // Store form result to avoid recreating widgets

    public DeckEditorScreen(Screen parent, Deck deck, boolean isNewDeck) {
        super(Text.literal("Edit Deck"));
        this.parent = parent;
        this.deck = deck;
        this.isNewDeck = isNewDeck;
    }

    @Override
    protected void init() {
        super.init();

        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);
        formPanel = new DeckFormPanel(this.textRenderer, layoutHelper, deck, isNewDeck);
        cardListPanel = new CardListPanel(layoutHelper, this.textRenderer, ROW_HEIGHT);
        scrollWidget = new ScrollableListWidget(layoutHelper, ROW_HEIGHT, SCROLL_SPEED);

        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSpacing();

        // Limit field width to reasonable size
        int maxFieldWidth = 400;
        int fieldWidth = Math.min(maxFieldWidth, this.width - margin * 2);

        int startY = layoutHelper.getTitleY() + layoutHelper.getLineHeight() + spacing;

        // Build form (only once in init, store result for render)
        formResult = formPanel.buildForm(startY, fieldWidth, () -> {
            clearChildren();
            init();
        });

        // Add form widgets (cast to proper types)
        for (Object widget : formResult.widgets) {
            if (widget instanceof ButtonWidget) {
                addDrawableChild((ButtonWidget) widget);
            } else if (widget instanceof net.minecraft.client.gui.widget.TextFieldWidget) {
                addDrawableChild((net.minecraft.client.gui.widget.TextFieldWidget) widget);
            }
        }

        cardsStartY = formResult.nextY;

        // Render card list with scroll
        renderCardList();

        // Add buttons at bottom using layout helper
        int buttonWidth = 120;

        // Top row of bottom buttons (Add Card)
        int topBottomRowY = layoutHelper.getBottomY(buttonHeight * 2 + spacing);
        GuiLayoutHelper.HorizontalRowBuilder topBottomRow = layoutHelper.createLeftRow(topBottomRowY);

        // Add Card button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("+ Card"),
                button -> {
                    Flashcard newCard = new Flashcard("card_" + System.currentTimeMillis(), "New Question", "New Answer");
                    MinecraftClient.getInstance().setScreen(new CardEditorScreen(this, deck, newCard, true));
                }
        ).dimensions(topBottomRow.nextX(buttonWidth), topBottomRow.getY(), buttonWidth, buttonHeight).build());

        // Bottom row of bottom buttons (Save, Cancel)
        int bottomBottomRowY = layoutHelper.getBottomY(buttonHeight);
        GuiLayoutHelper.HorizontalRowBuilder bottomBottomRow = layoutHelper.createLeftRow(bottomBottomRowY);

        // Save button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Save"),
                button -> saveDeck()
        ).dimensions(bottomBottomRow.nextX(buttonWidth), bottomBottomRow.getY(), buttonWidth, buttonHeight).build());

        // Cancel button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> close()
        ).dimensions(bottomBottomRow.nextX(buttonWidth), bottomBottomRow.getY(), buttonWidth, buttonHeight).build());
    }

    private void renderCardList() {
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSmallSpacing();
        int currentY = cardsStartY;

        for (Flashcard card : deck.getCards()) {
            int adjustedY = (int) (currentY - scrollWidget.getScrollOffset());

            // Skip if not visible
            if (adjustedY < cardsStartY - 50 || adjustedY > this.height - 150) {
                currentY += ROW_HEIGHT + spacing;
                continue;
            }

            // Use RowLayout to properly position buttons from right to left
            GuiLayoutHelper.RowLayout rowLayout = layoutHelper.createRowLayout(adjustedY);

            int deleteButtonWidth = 70;
            int deleteX = rowLayout.addButtonFromRight(deleteButtonWidth);

            int editButtonWidth = 60;
            int editX = rowLayout.addButtonFromRight(editButtonWidth);

            // Edit button
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Edit"),
                    btn -> MinecraftClient.getInstance().setScreen(new CardEditorScreen(this, deck, card, false))
            ).dimensions(editX, adjustedY, editButtonWidth, buttonHeight).build());

            // Delete button
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Delete"),
                    btn -> {
                        deck.removeCard(card.getId());
                        clearChildren();
                        init();
                    }
            ).dimensions(deleteX, adjustedY, deleteButtonWidth, buttonHeight).build());

            currentY += ROW_HEIGHT + spacing;
        }
    }

    private void saveDeck() {
        // Get form data
        DeckFormPanel.FormData formData = formPanel.getFormData();

        if (formData == null) {
            // Validation failed
            return;
        }

        // For new decks, if ID was changed, we need to create a new deck object
        // For existing decks, ID cannot be changed
        if (isNewDeck && !deck.getId().equals(formData.id)) {
            // Create new deck with the new ID
            Deck newDeck = new Deck(formData.id, formData.name);
            newDeck.setDescription(formData.description);
            newDeck.setEnabled(deck.isEnabled());
            if (deck.getCategoryId() != null) {
                newDeck.setCategoryId(deck.getCategoryId());
            }
            // Copy all cards
            for (Flashcard card : deck.getCards()) {
                newDeck.addCard(card);
            }
            DeckManager.getInstance().addDeck(newDeck);
        } else {
            // Update existing deck or new deck with same ID
            deck.setName(formData.name);
            deck.setDescription(formData.description);

            DeckManager manager = DeckManager.getInstance();
            if (isNewDeck) {
                manager.addDeck(deck);
            } else {
                manager.updateDeck(deck);
            }
        }

        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit Deck", centerX, layoutHelper.getTitleY(), 0xFFFFFF);

        // Render form labels (use stored formResult from init, don't recreate widgets)
        if (formResult != null) {
            for (DeckFormPanel.LabelData label : formResult.labels) {
                context.drawTextWithShadow(this.textRenderer, label.text, label.x, label.y, 0xAAAAAA);
            }
        }

        // Render card list
        cardListPanel.renderCardList(context, deck.getCards(), cardsStartY, scrollWidget.getScrollOffset(), this.height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollWidget.handleScroll(verticalAmount, deck.getCards().size(), this.height, 150);

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

