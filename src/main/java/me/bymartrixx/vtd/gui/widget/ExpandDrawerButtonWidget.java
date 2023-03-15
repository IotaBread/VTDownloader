package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ExpandDrawerButtonWidget extends DrawableHelper implements Element, Drawable, Selectable {
    private static final Identifier TEXTURE = new Identifier("vt_downloader", "textures/drawer_tab.png");
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 64;
    public static final int TAB_WIDTH = 16;
    private static final int TAB_HEIGHT = 32;

    private final int x;
    private final int y;
    private final int drawerWidth;
    private final Consumer<Boolean> callback;

    private boolean extended = false;

    public ExpandDrawerButtonWidget(int x, int y, int drawerWidth, Consumer<Boolean> callback) {
        this.x = x;
        this.y = y;
        this.drawerWidth = drawerWidth;
        this.callback = callback;
    }

    private int getLeft() {
        return this.extended ? this.x - this.drawerWidth : this.x;
    }

    private int getRight() {
        return this.getLeft() + TAB_WIDTH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.y && mouseY < this.y + TAB_HEIGHT
                && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            this.extended = !this.extended;
            this.callback.accept(this.extended);
            return true;
        }

        return Element.super.mouseClicked(mouseX, mouseY, button);
    }

    // TODO
    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        boolean hovered = mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.y && mouseY < this.y + TAB_HEIGHT;
        float u = hovered ? TAB_WIDTH : 0.0F;
        float v = this.extended ? TAB_HEIGHT : 0.0F;
        // noinspection SuspiciousNameCombination - flipped params in the mappings
        drawTexture(matrices, this.getLeft(), this.y, 0, u, v, TAB_WIDTH, TAB_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
