package me.bymartrixx.vtd.gui.popup;

import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class MessageScreenPopup extends AbstractScreenPopup implements Element, Selectable {
    private static final int TITLE_MARGIN = 4;
    private static final int MESSAGE_MARGIN = 2;

    private final Screen screen;
    private final Text title;
    private final int maxWidth;
    private final int maxHeight;
    private List<OrderedText> messageLines;
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

        this.messageLines = Util.getMultilineTextLines(this.client.textRenderer, message, maxLines, this.maxWidth);
        this.message = Util.createMultilineText(this.client.textRenderer, message, maxLines, this.maxWidth);

        this.updateSize(this.maxWidth, this.getHeight(this.message.count()));
        this.show(time);
    }

    @Override
    protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = this.client.textRenderer;
        int color = 0xFFFFFF | this.getFadeAlpha() << 24;
        drawCenteredText(matrices, textRenderer, this.title, this.centerX, this.getTop() + TITLE_MARGIN, color);

        this.message.drawCenterWithShadow(matrices,
                this.centerX, this.getTop() + TITLE_MARGIN * 2 + textRenderer.fontHeight, textRenderer.fontHeight, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.shouldShow() && button == GLFW.GLFW_MOUSE_BUTTON_1
                && mouseX >= this.getLeft() && mouseX < this.getRight()
                && mouseY >= this.getTop() && mouseY < this.getBottom()) {
            double clickedY = mouseY - this.getTop();
            TextRenderer textRenderer = this.client.textRenderer;
            int fontHeight = textRenderer.fontHeight;

            Style style = null;
            if (clickedY >= TITLE_MARGIN && clickedY < TITLE_MARGIN + fontHeight) {
                style = Util.getStyleAt(textRenderer, this.centerX, mouseX, this.title);
            } else if (clickedY >= TITLE_MARGIN * 2 + fontHeight) {
                int l = ((int) clickedY - TITLE_MARGIN * 2 - fontHeight) / fontHeight;
                OrderedText line = this.messageLines.get(l);
                style = Util.getStyleAt(textRenderer, this.centerX, mouseX, line);
            }

            if (style != null && style.getClickEvent() != null
                    && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                this.screen.handleTextClick(style);
                return true;
            }
        }

        return Element.super.mouseClicked(mouseX, mouseY, button);
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
