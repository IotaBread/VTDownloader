package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ReloadButtonWidget extends ButtonWidget {
    private static final Text ICON = Text.literal("\u21BB"); // Clockwise arrow ↻

    private static final int TEXTURE_V_OFFSET = 46;
    private static final int TEXTURE_HEIGHT = 20;
    private static final int TEXTURE_WIDTH = 200;
    private static final int BUTTON_SIZE = 20;

    public ReloadButtonWidget(int x, int y, Text message, PressAction onPress) {
        super(x, y, BUTTON_SIZE, BUTTON_SIZE, message, onPress);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int imageY = this.getYImage(this.isHoveredOrFocused());
        this.drawTexture(matrices, this.x, this.y,
                0, TEXTURE_V_OFFSET + imageY * TEXTURE_HEIGHT, this.width / 2, this.height);
        this.drawTexture(matrices, this.x + this.width / 2, this.y,
                TEXTURE_WIDTH - this.width / 2, TEXTURE_V_OFFSET + imageY * TEXTURE_HEIGHT, this.width / 2, this.height);

        this.renderBackground(matrices, client, mouseX, mouseY);

        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        drawCenteredScaledText(matrices, textRenderer, ICON, this.x + this.width / 2, this.y + (this.height - 16) / 2,
                color | MathHelper.ceil(this.alpha * 255.0F) << 24, 2.0F);
    }

    private static void drawCenteredScaledText(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color, float scale) {
        matrices.push();
        matrices.scale(scale, scale, scale);
        drawCenteredText(matrices, textRenderer, text, (int) (centerX / scale), (int) (y / scale), color);
        matrices.pop();
    }
}
