package com.github.dedinc.learnplay.storage;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.storage.category.CategoryFileHandler;
import com.github.dedinc.learnplay.storage.category.CategoryRepository;

import java.util.Collection;
import java.util.List;

/**
 * Manages loading and storing categories from JSON files.
 * Categories act as folders that contain decks.
 * <p>
 * Categories are loaded from the user's config folder (config/learnplay/categories/).
 * Unlike decks, categories do not have built-in resources and will not resurrect after deletion.
 */
public class CategoryManager {

    private final CategoryRepository repository;
    private final CategoryFileHandler fileHandler;

    // Singleton instance
    private static CategoryManager instance;

    private CategoryManager() {
        this.repository = new CategoryRepository();
        this.fileHandler = new CategoryFileHandler();
    }

    public static CategoryManager getInstance() {
        if (instance == null) {
            instance = new CategoryManager();
        }
        return instance;
    }

    /**
     * Load all categories from the config folder.
     */
    public void loadAllCategories() {
        LearnPlay.LOGGER.info("Loading categories...");

        // Load categories from config folder
        List<Category> categories = fileHandler.loadUserCategories();
        for (Category category : categories) {
            repository.put(category.getId(), category);
        }

        LearnPlay.LOGGER.info("Loaded {} category(ies)", categories.size());
    }

    /**
     * Get a category by ID.
     */
    public Category getCategory(String categoryId) {
        return repository.get(categoryId);
    }

    /**
     * Get all loaded categories.
     */
    public Collection<Category> getAllCategories() {
        return repository.getAll();
    }

    /**
     * Get all top-level categories (categories without a parent).
     */
    public List<Category> getTopLevelCategories() {
        return repository.getTopLevel();
    }

    /**
     * Get subcategories of a specific category.
     */
    public List<Category> getSubcategories(String parentCategoryId) {
        return repository.getSubcategories(parentCategoryId);
    }

    /**
     * Save a category to the config folder.
     *
     * @param category The category to save
     * @return true if save was successful
     */
    public boolean saveCategory(Category category) {
        return fileHandler.saveCategory(category);
    }

    /**
     * Delete a category.
     *
     * @param categoryId The ID of the category to delete
     * @return true if delete was successful
     */
    public boolean deleteCategory(String categoryId) {
        // Delete file
        boolean fileDeleted = fileHandler.deleteCategoryFile(categoryId);

        // Always remove from memory
        repository.remove(categoryId);
        LearnPlay.LOGGER.info("Removed category from memory: {}", categoryId);

        return fileDeleted;
    }

    /**
     * Add a new category to the manager and save it.
     *
     * @param category The category to add
     * @return true if add was successful
     */
    public boolean addCategory(Category category) {
        if (repository.contains(category.getId())) {
            LearnPlay.LOGGER.warn("Cannot add category {}: already exists", category.getId());
            return false;
        }

        repository.put(category.getId(), category);
        return saveCategory(category);
    }

    /**
     * Update an existing category and save it.
     *
     * @param category The category to update
     * @return true if update was successful
     */
    public boolean updateCategory(Category category) {
        if (!repository.contains(category.getId())) {
            LearnPlay.LOGGER.warn("Cannot update category {}: not found", category.getId());
            return false;
        }

        repository.put(category.getId(), category);
        return saveCategory(category);
    }

    /**
     * Reload all categories from disk.
     */
    public void reloadCategories() {
        repository.clear();
        loadAllCategories();
    }
}

