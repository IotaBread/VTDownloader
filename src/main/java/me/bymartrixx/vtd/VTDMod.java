package me.bymartrixx.vtd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.texture.NativeImage;
import me.bymartrixx.vtd.data.DownloadPackRequestData;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.data.RpCategories;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VTDMod implements ClientModInitializer {
    private static final ExecutorService ICON_DOWNLOAD_EXECUTOR = Executors.newCachedThreadPool();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DownloadPackRequestData.class, new DownloadPackRequestData.Serializer())
            .create();
    public static final String MOD_NAME = "VTDownloader";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String BASE_URL = "https://vanillatweaks.net";
    public static final String MOD_ID = "vt_downloader";

    public static final String VT_VERSION;
    public static final String VERSION;

    private static HttpClient httpClient;

    public static RpCategories rpCategories;

    static {
        String version = "2.0.0";
        String vtVersion = "1.19";

        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (container.isPresent()) {
            version = container.get().getMetadata().getVersion().toString();
            vtVersion = version.substring(version.indexOf('+') + 1);
            vtVersion = vtVersion.contains("+") ? vtVersion.substring(0, vtVersion.indexOf('+')) : vtVersion; // Remove build number if present
        }

        VERSION = version;
        VT_VERSION = vtVersion;
    }

    private static HttpClient getClient() {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }

        return httpClient;
    }

    private static String getResourceUri(String resource) {
        resource = !resource.startsWith("/") ? "/" + resource : resource;
        return BASE_URL + resource;
    }

    @Contract("_ -> new")
    private static HttpGet createHttpGet(String resource) {
        return new HttpGet(getResourceUri(resource));
    }

    @Contract("_ -> new")
    private static HttpPost createHttpPost(String resource) {
        return new HttpPost(getResourceUri(resource));
    }

    public static <R extends HttpRequestBase> HttpResponse executeRequest(R request) throws IOException {
        request.addHeader("User-Agent", "VTDownloader v" + VERSION);
        return getClient().execute(request);
    }

    public static void loadRpCategories() {
        try {
            HttpResponse response = executeRequest(createHttpGet("/assets/resources/json/" + VT_VERSION + "/rpcategories.json"));
            RpCategories categories;
            try (InputStream stream = new BufferedInputStream(response.getEntity().getContent())) {
                categories = GSON.fromJson(new InputStreamReader(stream), RpCategories.class);
            }

            if (categories == null) {
                LOGGER.error("Failed to load resource pack categories");
                return;
            }

            rpCategories = categories;
            LOGGER.info("Loaded {} resource pack categories", rpCategories.getCategories().size());
        } catch (IOException e) {
            LOGGER.error("Failed to load resource pack categories", e);
        }
    }

    public static CompletableFuture<Boolean> downloadIcon(Pack pack) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeRequest(createHttpPost(
                        String.format("/assets/resources/icons/resourcepacks/%s/%s.png", VT_VERSION, pack.getIcon())));
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute icon download request", e);
            }
        }, ICON_DOWNLOAD_EXECUTOR).thenApplyAsync(response -> {
            if (response.getStatusLine().getStatusCode() / 100 != 2) {
                return false;
            }

            try (InputStream stream = response.getEntity().getContent()) {
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                Identifier id = getIconId(pack);
                NativeImageBackedTexture icon = new NativeImageBackedTexture(NativeImage.read(stream));

                textureManager.registerTexture(id, icon);
                textureManager.bindTexture(id);
                return true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read icon download response", e);
            }
        });
    }

    @Contract("_ -> new")
    public static Identifier getIconId(Pack pack) {
        return new Identifier(MOD_ID, pack.getId().toLowerCase(Locale.ROOT));
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("VTDownloader {}, using Vanilla Tweaks {}", VERSION, VT_VERSION);
        loadRpCategories();
    }
}
