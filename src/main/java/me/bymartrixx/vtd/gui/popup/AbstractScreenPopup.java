package me.bymartrixx.vtd.gui.popup;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class AbstractScreenPopup extends DrawableHelper implements Drawable {
    private static final float BACKGROUND_TEXTURE_SIZE = 32.0F;

    protected final int centerX;
    protected final int centerY;
    protected final int width;
    protected final int height;

    private boolean show;
    private float shownTime;

    public AbstractScreenPopup(int centerX, int centerY, int width, int height) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }

    protected void show(float time) {
        this.show = true;
        this.shownTime = time;
    }

    protected final int getLeft() {
        return this.centerX - this.width / 2;
    }

    protected final int getRight() {
        return this.getLeft() + this.width;
    }

    protected final int getTop() {
        return this.centerY - this.height / 2;
    }

    protected final int getBottom() {
        return this.getTop() + this.height;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.show && mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.getTop() && mouseY < this.getBottom();
    }

    protected void updateShownTime(float delta) {
        if (this.shouldUpdateTime() && this.shownTime > 0.0F) {
            this.shownTime -= delta;
            if (this.shownTime <= 0.0F) {
                this.shownTime = 0.0F;
                this.show = false;
                this.reset();
            }
        }
    }

    protected boolean shouldUpdateTime() {
        return true;
    }

    protected void reset() {}

    @Override
    public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.show) {
            this.renderBackground();
            this.renderContent(matrices, mouseX, mouseY, delta);

            this.updateShownTime(delta);
        }
    }

    protected void renderBackground() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(this.getLeft() - 1, this.getBottom() + 1, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.getRight() + 1, this.getBottom() + 1, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.getRight() + 1, this.getTop() - 1, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.getLeft() - 1, this.getTop() - 1, 0.0)
                .color(0, 0, 0, 255)
                .next();
        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.getLeft(), this.getBottom(), 0.0)
                .uv(0.0F, this.height / BACKGROUND_TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.getRight(), this.getBottom(), 0.0)
                .uv(this.width / BACKGROUND_TEXTURE_SIZE, this.height / BACKGROUND_TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.getRight(), this.getTop(), 0.0)
                .uv(this.width / BACKGROUND_TEXTURE_SIZE, 0.0F)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.getLeft(), this.getTop(), 0.0)
                .uv(0.0F, 0.0F)
                .color(32, 32, 32, 255)
                .next();
        tessellator.draw();
    }

    protected abstract void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta);
}
