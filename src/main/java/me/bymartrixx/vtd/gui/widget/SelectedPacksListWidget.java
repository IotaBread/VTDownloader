package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class SelectedPacksListWidget extends EntryListWidget<SelectedPacksListWidget.AbstractEntry> {
    private static final float BACKGROUND_TEXTURE_SIZE = 32.0F;
    private static final int ITEM_HEIGHT = 16;

    private static final int HORIZONTAL_SHADOWS_BACKGROUND_Z = -100;
    private static final int HORIZONTAL_SHADOWS_SIZE = 4;

    private final VTDownloadScreen screen;
    private boolean extended = false;

    public SelectedPacksListWidget(VTDownloadScreen screen, MinecraftClient client, int width, int top,
                                   int bottom, int left) {
        super(client, width, screen.height, top, bottom, ITEM_HEIGHT);
        this.screen = screen;

        this.setLeftPos(left);
        this.setRenderHorizontalShadows(false); // Rendered at #renderHorizontalShadows
    }

    public boolean isExtended() {
        return this.extended;
    }

    public boolean toggleExtended() {
        this.extended = !this.extended;
        return this.extended;
    }

    // region input callbacks
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.extended && super.isMouseOver(mouseX, mouseY);
    }
    // endregion

    // region render
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.extended) {
            return;
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void renderList(MatrixStack matrices, int x, int y, float delta) {
        super.renderList(matrices, x, y, delta);

        this.renderHorizontalShadows();
    }

    private void renderHorizontalShadows() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GlConst.GL_ALWAYS);

        int z = HORIZONTAL_SHADOWS_BACKGROUND_Z;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.top, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), this.top, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), 0.0, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, 0.0F)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, 0.0, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, 0.0F)
                .color(64, 64, 64, 255)
                .next();

        bufferBuilder.vertex(this.left, this.height, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.height / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), this.height, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, this.height / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), this.bottom, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, this.bottom, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        tessellator.draw();

        RenderSystem.depthFunc(GlConst.GL_LEQUAL);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int size = HORIZONTAL_SHADOWS_SIZE;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(this.left, (this.top + size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        bufferBuilder.vertex(this.right, (this.top + size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        bufferBuilder.vertex(this.right, this.top, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.left, this.top, 0.0)
                .color(0, 0, 0, 255)
                .next();

        bufferBuilder.vertex(this.left, this.bottom, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.right, (this.bottom - size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        bufferBuilder.vertex(this.left, (this.bottom - size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        tessellator.draw();
    }
    // endregion

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    public static abstract class AbstractEntry extends Entry<AbstractEntry> {
    }
}
