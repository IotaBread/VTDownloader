package io.github.bymartrixx.vtd.object;

/**
 * A response from {@code /assets/server/zipresourcepacks.php} represented as
 * a Java Object.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "status": "success",
 *       "link": "\/download\/VanillaTweaks_r341311.zip"
 *     }
 * </pre>
 */
public class ZipResourcePacksResponse {
    public final String status;
    public final String link;

    ZipResourcePacksResponse(String status, String link) {
        this.status = status;
        this.link = link;
    }
}
