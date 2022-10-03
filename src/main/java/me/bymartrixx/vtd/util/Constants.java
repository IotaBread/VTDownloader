package me.bymartrixx.vtd.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class Constants {
    public static final Text ERROR_TEXT = Text.translatable("vtd.error").formatted(Formatting.BOLD);
    public static final Text RESOURCE_PACK_SCREEN_SUBTITLE = Text.translatable("vtd.resourcePack.subtitle")
            .formatted(Formatting.GRAY);
    public static final Text RESOURCE_PACK_BUTTON_TEXT = Text.translatable("vtd.resourcePack.button");
    public static final Text RESOURCE_PACK_RELOAD_TEXT = Text.translatable("vtd.resourcePack.reload");

    public static final Identifier PENCIL_TEXTURE = new Identifier("vt_downloader", "textures/pencil.png");

    public static final String VT_DESCRIPTION_MARKER = "vanillatweaks.net";
    public static final String SELECTED_PACKS_FILE = "Selected Packs.txt";
    public static final String SELECTED_PACKS_FILE_HEADER = "Vanilla Tweaks Resource Pack";

    public static final long PACK_DOWNLOAD_TIMEOUT = 60L; // Seconds
}
