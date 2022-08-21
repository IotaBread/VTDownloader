package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class CategoryButtonWidget extends DrawableHelper implements Element, Selectable {
    private static final int TEXTURE_HEIGHT = 20;
    private static final int TEXTURE_V_OFFSET = 46;

    private final Category category;
    private final int width;
    private final int height;
    private final Text text;
    private final VTDownloadScreen screen;
    private boolean selected = false;
    private boolean hovered;
    private boolean focused;

    public CategoryButtonWidget(VTDownloadScreen screen, int width, int height, Text text, Category category) {
        this.screen = screen;
        this.width = width;
        this.height = height;
        this.text = text;
        this.category = category;
    }

    public void render(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
        this.hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
        this.renderButton(matrices, x, y);
    }

    public void renderButton(MatrixStack matrices, int x, int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ClickableWidget.WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int buttonCenter = this.width / 2;

        int yImage = getYImage(this.isHoveredOrFocused());
        int v = TEXTURE_V_OFFSET + yImage * TEXTURE_HEIGHT;
        this.drawTexture(matrices, x, y, 0, v, buttonCenter, this.height);
        this.drawTexture(matrices, x + buttonCenter, y, 200 - buttonCenter, v, buttonCenter, this.height);

        int textColor = this.selected ? 0xA0A0A0 : 0xFFFFFF;
        drawCenteredText(matrices, textRenderer, this.text, x + buttonCenter, y + (this.height - 8) / 2, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered && !this.selected) {
            return this.screen.selectCategory(this.category);
        }

        return false;
    }

    @Override
    public SelectionType getType() {
        // TODO
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        this.focused = !this.focused;
        return this.focused;
    }

    private boolean isHoveredOrFocused() {
        return this.hovered || this.focused;
    }

    private int getYImage(boolean hovered) {
        int y = 1;
        if (selected) {
            y = 0;
        } else if (hovered) {
            y = 2;
        }

        return y;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
