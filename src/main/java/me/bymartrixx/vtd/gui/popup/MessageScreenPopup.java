package me.bymartrixx.vtd.gui.popup;

import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

import java.net.URI;

public class MessageScreenPopup extends AbstractScreenPopup implements GuiEventListener, NarratableEntry {
    private static final int TITLE_MARGIN = 4;
    private static final int MESSAGE_MARGIN = 2;

    private final Screen screen;
    private final Component title;
    private final int maxWidth;
    private final int maxHeight;
    private MultiLineLabel message;

    public MessageScreenPopup(Minecraft client, Screen screen, int centerX, int centerY, int maxWidth, int maxHeight, Component title) {
        super(client, centerX, centerY, maxWidth, maxHeight);
        this.screen = screen;
        this.title = title;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    private int getMaxLines() {
        int h = this.client.font.lineHeight;
        return (this.maxHeight - TITLE_MARGIN * 2 - h - MESSAGE_MARGIN) / h;
    }

    private int getHeight(int lines) {
        return this.client.font.lineHeight * (lines + 1) + TITLE_MARGIN * 2 + MESSAGE_MARGIN;
    }

    public void show(float time, Component message) {
        int maxLines = this.getMaxLines();

        this.message = Util.createMultilineText(this.client.font, message, maxLines, this.maxWidth);

        int height = this.getHeight(this.message.getLineCount());
        this.updateSize(this.maxWidth, height);
        this.show(time);
    }

    public void visitLines(ActiveTextCollector textCollector) {
        ActiveTextCollector.Parameters parameters = textCollector.defaultParameters();
        textCollector.defaultParameters(parameters.withOpacity(this.getFadeOpacity()));
        textCollector.accept(TextAlignment.CENTER, this.centerX, this.getTop() + TITLE_MARGIN, this.title);

        Font textRenderer = this.client.font;
        int lineHeight = textRenderer.lineHeight;
        int y = this.getTop() + TITLE_MARGIN * 2 + lineHeight;
        this.message.visitLines(TextAlignment.CENTER, this.centerX, y, lineHeight, textCollector);
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        this.visitLines(graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (this.shouldShow() && event.button() == GLFW.GLFW_MOUSE_BUTTON_1
                && mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.getTop() && mouseY < this.getBottom()) {
            Font textRenderer = this.client.font;

            ActiveTextCollector.ClickableStyleFinder styleFinder = new ActiveTextCollector.ClickableStyleFinder(textRenderer, (int) mouseX, (int) mouseY);
            this.visitLines(styleFinder);
            Style style = styleFinder.result();

            if (style != null && style.getClickEvent() != null
                    && style.getClickEvent().action() == ClickEvent.Action.OPEN_URL) {
                URI uri = ((ClickEvent.OpenUrl) style.getClickEvent()).uri();
                Util.openUri(this.client, this.screen, uri);

                return true;
            }
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
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.title);
    }
}
