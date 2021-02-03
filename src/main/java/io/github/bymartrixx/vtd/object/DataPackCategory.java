package io.github.bymartrixx.vtd.object;

import java.util.List;

/**
 * A single category from the categories from {@link DataPackCategories}.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "category": "Mobs",
 *       "packs": [
 *         {
 *           "name": "anti ghast grief",
 *           "display": "Anti Ghast Grief",
 *           "version": "1.1.0",
 *           "description": "Prevents ghasts from blowing up blocks.",
 *           "incompatible": [],
 *           "lastupdated": 1524346225,
 *           "video": "https://www.youtube.com/watch?v=LWZTL-vJRbw"
 *         }
 *       ]
 *     }
 * </pre>
 */
public class DataPackCategory implements PackCategory<DataPack> {
    private final String category;
    private final List<DataPack> packs;

    DataPackCategory(String category, List<DataPack> packs) {
        this.category = category;
        this.packs = packs;
    }

    public String name() {
        return this.category;
    }

    public List<DataPack> packs() {
        return this.packs;
    }
}
