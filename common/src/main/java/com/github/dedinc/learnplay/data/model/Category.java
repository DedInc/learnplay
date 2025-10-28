package com.github.dedinc.learnplay.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a category folder that can contain decks and subcategories.
 * Provides hierarchical organization for learning content.
 * <p>
 * Hierarchy: Category (folder) → Subcategory (subfolder, optional) → Deck → Cards
 */
public class Category {
    private final String id;
    private String name;
    private String description;
    private String parentCategoryId; // null for top-level categories
    private final List<String> deckIds; // IDs of decks in this category
    private final List<Category> subcategories; // Child categories (subfolders)

    public Category(String id, String name) {
        this(id, name, "", null, new ArrayList<>(), new ArrayList<>());
    }

    public Category(String id, String name, String description) {
        this(id, name, description, null, new ArrayList<>(), new ArrayList<>());
    }

    public Category(String id, String name, String description, String parentCategoryId, List<String> deckIds, List<Category> subcategories) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
        this.parentCategoryId = parentCategoryId;
        this.deckIds = new ArrayList<>(deckIds != null ? deckIds : new ArrayList<>());
        this.subcategories = new ArrayList<>(subcategories != null ? subcategories : new ArrayList<>());
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public List<String> getDeckIds() {
        return Collections.unmodifiableList(deckIds);
    }

    public List<Category> getSubcategories() {
        return Collections.unmodifiableList(subcategories);
    }

    public boolean isTopLevel() {
        return parentCategoryId == null;
    }

    // Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    // Deck management
    public void addDeckId(String deckId) {
        if (deckId == null || deckId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot add null or empty deck ID");
        }
        if (!deckIds.contains(deckId)) {
            deckIds.add(deckId);
        }
    }

    public boolean removeDeckId(String deckId) {
        return deckIds.remove(deckId);
    }

    public boolean hasDeck(String deckId) {
        return deckIds.contains(deckId);
    }

    public int getDeckCount() {
        return deckIds.size();
    }

    // Subcategory management
    public void addSubcategory(Category subcategory) {
        if (subcategory == null) {
            throw new IllegalArgumentException("Cannot add null subcategory");
        }
        // Check for duplicate IDs
        for (Category existing : subcategories) {
            if (existing.getId().equals(subcategory.getId())) {
                throw new IllegalArgumentException("Subcategory with ID " + subcategory.getId() + " already exists");
            }
        }
        subcategory.setParentCategoryId(this.id);
        subcategories.add(subcategory);
    }

    public boolean removeSubcategory(String subcategoryId) {
        return subcategories.removeIf(sub -> sub.getId().equals(subcategoryId));
    }

    public Category getSubcategory(String subcategoryId) {
        for (Category sub : subcategories) {
            if (sub.getId().equals(subcategoryId)) {
                return sub;
            }
        }
        return null;
    }

    public boolean hasSubcategory(String subcategoryId) {
        return getSubcategory(subcategoryId) != null;
    }

    public int getSubcategoryCount() {
        return subcategories.size();
    }

    // JSON Serialization
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("description", description);

        if (parentCategoryId != null) {
            json.addProperty("parentCategoryId", parentCategoryId);
        }

        // Save deck IDs
        if (!deckIds.isEmpty()) {
            JsonArray deckIdsArray = new JsonArray();
            for (String deckId : deckIds) {
                deckIdsArray.add(deckId);
            }
            json.add("deckIds", deckIdsArray);
        }

        // Save subcategories
        if (!subcategories.isEmpty()) {
            JsonArray subcategoriesArray = new JsonArray();
            for (Category sub : subcategories) {
                subcategoriesArray.add(sub.toJson());
            }
            json.add("subcategories", subcategoriesArray);
        }

        return json;
    }

    public static Category fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        String description = json.has("description") ? json.get("description").getAsString() : "";
        String parentCategoryId = json.has("parentCategoryId") ? json.get("parentCategoryId").getAsString() : null;

        // Load deck IDs
        List<String> deckIds = new ArrayList<>();
        if (json.has("deckIds")) {
            JsonArray deckIdsArray = json.getAsJsonArray("deckIds");
            for (int i = 0; i < deckIdsArray.size(); i++) {
                deckIds.add(deckIdsArray.get(i).getAsString());
            }
        }

        // Load subcategories
        List<Category> subcategories = new ArrayList<>();
        if (json.has("subcategories")) {
            JsonArray subcategoriesArray = json.getAsJsonArray("subcategories");
            for (int i = 0; i < subcategoriesArray.size(); i++) {
                try {
                    Category sub = Category.fromJson(subcategoriesArray.get(i).getAsJsonObject());
                    subcategories.add(sub);
                } catch (Exception e) {
                    System.err.println("Failed to load subcategory in category " + id + ": " + e.getMessage());
                }
            }
        }

        return new Category(id, name, description, parentCategoryId, deckIds, subcategories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", decks=" + deckIds.size() +
                ", subcategories=" + subcategories.size() +
                '}';
    }
}

