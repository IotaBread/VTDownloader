package me.bymartrixx.vtd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.texture.NativeImage;
import me.bymartrixx.vtd.data.DownloadPackRequestData;
import me.bymartrixx.vtd.data.DownloadPackResponseData;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.data.RpCategories;
import me.bymartrixx.vtd.data.SharePackRequestData;
import me.bymartrixx.vtd.data.SharePackResponseData;
import me.bymartrixx.vtd.util.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class VTDMod implements ClientModInitializer {
    // DEBUG
    public static final boolean USE_LOCAL_CATEGORIES = false;

    private static final ThreadFactory DOWNLOAD_THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("VT Download %d").build();
    private static final ExecutorService DOWNLOAD_EXECUTOR = Executors.newCachedThreadPool(DOWNLOAD_THREAD_FACTORY);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DownloadPackRequestData.class, new DownloadPackRequestData.Serializer())
            .registerTypeAdapter(SharePackRequestData.class, new SharePackRequestData.Serializer())
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
        String version = "2.0.6";
        String vtVersion = "1.20";

        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (container.isPresent()) {
            vtVersion = container.get().getMetadata().getCustomValue("vt_version").getAsString();
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
            RpCategories categories = null;
            String file = System.getProperty("vtd.debug.rpCategoriesFile");
            if (USE_LOCAL_CATEGORIES && file != null) {
                try (BufferedReader reader = Files.newBufferedReader(Path.of(file))) {
                    categories = GSON.fromJson(reader, RpCategories.class);
                } catch (IOException e) {
                    LOGGER.warn("Failed to load debug categories", e);
                    categories = null;
                }
            }

            if (categories == null) {
                HttpResponse response = executeRequest(createHttpGet("/assets/resources/json/" + VT_VERSION + "/rpcategories.json"));
                try (InputStream stream = new BufferedInputStream(response.getEntity().getContent())) {
                    categories = GSON.fromJson(new InputStreamReader(stream), RpCategories.class);
                }
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

    public static CompletableFuture<Boolean> executePackDownload(
            DownloadPackRequestData requestData, Consumer<Float> progressCallback,
            Path downloadPath, @Nullable String userFileName) {
        LOGGER.debug("Downloading resource packs: {}", GSON.toJson(requestData));

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost request = createHttpPost("/assets/server/zipresourcepacks.php");

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("version", VT_VERSION));
                params.add(new BasicNameValuePair("packs", GSON.toJson(requestData)));
                request.setEntity(new UrlEncodedFormEntity(params));

                return executeRequest(request);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute pack zipping request", e);
            }
        }, DOWNLOAD_EXECUTOR).thenApplyAsync(response -> {
            progressCallback.accept(0.1F);
            int code = response.getStatusLine().getStatusCode();
            if (code / 100 != 2) {
                throw new IllegalStateException("Pack zipping request returned status code " + code);
            }

            try (InputStream stream = new BufferedInputStream(response.getEntity().getContent())) {
                return GSON.fromJson(new InputStreamReader(stream), DownloadPackResponseData.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read pack zipping response", e);
            }
        }, DOWNLOAD_EXECUTOR).thenApplyAsync(data -> {
            progressCallback.accept(0.3F);
            String fileName = userFileName != null ? userFileName + ".zip" : data.getFileName();

            try {
                HttpGet request = createHttpGet(data.getLink());
                request.setConfig(RequestConfig.custom().setConnectTimeout(4000).build());

                return new Pair<>(fileName, executeRequest(request));
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute pack download request", e);
            }
        }, DOWNLOAD_EXECUTOR).thenApplyAsync(data -> {
            progressCallback.accept(0.4F);

            HttpResponse response = data.getRight();
            int code = response.getStatusLine().getStatusCode();
            if (code / 100 != 2) {
                throw new IllegalStateException("Pack download request returned status code " + code);
            }

            String fileName = data.getLeft().trim();
            try (InputStream stream = new BufferedInputStream(response.getEntity().getContent())) {
                return Files.copy(stream, downloadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING) > 0;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read pack download response", e);
            }
        }, DOWNLOAD_EXECUTOR);
    }

    public static CompletableFuture<String> executeShare(SharePackRequestData requestData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost request = createHttpPost("/assets/server/sharecode.php");

                List<NameValuePair> params = Collections.singletonList(
                        new BasicNameValuePair("data", GSON.toJson(requestData)));
                request.setEntity(new UrlEncodedFormEntity(params));

                return executeRequest(request);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute pack share request", e);
            }
        }).thenApplyAsync(response -> {
            int code = response.getStatusLine().getStatusCode();
            if (code / 100 != 2) {
                throw new IllegalStateException("Pack share request returned status code " + code);
            }

            try (InputStream stream = new BufferedInputStream(response.getEntity().getContent())) {
                return GSON.fromJson(new InputStreamReader(stream), SharePackResponseData.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read pack share response", e);
            }
        }).thenApplyAsync(data -> {
            if (data.getResult().equals("error")) {
                throw new IllegalStateException("There was an error sharing the pack");
            }
            return data.getCode();
        });
    }

    public static CompletableFuture<Boolean> downloadIcon(Pack pack) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeRequest(createHttpPost(
                        String.format("/assets/resources/icons/resourcepacks/%s/%s.png", VT_VERSION, pack.getIcon())));
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute icon download request", e);
            }
        }, DOWNLOAD_EXECUTOR).thenApplyAsync(response -> {
            int code = response.getStatusLine().getStatusCode();
            if (code / 100 != 2) {
                throw new IllegalStateException("Icon download request returned status code " + code);
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

    public static CompletableFuture<List<String>> readResourcePackData(ResourcePackProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try (ResourcePack resourcePack = profile.createResourcePack()) {
                ResourceIoSupplier<InputStream> fileStream = resourcePack.openRoot(Constants.SELECTED_PACKS_FILE);
                try (InputStream stream = fileStream != null ? fileStream.get() : null){
                    if (stream != null) {
                        return readSelectedPacks(new BufferedReader(new InputStreamReader(stream)));
                    } else {
                        return Collections.emptyList();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static List<String> readSelectedPacks(BufferedReader reader) throws IOException {
        List<String> selectedPacks = new ArrayList<>();

        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (count == 0 && line.trim().equals(Constants.SELECTED_PACKS_FILE_HEADER)) {
                count++;
                continue;
            } else if (count == 1 && line.trim().startsWith("Version:")) {
                if (!line.substring(8).trim().equals(VT_VERSION)) {
                    throw new IllegalArgumentException("Unsupported pack version");
                }
                count++;
                continue;
            } else if (count == 2 && line.trim().startsWith("Packs:")) {
                count++;
                continue;
            } else if (count > 2) {
                if (!line.isBlank()) {
                    selectedPacks.add(line.trim());
                }

                count++;
                continue;
            }

            throw new IllegalStateException("Invalid selected packs file");
        }

        return selectedPacks;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("VTDownloader {}, using Vanilla Tweaks {}", VERSION, VT_VERSION);
        loadRpCategories();
    }
}
