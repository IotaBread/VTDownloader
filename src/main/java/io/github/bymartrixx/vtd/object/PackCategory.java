package io.github.bymartrixx.vtd.object;

import java.util.List;

/**
 * A single category from the categories from {@link PackCategories}.
 *
 * @param <P> the type of the packs
 *
 * @see ResourcePackCategory
 * @see DataPackCategory
 */
public interface PackCategory<P extends Pack> {
    String name();

    List<P> packs();
}
