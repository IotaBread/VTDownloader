package io.github.bymartrixx.skeletonpkg;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SkeletonClass implements ModInitializer {
    public static final String MOD_ID = "skeleton_id";
    public static final String MOD_NAME = "Skeleton Name";
    private static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).isPresent() ? FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().toString() : "1.0.0";
    
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing {} version {}...", MOD_NAME, VERSION);

        // TODO: Mod Initializer

        log(Level.INFO, "Initialized {} version {}", MOD_NAME, VERSION);
    }
    
    public static void log(Level level, String message, Object ... fields) {
        LOGGER.log(level, "[" + MOD_NAME +"] " + message, fields);
    }

    public static void log(Level level, String message) {
        log(level, message, (Object) null);
    }
}
