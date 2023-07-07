package me.bymartrixx.vtd.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Category {
    public static final List<String> HARD_INCOMPATIBLE_CATEGORIES = List.of("Menu Panoramas", "Options Backgrounds", "Colorful Slime");

    @SerializedName("category")
    private final String name;
    @SerializedName("categories")
    @Nullable
    private List<SubCategory> subCategories;
    private final List<Pack> packs;
    @Nullable
    private Warning warning = null;

    private boolean hardIncompatible = false; // Only used in testing

    private Map<String, Pack> packsById;

    public Category(String name, List<Pack> packs) {
        this.name = name;
        this.packs = packs;

        this.buildPacksById();
    }

    public Category(String name, @Nullable List<SubCategory> subCategories, List<Pack> packs) {
        this(name, packs);
        this.subCategories = subCategories;
    }

    public Category(String name, List<Pack> packs, @Nullable Warning warning) {
        this(name, packs);
        this.warning = warning;
    }

    public Category(String name, @Nullable List<SubCategory> subCategories, List<Pack> packs, @Nullable Warning warning) {
        this(name, subCategories, packs);
        this.warning = warning;
    }

    public Category(String name, List<Pack> packs, @Nullable Warning warning, boolean hardIncompatible) {
        this(name, packs, warning);
        this.hardIncompatible = hardIncompatible;
    }

    public Category(String name, @Nullable List<SubCategory> subCategories, List<Pack> packs, @Nullable Warning warning, boolean hardIncompatible) {
        this(name, subCategories, packs, warning);
        this.hardIncompatible = hardIncompatible;
    }

    private void buildPacksById() {
        if (this.packsById != null) {
            return;
        }

        this.packsById = new HashMap<>();
        for (Pack pack : this.packs) {
            this.packsById.put(pack.getId(), pack);
        }
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public List<SubCategory> getSubCategories() {
        return this.subCategories;
    }

    public List<Pack> getPacks() {
        return this.packs;
    }

    @Nullable
    public Warning getWarning() {
        return this.warning;
    }

    public Pack getPack(String id) {
        this.buildPacksById();
        return this.packsById.get(id);
    }

    public boolean hasWarning() {
        return this.warning != null;
    }

    public List<String> getPackIds() {
        return this.getPacks().stream().map(Pack::getId).toList();
    }

    public boolean isHardIncompatible() {
        return this.hardIncompatible || HARD_INCOMPATIBLE_CATEGORIES.contains(this.getName());
    }

    public String getId() {
        return this.name.toLowerCase(Locale.ROOT).replaceAll("\\s", "-");
    }

    @SuppressWarnings("ClassCanBeRecord") // Gson doesn't support records
    public static class Warning {
        private final String text;
        private final String color;

        public Warning(String text, String color) {
            this.text = text;
            this.color = color;
        }

        public String getText() {
            return text;
        }

        public String getColor() {
            return color;
        }
    }

    public class SubCategory extends Category {
        public SubCategory(String name, List<Pack> packs) {
            super(name, packs);
        }

        public SubCategory(String name, List<Pack> packs, @Nullable Warning warning) {
            super(name, packs, warning);
        }

        public SubCategory(String name, List<Pack> packs, @Nullable Warning warning, boolean hardIncompatible) {
            super(name, packs, warning, hardIncompatible);
        }

        public Category getParent() {
            return Category.this;
        }

        @Override
        @Nullable
        public List<SubCategory> getSubCategories() {
            return null;
        }

        @Override
        public String getId() {
            return this.getParent().getId() + "." + super.getId();
        }
    }
}
