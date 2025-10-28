package com.github.dedinc.learnplay.client.gui.browser;

import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.storage.CategoryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for building breadcrumb navigation strings for category hierarchies.
 */
public class BreadcrumbBuilder {

    /**
     * Build a breadcrumb string for a category.
     * Example: "Root > Parent > Current"
     */
    public static String buildBreadcrumb(Category category) {
        if (category == null) {
            return "Root";
        }

        List<String> parts = new ArrayList<>();
        Category current = category;
        CategoryManager manager = CategoryManager.getInstance();

        while (current != null) {
            parts.add(0, current.getName());
            if (current.getParentCategoryId() != null) {
                current = manager.getCategory(current.getParentCategoryId());
            } else {
                break;
            }
        }

        return String.join(" > ", parts);
    }
}

