package io.github.bymartrixx.vtd.object;

import java.util.List;

/**
 * Some json data represented as a Java Object, with some extra methods to make
 * it act like a {@link List}.
 *
 * @param <P> the type of the pack categories
 *
 * @see ResourcePackCategories
 * @see DataPackCategories
 */
public interface PackCategories<P extends PackCategory<?>> extends Iterable<P> {
    int size();

    P get(int i);
}
