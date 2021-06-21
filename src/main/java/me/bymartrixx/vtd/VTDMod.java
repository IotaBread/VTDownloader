package me.bymartrixx.vtd;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
    public static final String VERSION = FabricLoader.getInstance().getModContainer(VTDMod.MOD_ID).isPresent() ? FabricLoader.getInstance().getModContainer(VTDMod.MOD_ID).get().getMetadata().getVersion().toString() : "1.0.0";
    public static final String BASE_URL = "https://vanillatweaks.net";
    public static JsonArray rpCategories;

    public static void log(Level level, String message, Object... fields) {
        VTDMod.LOGGER.log(level, "[" + VTDMod.MOD_NAME + "] " + message, fields);
    }

    public static void log(Level level, String message) {
        log(level, message, (Object) null);
    }

    public static void logError(String message, Throwable t) {
        VTDMod.LOGGER.log(Level.ERROR, "[" + VTDMod.MOD_NAME + "] " + message, t);
    }

    public static JsonArray getCategories(String resourceUrl) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet request = new HttpGet(VTDMod.BASE_URL + resourceUrl);
        HttpResponse response = client.execute(request);

        int responseStatusCode = response.getStatusLine().getStatusCode();

        if (responseStatusCode / 100 != 2) { // Check if responseStatusCode is 2xx/Success
            VTDMod.log(Level.WARN, "The request to the URL {} responded with an unexpected status code: {}. The request processing has been canceled.", VTDMod.BASE_URL + resourceUrl, responseStatusCode);
            return new JsonArray(); // Prevent NPE
        }

        StringBuilder responseContent = new StringBuilder();
        Scanner responseScanner = new Scanner(response.getEntity().getContent());

        while (responseScanner.hasNext()) {
            responseContent.append(responseScanner.nextLine());
        }

        return VTDMod.GSON.fromJson(responseContent.toString(), JsonObject.class).get("categories").getAsJsonArray();
    }

    public static void getRPCategories() throws IOException {
        // TODO: Detect minecraft version from mod version
        VTDMod.rpCategories = VTDMod.getCategories("/assets/resources/json/1.17/rpcategories.json");
    }

    public static void reloadRPCategories() {
        try {
            VTDMod.log(Level.INFO, "Requesting Resource pack categories and packs.");
            VTDMod.getRPCategories();
            VTDMod.log(Level.INFO, "Resource pack categories and packs loaded. There are {} Resource pack categories.", VTDMod.rpCategories.size());
        } catch (IOException e) {
            VTDMod.logError("Encountered an exception while getting the Resource pack categories.", e);
            VTDMod.rpCategories = new JsonArray(); // Prevent NPE
        }
    }

    @Override
    public void onInitializeClient() {
        VTDMod.log(Level.INFO, "Initializing {} version {}...", VTDMod.MOD_NAME, VTDMod.VERSION);

        VTDMod.reloadRPCategories();

        VTDMod.log(Level.INFO, "Initialized {} version {}", VTDMod.MOD_NAME, VTDMod.VERSION);
    }
}
