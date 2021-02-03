package io.github.bymartrixx.vtd.object;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * The data from {@code /assets/resources/json/1.16/rpcategories.json}
 * represented as a Java Object, with some extra methods to make it
 * act like a {@link List}.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "categories": [
 *         {
 *           "category": "Aesthetic",
 *           "packs": [
 *             {
 *               "name": "BlackNetherBricks",
 *               "display": "Black Nether Bricks",
 *               "description": "Changes the texture of Nether Bricks to make them black.",
 *               "incompatible": [
 *                 "BrighterNether"
 *               ]
 *             }
 *           ]
 *         }
 *       ]
 *     }
 * </pre>
 */
public class ResourcePackCategories implements Iterable<PackCategory> {
    private final List<PackCategory> categories;

    ResourcePackCategories(List<PackCategory> categories) {
        this.categories = categories;
    }

    public int size() {
        return categories.size();
    }

    @NotNull
    public Iterator<PackCategory> iterator() {
        return categories.iterator();
    }

    public PackCategory get(int i) {
        return categories.get(i);
    }
}
