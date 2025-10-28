package com.github.dedinc.learnplay.storage.category;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.data.model.Category;
import com.github.dedinc.learnplay.storage.common.JsonFileHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles file I/O operations for categories.
 */
public class CategoryFileHandler {

    private static final String CONFIG_PATH = "config/learnplay/categories/";
    private final JsonFileHandler jsonHandler;

    public CategoryFileHandler() {
        this.jsonHandler = new JsonFileHandler(CONFIG_PATH);
    }

    /**
     * Load all user categories from config folder.
     */
    public List<Category> loadUserCategories() {
        List<Category> categories = new ArrayList<>();
        Path configDir = Paths.get(CONFIG_PATH);

        try {
            // Create config directory if it doesn't exist
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                LearnPlay.LOGGER.info("Created category directory: {}", configDir.toAbsolutePath());
            }

            // Load all JSON files from config directory
            if (Files.exists(configDir) && Files.isDirectory(configDir)) {
                List<Path> jsonFiles = Files.walk(configDir)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .collect(Collectors.toList());

                for (Path jsonFile : jsonFiles) {
                    try {
                        Category category = loadCategoryFromFile(jsonFile);
                        if (category != null) {
                            categories.add(category);
                            LearnPlay.LOGGER.info("Loaded category: {} ({} decks, {} subcategories)",
                                    category.getName(), category.getDeckCount(), category.getSubcategoryCount());
                        }
                    } catch (Exception e) {
                        LearnPlay.LOGGER.error("Failed to load category: {}", jsonFile, e);
                    }
                }
            }
        } catch (IOException e) {
            LearnPlay.LOGGER.error("Failed to access config directory: {}", configDir, e);
        }

        return categories;
    }

    /**
     * Load a category from a file.
     */
    private Category loadCategoryFromFile(Path file) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            return Category.fromJson(json);
        } catch (Exception e) {
            LearnPlay.LOGGER.error("Failed to load category from file: {}", file, e);
            return null;
        }
    }

    /**
     * Save a category to the config folder.
     */
    public boolean saveCategory(Category category) {
        JsonObject json = category.toJson();
        boolean success = jsonHandler.saveJson(category.getId(), json);
        if (success) {
            LearnPlay.LOGGER.info("Saved category: {}", category.getName());
        }
        return success;
    }

    /**
     * Delete a category file.
     */
    public boolean deleteCategoryFile(String categoryId) {
        boolean success = jsonHandler.deleteFile(categoryId);
        if (success) {
            LearnPlay.LOGGER.info("Deleted category file: {}", categoryId);
        }
        return success;
    }
}

