package me.bymartrixx.vtd.gui.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.render.RenderPipelines;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ExpandDrawerButtonWidget implements Element, Drawable, Selectable {
    private static final Identifier TEXTURE = Identifier.of("vt_downloader", "textures/drawer_tab.png");
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
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        double mouseX = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
        if (this.isMouseOver(mouseX, mouseY)) {
            this.extended = !this.extended;
            this.callback.accept(this.extended);
            return true;
        }

        return false;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        boolean hovered = mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.y && mouseY < this.y + TAB_HEIGHT;
        float u = hovered ? TAB_WIDTH : 0.0F;
        float v = this.extended ? TAB_HEIGHT : 0.0F;
        graphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getLeft(), this.y, u, v, TAB_WIDTH, TAB_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
