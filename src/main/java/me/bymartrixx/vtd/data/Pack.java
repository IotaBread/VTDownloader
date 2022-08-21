package me.bymartrixx.vtd.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Pack {
    @SerializedName("name")
    private final String id;
    @SerializedName("display")
    private final String name;
    private final String description;
    @SerializedName("incompatible")
    private final List<String> incompatiblePacks;
    @SerializedName("experiment")
    private boolean experimental = false;

    private String icon = null; // Only used in testing

    public Pack(String id, String name, String description, List<String> incompatiblePacks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.incompatiblePacks = incompatiblePacks;
    }

    public Pack(String id, String name, String description, List<String> incompatiblePacks, boolean experimental) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.incompatiblePacks = incompatiblePacks;
        this.experimental = experimental;
    }

    public Pack(String id, String name, String description, List<String> incompatiblePacks, boolean experimental, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.incompatiblePacks = incompatiblePacks;
        this.experimental = experimental;
        this.icon = icon;
    }

    public Pack(String id, String name, String description, List<String> incompatiblePacks, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.incompatiblePacks = incompatiblePacks;
        this.icon = icon;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getIncompatiblePacks() {
        return this.incompatiblePacks;
    }

    public boolean isExperimental() {
        return this.experimental;
    }

    public boolean isCompatible(Pack pack) {
        return !this.incompatiblePacks.contains(pack.getId());
    }

    public String getIcon() {
        return this.icon == null ? this.getId() : this.icon;
    }
}
