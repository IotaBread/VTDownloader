package me.bymartrixx.vtd.gui.widget;

import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

// Doesn't extend ButtonWidget to allow dynamic positioning
public class CategoryButtonWidget implements Element, Selectable {
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

    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float delta) {
        this.hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
        this.renderButton(graphics, x, y);
    }

    public void renderButton(GuiGraphics graphics, int x, int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int buttonCenter = this.width / 2;

        int yImage = getYImage(this.isHoveredOrFocused());
        int v = TEXTURE_V_OFFSET + yImage * TEXTURE_HEIGHT;
        graphics.drawTexture(ClickableWidget.WIDGETS_TEXTURE, x, y, 0, v, buttonCenter, this.height);
        graphics.drawTexture(ClickableWidget.WIDGETS_TEXTURE, x + buttonCenter, y, 200 - buttonCenter, v, buttonCenter, this.height);

        int textColor = this.selected ? 0xA0A0A0 : 0xFFFFFF;
        graphics.drawCenteredShadowedText(textRenderer, this.text, x + buttonCenter, y + (this.height - 8) / 2, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered && !this.selected) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return this.screen.selectCategory(this.category);
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.selected) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return this.screen.selectCategory(this.category);
        }

        return Element.super.keyPressed(keyCode, scanCode, modifiers);
    }

    // TODO
    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    private void playDownSound(SoundManager soundManager) {
        soundManager.play(PositionedSoundInstance.create(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public SelectionType getType() {
        if (this.focused) {
            return SelectionType.FOCUSED;
        } else if (this.hovered) {
            return SelectionType.HOVERED;
        }

        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, ClickableWidget.getNarrationMessage(this.text));
        if (!this.selected) {
            if (this.focused) {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.button.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.button.usage.hovered"));
            }
        }
    }

    // @Override
    // public boolean changeFocus(boolean lookForwards) {
    //     this.focused = !this.focused;
    //     return this.focused;
    // }

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
