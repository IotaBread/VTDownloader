package me.bymartrixx.vtd.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Constants {
    public static final Component ERROR_TEXT = Component.translatable("vtd.error").withStyle(ChatFormatting.BOLD);
    public static final Component RESOURCE_PACK_SCREEN_SUBTITLE = Component.translatable("vtd.resourcePack.subtitle")
            .withStyle(ChatFormatting.GRAY);
    public static final Component RESOURCE_PACK_BUTTON_TEXT = Component.translatable("vtd.resourcePack.button");
    public static final Component RESOURCE_PACK_RELOAD_TEXT = Component.translatable("vtd.resourcePack.reload");

    public static final Identifier PENCIL_TEXTURE = Identifier.fromNamespaceAndPath("vt_downloader", "textures/pencil.png");

    public static final String VT_DESCRIPTION_MARKER = "vanillatweaks.net";
    public static final String SELECTED_PACKS_FILE = "Selected Packs.txt";
    public static final String SELECTED_PACKS_FILE_HEADER = "Vanilla Tweaks Resource Pack";

    public static final long PACK_DOWNLOAD_TIMEOUT = 60L; // Seconds
}
