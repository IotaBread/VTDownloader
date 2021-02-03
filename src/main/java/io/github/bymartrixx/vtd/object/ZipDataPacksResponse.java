package io.github.bymartrixx.vtd.object;

/**
 * A response from {@code /assets/server/zipresourcepacks.php} represented as
 * a Java Object.
 *
 * <pre>
 *     An example of the data:
 *     {
 *       "status": "success",
 *       "link": "\/download\/VanillaTweaks_d253622_UNZIP_ME.zip"
 *     }
 * </pre>
 *
 * @see ZipResourcePacksResponse
 */
public class ZipDataPacksResponse {
    public final String status;
    public final String link;

    ZipDataPacksResponse(String status, String link) {
        this.status = status;
        this.link = link;
    }
}
