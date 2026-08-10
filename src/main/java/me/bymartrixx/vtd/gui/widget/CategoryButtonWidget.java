package me.bymartrixx.vtd.gui.widget;

import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

// Doesn't extend ButtonWidget to allow dynamic positioning
public class CategoryButtonWidget implements GuiEventListener, NarratableEntry {
    private static final int TEXTURE_HEIGHT = 20;
    private static final int TEXTURE_V_OFFSET = 46;
    private static final WidgetSprites TEXTURES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted")
    );

    private final Category category;
    private final int width;
    private final int height;
    private final Component text;
    private final VTDownloadScreen screen;
    private boolean selected = false;
    private boolean hovered;
    private boolean focused;

    public CategoryButtonWidget(VTDownloadScreen screen, int width, int height, Component text, Category category) {
        this.screen = screen;
        this.width = width;
        this.height = height;
        this.text = text;
        this.category = category;
    }

    public void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        this.hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
        this.renderButton(graphics, x, y);
    }

    public void renderButton(GuiGraphicsExtractor graphics, int x, int y) {
        Minecraft client = Minecraft.getInstance();
        Font textRenderer = client.font;

        // drawSprite
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURES.get(!this.selected, this.isHoveredOrFocused()), x, y, this.width, this.height);

        int textColor = this.selected ? 0xFFA0A0A0 : 0xFFFFFFFF;
        graphics.centeredText(textRenderer, this.text, x + this.width / 2, y + (this.height - 8) / 2, textColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1 && this.hovered && !this.selected) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return this.screen.selectCategory(this.category);
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (this.selected) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return this.screen.selectCategory(this.category);
        }

        return GuiEventListener.super.keyPressed(event);
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
        return this.hovered;
    }

    private void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public NarrationPriority narrationPriority() {
        if (this.focused) {
            return NarrationPriority.FOCUSED;
        } else if (this.hovered) {
            return NarrationPriority.HOVERED;
        }

        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, AbstractWidget.wrapDefaultNarrationMessage(this.text));
        if (!this.selected) {
            if (this.focused) {
                builder.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
            } else {
                builder.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
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

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
