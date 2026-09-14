package me.bymartrixx.vtd.util;

import me.bymartrixx.vtd.VTDMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Util {
    public static final int VTD_BUTTON_WIDTH = 120;
    public static final int VTD_BUTTON_CENTER_X = VTD_BUTTON_WIDTH / 2;
    public static final int VTD_BUTTON_HEIGHT = 20;
    public static final int VTD_BUTTON_BOTTOM_MARGIN = 24;

    /**
     * Parse an 0xAARRGGBB color from `rgba(red, green, blue, alpha)`
     */
    public static int parseColor(String color) {
        String format = color.substring(0, color.indexOf("("));
        if (color.endsWith(")")) {
            List<String> components = Arrays.stream(color.substring(color.indexOf("(") + 1, color.length() - 1)
                    .split(",")).map(String::trim).toList();

            if (format.equals("rgba")) {
                if (components.size() == 4) {
                    int red = Integer.parseInt(components.get(0));
                    int green = Integer.parseInt(components.get(1));
                    int blue = Integer.parseInt(components.get(2));
                    float alpha = Float.parseFloat(components.get(3));

                    return ARGB.color((int) (alpha * 255), red, green, blue);
                }
            }
        }

        VTDMod.LOGGER.warn("Unknown color format: {}", color);
        return 0x00000000;
    }

    public static String removeHtmlTags(String text) {
        // Remove html tags
        return StringUtils.normalizeSpace(text.replaceAll("(?!<br>)<[^>]*>", " "))
                .replaceAll("<br>", "\n"); // Replace <br> after normalizing to keep new lines
    }

    public static Component urlText(String url) {
        MutableComponent t = Component.literal(url)
                .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.ITALIC, ChatFormatting.BLUE);
        try {
            URI uri = net.minecraft.util.Util.parseAndValidateUntrustedUri(url);
            t.withStyle(s -> s.withClickEvent(new ClickEvent.OpenUrl(uri)));
        } catch (URISyntaxException ignored) {
        }

        return t;
    }

    public static void openUri(Minecraft client, @Nullable Screen screen, URI uri) {
        if (client.options.chatLinksPrompt().get()) {
            client.setScreen(new ConfirmLinkScreen(open -> {
                if (open) {
                    net.minecraft.util.Util.getPlatform().openUri(uri);
                }

                client.setScreen(screen);
            }, uri.toString(), false));
        } else {
            net.minecraft.util.Util.getPlatform().openUri(uri);
        }
    }

    public static List<FormattedCharSequence> getMultilineTextLines(Font textRenderer, Component text, int maxLines, int width) {
        return textRenderer.split(text, width).stream()
                .limit(maxLines)
                .toList();
    }

    public static MultiLineLabel createMultilineText(Font textRenderer, Component text, int maxLines, int width) {
        return MultiLineLabel.create(textRenderer, width, maxLines, text);
    }

    public static MultiLineLabel createMultilineText(Font textRenderer, List<Component> lines, int maxLines) {
        if (lines.size() > maxLines) {
            lines = lines.subList(0, maxLines);
        }

        return MultiLineLabel.create(textRenderer, lines.toArray(new Component[0]));
    }

    public static List<Component> wrapText(Font textRenderer, String text, int maxWidth) {
        StringSplitter textHandler = textRenderer.getSplitter();
        List<FormattedText> visitableLines = textHandler.splitLines(text, maxWidth, Style.EMPTY);
        return visitableLines.stream().map(FormattedText::getString).map(Component::nullToEmpty).toList();
    }
}
