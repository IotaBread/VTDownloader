package me.bymartrixx.vtd.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RpCategories {
    private final List<Category> categories;

    @Nullable
    private List<Category> allCategories;

    public RpCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    private List<Category> getAllCategories() {
        if (this.allCategories != null) {
            return this.allCategories;
        }

        this.allCategories = this.categories.stream().<Category>mapMulti((category, consumer) -> {
            consumer.accept(category);
            if (category.getSubCategories() != null) {
                category.getSubCategories().forEach(consumer);
            }
        }).toList();

        return this.allCategories;
    }

    @Nullable
    public Pack findPack(String id) {
        for (Category category : this.getAllCategories()) {
            Pack pack = category.getPack(id);
            if (pack != null) {
                return pack;
            }
        }

        return null;
    }

    @Nullable
    public Category getCategory(Pack pack) {
        for (Category category : this.getAllCategories()) {
            if (category.getPacks().contains(pack)) {
                return category;
            }
        }

        return null;
    }
}
