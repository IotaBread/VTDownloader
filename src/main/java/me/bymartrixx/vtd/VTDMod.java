package me.bymartrixx.vtd;

import com.google.gson.Gson;
import com.mojang.blaze3d.texture.NativeImage;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class VTDMod implements ClientModInitializer {
    private static final ExecutorService ICON_DOWNLOAD_EXECUTOR = Executors.newCachedThreadPool();
    private static final Gson GSON = new Gson();
    public static final String MOD_NAME = "VTDownloader";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String BASE_URL = "https://vanillatweaks.net";
    public static final String MOD_ID = "vt_downloader";

    public static final String VT_VERSION;
    public static final String VERSION;

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

    @Nullable
    public static <T> T executeRequest(String resourceUrl, Function<InputStream, T> reader) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(VTDMod.BASE_URL + resourceUrl);
            HttpResponse response = client.execute(request);

            // Check if the response code is 2xx/Success
            int code = response.getStatusLine().getStatusCode();
            if (code / 100 != 2) {
                LOGGER.error("Failed to execute request to {}, status code {}", VTDMod.BASE_URL + resourceUrl, code);
                return null;
            }

            return reader.apply(response.getEntity().getContent());
        }
    }

    public static void loadRpCategories() {
        try {
            RpCategories categories = VTDMod.executeRequest("/assets/resources/json/" + VT_VERSION + "/rpcategories.json",
                    stream -> GSON.fromJson(new InputStreamReader(stream), RpCategories.class));

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
        String resourceUrl = "/assets/resources/icons/resourcepacks/" + VT_VERSION + "/" + pack.getId() + ".png";

        return CompletableFuture.supplyAsync(() -> {
            Identifier id = getIconId(pack);
            try (InputStream stream = executeRequest(resourceUrl, Function.identity())) {
                if (stream == null) {
                    LOGGER.error("Failed to download icon for pack {}", pack.getName());
                    return false;
                }

                NativeImageBackedTexture icon = new NativeImageBackedTexture(NativeImage.read(stream));
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                textureManager.registerTexture(id, icon);
                textureManager.bindTexture(id);
                return true;
            } catch (IOException e) {
                LOGGER.error("Failed to download icon for pack {}", pack.getName(), e);
                return false;
            }
        }, ICON_DOWNLOAD_EXECUTOR);
    }

    public static Identifier getIconId(Pack pack) {
        return new Identifier(MOD_ID, pack.getId().toLowerCase(Locale.ROOT));
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("VTDownloader {}, using Vanilla Tweaks {}", VERSION, VT_VERSION);
        loadRpCategories();
    }
}
