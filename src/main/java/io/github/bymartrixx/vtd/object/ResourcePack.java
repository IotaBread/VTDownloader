package io.github.bymartrixx.vtd.object;

/**
 * A single pack from the packs from {@link ResourcePackCategory}.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "name": "BlackNetherBricks",
 *       "display": "Black Nether Bricks",
 *       "description": "Changes the texture of Nether Bricks to make them black.",
 *       "incompatible": [
 *         "BrighterNether"
 *       ]
 *     }
 * </pre>
 */
public class ResourcePack implements Pack {
    private final String name;
    private final String display;
    private final String description;
    private final String[] incompatible;

    ResourcePack(String name, String display, String description, String[] incompatible) {
        this.name = name;
        this.display = display;
        this.description = description;
        this.incompatible = incompatible;
    }

    public String name() {
        return this.name;
    }

    public String displayName() {
        return this.display;
    }

    public String description() {
        return this.description;
    }

    public String[] incompatiblePacks() {
        return this.incompatible;
    }
}
