package me.bymartrixx.vtd.gui.popup;

import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_emchntzr;
import net.minecraft.unmapped.C_gitqeeaa;
import org.lwjgl.glfw.GLFW;

import java.net.URI;

public class MessageScreenPopup extends AbstractScreenPopup implements Element, Selectable {
    private static final int TITLE_MARGIN = 4;
    private static final int MESSAGE_MARGIN = 2;

    private final Screen screen;
    private final Text title;
    private final int maxWidth;
    private final int maxHeight;
    private MultilineText message;

    public MessageScreenPopup(MinecraftClient client, Screen screen, int centerX, int centerY, int maxWidth, int maxHeight, Text title) {
        super(client, centerX, centerY, maxWidth, maxHeight);
        this.screen = screen;
        this.title = title;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    private int getMaxLines() {
        int h = this.client.textRenderer.fontHeight;
        return (this.maxHeight - TITLE_MARGIN * 2 - h - MESSAGE_MARGIN) / h;
    }

    private int getHeight(int lines) {
        return this.client.textRenderer.fontHeight * (lines + 1) + TITLE_MARGIN * 2 + MESSAGE_MARGIN;
    }

    public void show(float time, Text message) {
        int maxLines = this.getMaxLines();

        this.message = Util.createMultilineText(this.client.textRenderer, message, maxLines, this.maxWidth);

        int height = this.getHeight(this.message.count());
        this.updateSize(this.maxWidth, height);
        this.show(time);
    }

    public void visitLines(C_gitqeeaa textCollector) {
        C_gitqeeaa.C_cnryfyay parameters = textCollector.method_75760();
        textCollector.method_75764(parameters.method_75782(this.getFadeOpacity()));
        textCollector.method_75768(C_emchntzr.CENTER, this.centerX, this.getTop() + TITLE_MARGIN, this.title);

        TextRenderer textRenderer = this.client.textRenderer;
        int lineHeight = textRenderer.fontHeight;
        int y = this.getTop() + TITLE_MARGIN * 2 + lineHeight;
        this.message.method_75816(C_emchntzr.CENTER, this.centerX, y, lineHeight, textCollector);
    }

    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.visitLines(graphics.method_75785(GuiGraphics.C_kbymqsba.TOOLTIP_AND_CURSOR));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (this.shouldShow() && event.method_74245() == GLFW.GLFW_MOUSE_BUTTON_1
                && mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.getTop() && mouseY < this.getBottom()) {
            TextRenderer textRenderer = this.client.textRenderer;

            C_gitqeeaa.C_abiemazc styleFinder = new C_gitqeeaa.C_abiemazc(textRenderer, (int) mouseX, (int) mouseY);
            this.visitLines(styleFinder);
            Style style = styleFinder.method_75777();

            if (style != null && style.getClickEvent() != null
                    && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                URI uri = ((ClickEvent.OpenUrl) style.getClickEvent()).uri();
                Util.openUri(this.client, this.screen, uri);

                return true;
            }
        }

        return Element.super.mouseClicked(event, bl);
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
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.title);
    }
}
