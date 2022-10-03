package me.bymartrixx.vtd.gui.popup;

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
import org.jetbrains.annotations.Nullable;
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

    private List<OrderedText> getMultilineTextLines(Text text, int maxLines) {
        return this.client.textRenderer.wrapLines(text, this.maxWidth).stream()
                .limit(maxLines)
                .toList();
    }

    private MultilineText createMultilineText(Text text, int maxLines) {
        return MultilineText.create(this.client.textRenderer, text, this.maxWidth, maxLines);
    }

    public void show(float time, Text message) {
        int maxLines = this.getMaxLines();

        this.messageLines = this.getMultilineTextLines(message, maxLines);
        this.message = this.createMultilineText(message, maxLines);

        this.updateSize(this.maxWidth, this.getHeight(this.message.count()));
        this.show(time);
    }

    @Nullable
    private Style getStyleAt(double mouseX, Text text) {
        TextRenderer textRenderer = this.client.textRenderer;
        int width = textRenderer.getWidth(text);
        int startX = this.centerX - width / 2;
        int endX = startX + width;

        return mouseX >= startX && mouseX < endX ?
                textRenderer.getTextHandler().getStyleAt(text, (int) mouseX - startX) : null;
    }

    @Nullable
    private Style getStyleAt(double mouseX, OrderedText text) {
        TextRenderer textRenderer = this.client.textRenderer;
        int width = textRenderer.getWidth(text);
        int startX = this.centerX - width / 2;
        int endX = startX + width;

        return mouseX >= startX && mouseX < endX ?
                textRenderer.getTextHandler().getStyleAt(text, (int) mouseX - startX) : null;
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

            int fontHeight = this.client.textRenderer.fontHeight;
            Style style = null;
            if (clickedY >= TITLE_MARGIN && clickedY < TITLE_MARGIN + fontHeight) {
                style = this.getStyleAt(mouseX, this.title);
            } else if (clickedY >= TITLE_MARGIN * 2 + fontHeight) {
                int l = ((int) clickedY - TITLE_MARGIN * 2 - fontHeight) / fontHeight;
                OrderedText line = this.messageLines.get(l);
                style = this.getStyleAt(mouseX, line);
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
