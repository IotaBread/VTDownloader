package io.github.bymartrixx.vtd.object;

/**
 * A single pack from the packs from {@link DataPackCategory}.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "name": "anti ghast grief",
 *       "display": "Anti Ghast Grief",
 *       "version": "1.1.0",
 *       "description": "Prevents ghasts from blowing up blocks.",
 *       "incompatible": [],
 *       "lastupdated": 1524346225,
 *       "video": "https://www.youtube.com/watch?v=LWZTL-vJRbw"
 *     }
 * </pre>
 */
public class DataPack implements Pack {
    private final String name;
    private final String display;
    public final String version;
    private final String description;
    private final String[] incompatible;
    public final int lastupdated;
    public final String video;

    DataPack(String name, String display, String version, String description, String[] incompatible, int lastupdated, String video) {
        this.name = name;
        this.display = display;
        this.version = version;
        this.description = description;
        this.incompatible = incompatible;
        this.lastupdated = lastupdated;
        this.video = video;
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
