package io.github.bymartrixx.vtd.object;

/**
 * A single pack from the packs from {@link PackCategory}.
 *
 * <pre>
 *     An example of the data:
 *     "packs": [
 *       {
 *         "name": "BlackNetherBricks",
 *         "display": "Black Nether Bricks",
 *         "description": "Changes the texture of Nether Bricks to make them black.",
 *         "incompatible": [
 *           "BrighterNether"
 *         ]
 *       }
 * </pre>
 */
public class Pack {
    public final String name;
    public final String display;
    public final String description;
    public final String[] incompatible;

    Pack(String name, String display, String description, String[] incompatible) {
        this.name = name;
        this.display = display;
        this.description = description;
        this.incompatible = incompatible;
    }
}
