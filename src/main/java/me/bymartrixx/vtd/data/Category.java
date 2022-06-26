package me.bymartrixx.vtd.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Category {
    @SerializedName("category")
    private final String name;
    private final List<Pack> packs;
    @Nullable
    private Warning warning = null;

    private final Map<String, Pack> packsById = new HashMap<>();

    public Category(String name, List<Pack> packs) {
        this.name = name;
        this.packs = packs;

        for (Pack pack : packs) {
            this.packsById.put(pack.getId(), pack);
        }
    }

    public Category(String name, List<Pack> packs, @Nullable Warning warning) {
        this(name, packs);
        this.warning = warning;
    }

    public String getName() {
        return this.name;
    }

    public List<Pack> getPacks() {
        return this.packs;
    }

    @Nullable
    public Warning getWarning() {
        return this.warning;
    }

    public Pack getPack(String id) {
        return packsById.get(id);
    }

    public record Warning(String text, String color) {
    }
}
