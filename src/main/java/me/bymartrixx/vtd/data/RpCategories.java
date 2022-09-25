package me.bymartrixx.vtd.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RpCategories {
    private final List<Category> categories;

    public RpCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Category> getCategories() {
        return categories;
    }

    @Nullable
    public Pack findPack(String id) {
        for (Category category : categories) {
            Pack pack = category.getPack(id);
            if (pack != null) {
                return pack;
            }
        }

        return null;
    }

    @Nullable
    public Category getCategory(Pack pack) {
        for (Category category : categories) {
            if (category.getPacks().contains(pack)) {
                return category;
            }
        }

        return null;
    }
}
