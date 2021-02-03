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
public class PackCategory {
    public final String category;
    public final List<Pack> packs;

    PackCategory(String category, List<Pack> packs) {
        this.category = category;
        this.packs = packs;
    }
}
