package com.github.dedinc.learnplay.client.gui.browser;

import com.github.dedinc.learnplay.client.gui.GuiLayoutHelper;
import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.data.model.Deck;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for creating browser screen buttons (category/deck action buttons).
 */
public class BrowserButtonBuilder {

    private final GuiLayoutHelper layoutHelper;

    public BrowserButtonBuilder(GuiLayoutHelper layoutHelper) {
        this.layoutHelper = layoutHelper;
    }

    /**
     * Create buttons for a category row.
     */
    public List<ButtonWidget> createCategoryButtons(
            Category category,
            int y,
            int buttonHeight,
            GuiLayoutHelper.RowLayout rowLayout,
            Consumer<Category> onOpen,
            Consumer<Category> onEdit,
            Consumer<Category> onDelete
    ) {
        List<ButtonWidget> buttons = new ArrayList<>();

        int deleteButtonWidth = 70;
        int deleteX = rowLayout.addButtonFromRight(deleteButtonWidth);

        int editButtonWidth = 60;
        int editX = rowLayout.addButtonFromRight(editButtonWidth);

        int openButtonWidth = 70;
        int openX = rowLayout.addButtonFromRight(openButtonWidth);

        // Open button
        buttons.add(ButtonWidget.builder(
                Text.literal("Open"),
                btn -> onOpen.accept(category)
        ).dimensions(openX, y, openButtonWidth, buttonHeight).build());

        // Edit button
        buttons.add(ButtonWidget.builder(
                Text.literal("Edit"),
                btn -> onEdit.accept(category)
        ).dimensions(editX, y, editButtonWidth, buttonHeight).build());

        // Delete button
        buttons.add(ButtonWidget.builder(
                Text.literal("Delete"),
                btn -> onDelete.accept(category)
        ).dimensions(deleteX, y, deleteButtonWidth, buttonHeight).build());

        return buttons;
    }

    /**
     * Create buttons for a deck row.
     */
    public List<ButtonWidget> createDeckButtons(
            Deck deck,
            int y,
            int buttonHeight,
            GuiLayoutHelper.RowLayout rowLayout,
            Consumer<Deck> onToggle,
            Consumer<Deck> onEdit,
            Consumer<Deck> onDelete
    ) {
        List<ButtonWidget> buttons = new ArrayList<>();

        int deleteButtonWidth = 70;
        int deleteX = rowLayout.addButtonFromRight(deleteButtonWidth);

        int editButtonWidth = 60;
        int editX = rowLayout.addButtonFromRight(editButtonWidth);

        int toggleButtonWidth = 80;
        int toggleX = rowLayout.addButtonFromRight(toggleButtonWidth);

        // Toggle button
        buttons.add(ButtonWidget.builder(
                Text.literal(deck.isEnabled() ? "✓ On" : "✗ Off"),
                btn -> onToggle.accept(deck)
        ).dimensions(toggleX, y, toggleButtonWidth, buttonHeight).build());

        // Edit button
        buttons.add(ButtonWidget.builder(
                Text.literal("Edit"),
                btn -> onEdit.accept(deck)
        ).dimensions(editX, y, editButtonWidth, buttonHeight).build());

        // Delete button
        buttons.add(ButtonWidget.builder(
                Text.literal("Delete"),
                btn -> onDelete.accept(deck)
        ).dimensions(deleteX, y, deleteButtonWidth, buttonHeight).build());

        return buttons;
    }

    /**
     * Create bottom navigation buttons.
     */
    public List<ButtonWidget> createBottomButtons(
            int bottomRowY,
            int buttonHeight,
            int buttonWidth,
            Runnable onAddCategory,
            Runnable onAddDeck,
            Runnable onBack,
            Runnable onDone
    ) {
        List<ButtonWidget> buttons = new ArrayList<>();
        GuiLayoutHelper.HorizontalRowBuilder bottomRow = layoutHelper.createLeftRow(bottomRowY);

        // Add Category button
        buttons.add(ButtonWidget.builder(
                Text.literal("+ Category"),
                btn -> onAddCategory.run()
        ).dimensions(bottomRow.nextX(buttonWidth), bottomRow.getY(), buttonWidth, buttonHeight).build());

        // Add Deck button
        buttons.add(ButtonWidget.builder(
                Text.literal("+ Deck"),
                btn -> onAddDeck.run()
        ).dimensions(bottomRow.nextX(buttonWidth), bottomRow.getY(), buttonWidth, buttonHeight).build());

        // Back button (if not at root)
        if (onBack != null) {
            buttons.add(ButtonWidget.builder(
                    Text.literal("← Back"),
                    btn -> onBack.run()
            ).dimensions(bottomRow.nextX(buttonWidth), bottomRow.getY(), buttonWidth, buttonHeight).build());
        }

        // Done button
        int doneRowY = layoutHelper.getBottomY(buttonHeight);
        GuiLayoutHelper.HorizontalRowBuilder doneRow = layoutHelper.createLeftRow(doneRowY);
        buttons.add(ButtonWidget.builder(
                Text.literal("Done"),
                btn -> onDone.run()
        ).dimensions(doneRow.nextX(buttonWidth), doneRow.getY(), buttonWidth, buttonHeight).build());

        return buttons;
    }
}

