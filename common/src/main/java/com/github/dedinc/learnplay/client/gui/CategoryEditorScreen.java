package com.github.dedinc.learnplay.client.gui;

import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.storage.CategoryManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Screen for editing category properties (name and description).
 * Categories are folders that contain decks and subcategories.
 */
public class CategoryEditorScreen extends Screen {
    private final Screen parent;
    private final Category category;
    private final boolean isNewCategory;
    private GuiLayoutHelper layoutHelper;

    private TextFieldWidget nameField;
    private TextFieldWidget descriptionField;

    public CategoryEditorScreen(Screen parent, Category category, boolean isNewCategory) {
        super(Text.literal("Edit Category"));
        this.parent = parent;
        this.category = category;
        this.isNewCategory = isNewCategory;
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

        // Name label (will be rendered in render() method)
        // Reserve space for label
        currentY += layoutHelper.getLineHeight();

        // Name field
        nameField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Name"));
        nameField.setMaxLength(100);
        nameField.setText(category.getName());
        addDrawableChild(nameField);
        currentY += buttonHeight + spacing * 2;

        // Description label (will be rendered in render() method)
        // Reserve space for label
        currentY += layoutHelper.getLineHeight();

        // Description field
        descriptionField = new TextFieldWidget(this.textRenderer, margin, currentY, fieldWidth, buttonHeight, Text.literal("Description"));
        descriptionField.setMaxLength(200);
        descriptionField.setText(category.getDescription());
        addDrawableChild(descriptionField);
        currentY += buttonHeight + spacing * 2;

        // Buttons at bottom
        int buttonWidth = 100;
        int bottomRowY = layoutHelper.getBottomY(buttonHeight);
        int totalButtonWidth = buttonWidth * 2 + spacing;
        int startX = layoutHelper.getCenterX(totalButtonWidth);

        // Save button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Save"),
                button -> saveCategory()
        ).dimensions(startX, bottomRowY, buttonWidth, buttonHeight).build());

        // Cancel button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> close()
        ).dimensions(startX + buttonWidth + spacing, bottomRowY, buttonWidth, buttonHeight).build());
    }

    private void saveCategory() {
        String newName = nameField.getText().trim();
        String newDesc = descriptionField.getText().trim();

        if (newName.isEmpty()) {
            return;
        }

        category.setName(newName);
        category.setDescription(newDesc);

        CategoryManager manager = CategoryManager.getInstance();
        if (isNewCategory) {
            manager.addCategory(category);
        } else {
            manager.updateCategory(category);
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
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit Category", centerX, layoutHelper.getTitleY(), 0xFFFFFF);

        // Labels - positioned ABOVE the fields with proper spacing
        int currentY = layoutHelper.getTitleY() + layoutHelper.getLineHeight() + spacing * 2;

        // Name label
        context.drawTextWithShadow(this.textRenderer, "Name:", margin, currentY, 0xAAAAAA);
        currentY += layoutHelper.getLineHeight();
        currentY += buttonHeight + spacing * 2;

        // Description label
        context.drawTextWithShadow(this.textRenderer, "Description:", margin, currentY, 0xAAAAAA);

        // Info text
        String infoText = isNewCategory ? "Creating new category" : "Editing category: " + category.getId();
        context.drawCenteredTextWithShadow(this.textRenderer, infoText, centerX, this.height - 60, 0x888888);
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

