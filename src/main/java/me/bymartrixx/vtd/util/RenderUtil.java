package me.bymartrixx.vtd.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class RenderUtil {
    public static final int TEXT_MARGIN = 2;
    public static final int DEBUG_INFO_COLOR = 0xFFCCCCCC;

    public static void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int size, int color) {
        graphics.fill(x - size, y - size, x + width + size, y, color); // Top line
        graphics.fill(x - size, y + height, x + width + size, y + height + size, color); // Bottom line
        graphics.fill(x - size, y, x, y + height, color); // Left line
        graphics.fill(x + width, y, x + width + size, y + height, color); // Right line
    }

    public static void drawEntrySelectionHighlight(GuiGraphics graphics, int x, int y, int width, int height, int outlineColor, int fillColor) {
        int outlineSize = 1;
        graphics.fill(x, y, x + width, y + height, fillColor);
        graphics.fill(x, y, x + width, y + outlineSize, outlineColor); // Top
        graphics.fill(x, y + height - outlineSize, x + width, y + height, outlineColor); // Bottom
        graphics.fill(x, y, x + outlineSize, y + height, outlineColor); // Left
        graphics.fill(x + width - outlineSize, y, x + width, y + height, outlineColor); // Right
    }

    public static void renderDebugInfo(GuiGraphics graphics, TextRenderer textRenderer, int x, int endY, List<String> info) {
        // Make text half its size
        graphics.getMatrices().pushMatrix();
        graphics.getMatrices().scale(0.5F, 0.5F);

        int lineHeight = textRenderer.fontHeight + TEXT_MARGIN;
        int startY = endY * 2 - lineHeight * info.size();
        for (int i = 0; i < info.size(); i++) {
            String text = info.get(i);
            graphics.drawString(textRenderer, text, x, startY + i * lineHeight, DEBUG_INFO_COLOR, false);
        }

        graphics.getMatrices().popMatrix();
    }
}
