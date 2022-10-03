package me.bymartrixx.vtd.util;

import me.bymartrixx.vtd.VTDMod;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ColorUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class Util {
    public static final int VTD_BUTTON_WIDTH = 150;
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

                    return ColorUtil.ARGB32.getArgb((int) (alpha * 255), red, green, blue);
                }
            }
        }

        VTDMod.LOGGER.warn("Unknown color format: " + color);
        return 0x00000000;
    }

    public static String removeHtmlTags(String text) {
        // Remove html tags
        return StringUtils.normalizeSpace(text.replaceAll("(?!<br>)<[^>]*>", " "))
                .replaceAll("<br>", "\n"); // Replace <br> after normalizing to keep new lines
    }

    public static Text urlText(String url) {
        return Text.literal(url)
                .formatted(Formatting.UNDERLINE, Formatting.ITALIC, Formatting.BLUE)
                .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
    }

    @Nullable
    public static Style getStyleAt(TextRenderer textRenderer, int centerX, double mouseX, Text text) {
        int width = textRenderer.getWidth(text);
        int startX = centerX - width / 2;
        int endX = startX + width;

        return mouseX >= startX && mouseX < endX ?
                textRenderer.getTextHandler().getStyleAt(text, (int) mouseX - startX) : null;
    }

    @Nullable
    public static Style getStyleAt(TextRenderer textRenderer, int centerX, double mouseX, OrderedText text) {
        int width = textRenderer.getWidth(text);
        int startX = centerX - width / 2;
        int endX = startX + width;

        return mouseX >= startX && mouseX < endX ?
                textRenderer.getTextHandler().getStyleAt(text, (int) mouseX - startX) : null;
    }

    public static List<OrderedText> getMultilineTextLines(TextRenderer textRenderer, Text text, int maxLines, int width) {
        return textRenderer.wrapLines(text, width).stream()
                .limit(maxLines)
                .toList();
    }

    public static MultilineText createMultilineText(TextRenderer textRenderer, Text text, int maxLines, int width) {
        return MultilineText.create(textRenderer, text, width, maxLines);
    }

    public static MultilineText createMultilineText(TextRenderer textRenderer, List<Text> lines, int maxLines) {
        if (lines.size() > maxLines) {
            lines = lines.subList(0, maxLines);
        }

        return MultilineText.create(textRenderer, lines);
    }

    public static List<Text> wrapText(TextRenderer textRenderer, String text, int maxWidth) {
        TextHandler textHandler = textRenderer.getTextHandler();
        List<StringVisitable> visitableLines = textHandler.wrapLines(text, maxWidth, Style.EMPTY);
        return visitableLines.stream().map(StringVisitable::getString).map(Text::of).toList();
    }
}
