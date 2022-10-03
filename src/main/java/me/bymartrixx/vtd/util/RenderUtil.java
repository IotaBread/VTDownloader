package me.bymartrixx.vtd.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class RenderUtil {
    public static final int TEXT_MARGIN = 2;
    public static final int DEBUG_INFO_COLOR = 0xFFCCCCCC;

    public static void renderDebugInfo(MatrixStack matrices, TextRenderer textRenderer, int x, int endY, List<String> info) {
        // Make text half its size
        matrices.push();
        matrices.scale(0.5F, 0.5F, 0.5F);

        float lineHeight = textRenderer.fontHeight + TEXT_MARGIN;
        float startY = endY * 2 - lineHeight * info.size();
        for (int i = 0; i < info.size(); i++) {
            String text = info.get(i);
            textRenderer.draw(matrices, text, x, startY + i * lineHeight, DEBUG_INFO_COLOR);
        }

        matrices.pop();
    }

    public static void drawCenteredScaledText(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color, float scale) {
        matrices.push();
        matrices.scale(scale, scale, scale);
        DrawableHelper.drawCenteredText(matrices, textRenderer, text, (int) (centerX / scale), (int) (y / scale), color);
        matrices.pop();
    }
}
