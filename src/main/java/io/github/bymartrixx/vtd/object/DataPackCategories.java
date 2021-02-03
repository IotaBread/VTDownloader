package io.github.bymartrixx.vtd.object;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * The data from {@code /assets/resources/json/1.16/dpcategories.json}
 * represented as a Java Object, with some extra methods to make it
 * act like a {@link List}.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "categories": [
 *         {
 *           "category": "Mobs",
 *           "packs": [
 *             "name": "anti ghast grief",
 *             "display": "Anti Ghast Grief",
 *             "version": "1.1.0",
 *             "description": "Prevents ghasts from blowing up blocks.",
 *             "incompatible": [],
 *             "lastupdated": 1524346225,
 *             "video": "https://www.youtube.com/watch?v=LWZTL-vJRbw"
 *           ]
 *         }
 *       ]
 *     }
 * </pre>
 */
public class DataPackCategories implements PackCategories<DataPackCategory> {
    private final List<DataPackCategory> categories;

    public DataPackCategories(List<DataPackCategory> categories) {
        this.categories = categories;
    }

    public int size() {
        return categories.size();
    }

    @NotNull
    public Iterator<DataPackCategory> iterator() {
        return categories.iterator();
    }

    public DataPackCategory get(int i) {
        return categories.get(i);
    }
}
