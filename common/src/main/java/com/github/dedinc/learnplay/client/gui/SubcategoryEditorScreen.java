package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.Subcategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Screen for editing subcategories and their cards.
 */
public class SubcategoryEditorScreen extends Screen {
    private final Screen parent;
    private final Category category;
    private final Subcategory subcategory;
    private final boolean isNewSubcategory;
    private GuiLayoutHelper layoutHelper;

    private TextFieldWidget idField;
    private TextFieldWidget nameField;
    private TextFieldWidget descriptionField;

    private double scrollOffset = 0;
    private static final int ROW_HEIGHT = 25;
    private static final int SCROLL_SPEED = 10;

    public SubcategoryEditorScreen(Screen parent, Category category, Subcategory subcategory, boolean isNewSubcategory) {
        super(Text.literal("Edit Subcategory"));
        this.parent = parent;
        this.category = category;
        this.subcategory = subcategory;
        this.isNewSubcategory = isNewSubcategory;
    }

    @Override
    protected void init() {
        super.init();

        layoutHelper = new GuiLayoutHelper(this.width, this.height, this.textRenderer);

        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSpacing();
        int labelSpacing = 12;

        // Limit field width to reasonable size
        int maxFieldWidth = 400;
        int fieldWidth = Math.min(maxFieldWidth, this.width - margin * 2);

        int currentY = layoutHelper.getTitleY() + layoutHelper.getLineHeight() + spacing;

        // Subcategory ID field
        currentY += labelSpacing;
        idField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Subcategory ID"));
        idField.setMaxLength(50);
        idField.setText(subcategory.getId());
        idField.setEditable(isNewSubcategory);
        addDrawableChild(idField);
        currentY += buttonHeight + spacing;

        // Subcategory name field
        currentY += labelSpacing;
        nameField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Subcategory Name"));
        nameField.setMaxLength(100);
        nameField.setText(subcategory.getName());
        addDrawableChild(nameField);
        currentY += buttonHeight + spacing;

        // Description field
        currentY += labelSpacing;
        descriptionField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Description"));
        descriptionField.setMaxLength(200);
        descriptionField.setText(subcategory.getDescription());
        addDrawableChild(descriptionField);
        currentY += buttonHeight + spacing * 2;

        // Cards section
        currentY += labelSpacing + 8;
        renderCards(currentY);

        // Buttons at bottom using layout helper
        int buttonWidth = 120;

        int topBottomRowY = layoutHelper.getBottomY(buttonHeight * 2 + spacing);
        GuiLayoutHelper.HorizontalRowBuilder topBottomRow = layoutHelper.createLeftRow(topBottomRowY);

        // Add Card button - TODO: Subcategory is deprecated, convert to use Deck
        // addDrawableChild(ButtonWidget.builder(
        //     Text.literal("+ Card"),
        //     button -> {
        //         Flashcard newCard = new Flashcard("card_" + System.currentTimeMillis(), "New Question", "New Answer");
        //         MinecraftClient.getInstance().setScreen(new CardEditorScreen(this, null, newCard, true));
        //     }
        // ).dimensions(topBottomRow.nextX(buttonWidth), topBottomRow.getY(), buttonWidth, buttonHeight).build());

        int bottomBottomRowY = layoutHelper.getBottomY(buttonHeight);
        GuiLayoutHelper.HorizontalRowBuilder bottomBottomRow = layoutHelper.createLeftRow(bottomBottomRowY);

        // Save button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Save"),
                button -> saveSubcategory()
        ).dimensions(bottomBottomRow.nextX(buttonWidth), bottomBottomRow.getY(), buttonWidth, buttonHeight).build());

        // Cancel button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> close()
        ).dimensions(bottomBottomRow.nextX(buttonWidth), bottomBottomRow.getY(), buttonWidth, buttonHeight).build());
    }

    private void renderCards(int startY) {
        int margin = layoutHelper.getMargin();
        int buttonHeight = layoutHelper.getButtonHeight();
        int spacing = layoutHelper.getSmallSpacing();

        int currentY = startY;

        for (Flashcard card : subcategory.getCards()) {
            int adjustedY = (int) (currentY - scrollOffset);

            // Skip if not visible
            if (adjustedY < startY - 50 || adjustedY > this.height - 150) {
                currentY += ROW_HEIGHT + spacing;
                continue;
            }

            // Edit button - TODO: Subcategory is deprecated, convert to use Deck
            // addDrawableChild(ButtonWidget.builder(
            //     Text.literal("Edit"),
            //     btn -> MinecraftClient.getInstance().setScreen(new CardEditorScreen(this, null, card, false))
            // ).dimensions(this.width - margin - 140, adjustedY, 60, buttonHeight).build());

            // Delete button
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Delete"),
                    btn -> {
                        subcategory.removeCard(card.getId());
                        clearChildren();
                        init();
                    }
            ).dimensions(this.width - margin - 70, adjustedY, 70, buttonHeight).build());

            currentY += ROW_HEIGHT + spacing;
        }
    }

    private void saveSubcategory() {
        String newName = nameField.getText().trim();
        String newDesc = descriptionField.getText().trim();

        if (newName.isEmpty()) {
            return;
        }

        subcategory.setName(newName);
        subcategory.setDescription(newDesc);

        if (isNewSubcategory) {
            // TODO: Subcategory is deprecated - convert to Category
            // category.addSubcategory(subcategory);
        }

        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int margin = layoutHelper.getMargin();
        int centerX = this.width / 2;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit Subcategory", centerX, margin + 5, 0xFFFFFF);

        // Labels
        int currentY = margin + 20;
        context.drawTextWithShadow(this.textRenderer, "Subcategory ID:", margin, currentY, 0xAAAAAA);
        currentY += 30;
        context.drawTextWithShadow(this.textRenderer, "Subcategory Name:", margin, currentY, 0xAAAAAA);
        currentY += 30;
        context.drawTextWithShadow(this.textRenderer, "Description:", margin, currentY, 0xAAAAAA);
        currentY += 30 + layoutHelper.getSpacing() * 2;
        context.drawTextWithShadow(this.textRenderer, "Cards:", margin, currentY, 0xAAAAAA);

        // Render cards
        int startY = currentY + 20;
        int renderY = startY;

        for (Flashcard card : subcategory.getCards()) {
            int adjustedY = (int) (renderY - scrollOffset);

            if (adjustedY >= startY - 50 && adjustedY <= this.height - 150) {
                String question = card.getQuestion();
                if (question.length() > 40) {
                    question = question.substring(0, 37) + "...";
                }
                context.drawTextWithShadow(this.textRenderer, "📄 " + question, margin, adjustedY + 5, 0xFFFFFF);
            }

            renderY += ROW_HEIGHT + layoutHelper.getSmallSpacing();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= verticalAmount * SCROLL_SPEED;
        scrollOffset = Math.max(0, scrollOffset);

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

