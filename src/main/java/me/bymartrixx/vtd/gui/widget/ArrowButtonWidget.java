package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class ArrowButtonWidget extends ButtonWidget {
    private static final Identifier ARROWS_TEXTURE = new Identifier("textures/font/unicode_page_21.png");
    private final ArrowType arrowType;

    public ArrowButtonWidget(int x, int y, int width, int height, ArrowType arrowType, PressAction onPress) {
        super(x, y, width, height, new LiteralText(""), onPress);
        this.arrowType = arrowType;
    }

    private static void drawArrowTexture(MatrixStack matrices, int x, int y, ArrowType arrowType, MinecraftClient client) {
        client.getTextureManager().bindTexture(ARROWS_TEXTURE);

        float u;
        float v;
        int textureXOffset = 4; // Center the texture in a 16 x 16 area
        switch (arrowType) {
            case CLOCKWISE:
                u = 176.0F;
                v = 176.0F;
                break;
            case LEFT:
                u = 96.0F;
                v = 224.0F;
                break;
            case RIGHT:
            default:
                u = 128.0F;
                v = 224.0F;
                break;
        }

        DrawableHelper.drawTexture(matrices, x + textureXOffset + 2, y + 2, u, v, 16, 16, 256, 256);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getTextureManager().bindTexture(WIDGETS_TEXTURE);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawTexture(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBackground(matrices, minecraftClient, mouseX, mouseY);
        int j = this.active ? 16777215 : 10526880;

        ArrowButtonWidget.drawArrowTexture(matrices, this.x, this.y, arrowType, minecraftClient);
    }

    public enum ArrowType {
        CLOCKWISE,
        LEFT,
        RIGHT
    }
}
