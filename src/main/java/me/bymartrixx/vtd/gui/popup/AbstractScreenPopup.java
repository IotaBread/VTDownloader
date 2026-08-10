package me.bymartrixx.vtd.gui.popup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

public abstract class AbstractScreenPopup implements Renderable {
    private static final int BACKGROUND_TEXTURE_SIZE = 32;
    private static final float FADE_TIME = 20.0F;

    protected final Minecraft client;
    protected final int centerX;
    protected final int centerY;
    private int width;
    private int height;

    private boolean show;
    private float shownTime;
    private float fadeTime;

    public AbstractScreenPopup(Minecraft client, int centerX, int centerY, int width, int height) {
        this.client = client;
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }

    protected void show(float time) {
        this.show = true;
        this.shownTime = time;
        this.fadeTime = 0.0F;
    }

    public boolean shouldShow() {
        return this.show;
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

    protected final int getWidth() {
        return this.width;
    }

    protected final int getHeight() {
        return this.height;
    }

    protected final float getFadeOpacity() {
        return Math.clamp((FADE_TIME - this.fadeTime) / FADE_TIME, 0.0f, 1.0f);
    }

    protected final int getFadeAlpha() {
        return ARGB.as8BitChannel(this.getFadeOpacity());
    }

    protected void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.show && mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.getTop() && mouseY < this.getBottom();
    }

    protected void updateShownTime(float delta) {
        if (this.shouldUpdateTime() && this.show) {
            if (this.shownTime > 0.0F) {
                this.shownTime -= delta;
                if (this.shownTime <= 0.0F) {
                    this.shownTime = 0.0F;
                    this.fadeTime = 0.0F;
                }
            } else if (this.fadeTime >= 0.0F) {
                this.fadeTime += delta;
                if (this.fadeTime >= FADE_TIME) {
                    this.fadeTime = 0.0F;
                    this.show = false;
                    this.reset();
                }
            }
        }
    }

    protected boolean shouldUpdateTime() {
        return true;
    }

    protected void reset() {}

    @Override
    public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (this.show) {
            this.renderBackground(graphics);
            this.renderContent(graphics, mouseX, mouseY, delta);
            this.updateShownTime(delta);
        }
    }

    protected void renderBackground(GuiGraphicsExtractor graphics) {
        int alpha = this.getFadeAlpha();
        graphics.fillGradient(this.getLeft() - 1, this.getTop() - 1, this.getRight() + 1, this.getBottom() + 1, alpha << 24, alpha << 24);

        int color = ARGB.color(alpha, 64, 64, 64);
        // drawTexture
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.MENU_BACKGROUND,
                this.getLeft(), this.getTop(), 0.0F, 0.0F, this.width, this.height, BACKGROUND_TEXTURE_SIZE, BACKGROUND_TEXTURE_SIZE, color);
    }

    protected abstract void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta);
}
