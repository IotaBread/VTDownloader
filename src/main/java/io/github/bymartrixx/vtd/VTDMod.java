package io.github.bymartrixx.vtd;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;

public class VTDMod implements ClientModInitializer {
    public static final String MOD_ID = "vt_downloader";
    public static final String MOD_NAME = "VTDownloader";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new Gson();
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).isPresent() ? FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().toString() : "1.0.0";
    public static final String BASE_URL = "https://vanillatweaks.net";
    public static JsonArray categories;

    public static void log(Level level, String message, Object... fields) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message, fields);
    }

    public static void log(Level level, String message) {
        log(level, message, (Object) null);
    }

    public static void logError(String message, Throwable t) {
        LOGGER.log(Level.ERROR, "[" + MOD_NAME + "] " + message, t);
    }

    protected static void getCategories() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/assets/resources/json/1.16/rpcategories.json");
            HttpResponse response = client.execute(request);

            int responseStatus = response.getStatusLine().getStatusCode();

            if (responseStatus != 200) {
                VTDMod.log(Level.WARN, "The categories request responded with an unexpected status code: {}", responseStatus);
                return;
            }

            StringBuilder responseContent = new StringBuilder();

            Scanner scanner = new Scanner(response.getEntity().getContent());

            while (scanner.hasNext()) {
                responseContent.append(scanner.nextLine());
            }

            categories = new JsonParser().parse(responseContent.toString()).getAsJsonObject().get("categories").getAsJsonArray();
        }
    }

    @Override
    public void onInitializeClient() {
        log(Level.INFO, "Initializing {} version {}...", MOD_NAME, VERSION);

        try {
            getCategories();
        } catch (IOException e) {
            logError("Encountered an exception while getting the categories.", e);
            categories = new JsonArray(); // Prevent NPE
        }

        log(Level.INFO, "Initialized {} version {}", MOD_NAME, VERSION);
    }
}
