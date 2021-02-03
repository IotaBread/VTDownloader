package io.github.bymartrixx.vtd.object;

/**
 * A single pack from the packs from {@link PackCategory}.
 *
 * @see DataPack
 * @see ResourcePack
 */
public interface Pack {
    String name();

    String displayName();

    String description();

    String[] incompatiblePacks();
}
