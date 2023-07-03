package me.bymartrixx.vtd.gui.popup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;

public abstract class AbstractScreenPopup implements Drawable {
    private static final int BACKGROUND_TEXTURE_SIZE = 32;
    private static final float FADE_TIME = 20.0F;

    protected final MinecraftClient client;
    protected final int centerX;
    protected final int centerY;
    private int width;
    private int height;

    private boolean show;
    private float shownTime;
    private float fadeTime;

    public AbstractScreenPopup(MinecraftClient client, int centerX, int centerY, int width, int height) {
        this.client = client;
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }

    protected void show(float time) {
        this.show = true;
        this.shownTime = time;
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

    protected final int getFadeAlpha() {
        return (int) ((FADE_TIME - this.fadeTime) / FADE_TIME * 255);
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
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (this.show) {
            this.renderBackground(graphics);
            this.renderContent(graphics, mouseX, mouseY, delta);

            this.updateShownTime(delta);
        }
    }

    protected void renderBackground(GuiGraphics graphics) {
        int alpha = this.getFadeAlpha();
        graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.fill(this.getLeft() - 1, this.getTop() - 1, this.getRight() + 1, this.getBottom() + 1, alpha << 24);

        graphics.setShaderColor(0.25F, 0.25F, 0.25F, alpha / 255.0F);
        graphics.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.getLeft(), this.getTop(), 0.0F, 0.0F, this.width, this.height, BACKGROUND_TEXTURE_SIZE, BACKGROUND_TEXTURE_SIZE);
    }

    protected abstract void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta);
}
