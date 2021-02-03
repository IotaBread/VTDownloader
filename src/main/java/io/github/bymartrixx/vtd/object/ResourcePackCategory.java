package io.github.bymartrixx.vtd.object;

import java.util.List;

/**
 * A single category from the categories from {@link ResourcePackCategories}.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "category": "Aesthetic",
 *       "packs": [
 *         {
 *           "name": "BlackNetherBricks",
 *           "display": "Black Nether Bricks",
 *           "description": "Changes the texture of Nether Bricks to make them black.",
 *           "incompatible": [
 *             "BrighterNether"
 *           ]
 *         }
 *       ]
 *     }
 * </pre>
 */
public class ResourcePackCategory implements PackCategory<ResourcePack> {
    private final String category;
    private final List<ResourcePack> packs;

    ResourcePackCategory(String category, List<ResourcePack> packs) {
        this.category = category;
        this.packs = packs;
    }

    public String name() {
        return this.category;
    }

    public List<ResourcePack> packs() {
        return this.packs;
    }
}
