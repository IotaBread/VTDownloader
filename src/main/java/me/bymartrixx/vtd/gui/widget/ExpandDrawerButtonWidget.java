package me.bymartrixx.vtd.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ExpandDrawerButtonWidget implements GuiEventListener, Renderable, NarratableEntry {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("vt_downloader", "textures/drawer_tab.png");
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
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (this.isMouseOver(event.x(), event.y()) && event.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            this.extended = !this.extended;
            this.callback.accept(this.extended);
            return true;
        }

        return GuiEventListener.super.mouseClicked(event, bl);
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
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.y && mouseY < this.y + TAB_HEIGHT;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        boolean hovered = mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.y && mouseY < this.y + TAB_HEIGHT;
        float u = hovered ? TAB_WIDTH : 0.0F;
        float v = this.extended ? TAB_HEIGHT : 0.0F;
        // drawTexture
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getLeft(), this.y, u, v, TAB_WIDTH, TAB_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
    }
}
