package com.github.dedinc.learnplay.storage.category;

import com.github.dedinc.learnplay.data.model.Category;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for categories.
 * Provides query methods for category data.
 */
public class CategoryRepository {

    private final Map<String, Category> categories = new HashMap<>();

    /**
     * Add or update a category in the repository.
     */
    public void put(String categoryId, Category category) {
        categories.put(categoryId, category);
    }

    /**
     * Get a category by ID.
     */
    public Category get(String categoryId) {
        return categories.get(categoryId);
    }

    /**
     * Remove a category from the repository.
     */
    public void remove(String categoryId) {
        categories.remove(categoryId);
    }

    /**
     * Check if a category exists.
     */
    public boolean contains(String categoryId) {
        return categories.containsKey(categoryId);
    }

    /**
     * Get all categories.
     */
    public Collection<Category> getAll() {
        return Collections.unmodifiableCollection(categories.values());
    }

    /**
     * Get all top-level categories (categories without a parent).
     */
    public List<Category> getTopLevel() {
        return categories.values().stream()
                .filter(Category::isTopLevel)
                .collect(Collectors.toList());
    }

    /**
     * Get subcategories of a specific category.
     */
    public List<Category> getSubcategories(String parentCategoryId) {
        return categories.values().stream()
                .filter(cat -> parentCategoryId.equals(cat.getParentCategoryId()))
                .collect(Collectors.toList());
    }

    /**
     * Clear all categories.
     */
    public void clear() {
        categories.clear();
    }

    /**
     * Get the total number of categories.
     */
    public int size() {
        return categories.size();
    }
}

