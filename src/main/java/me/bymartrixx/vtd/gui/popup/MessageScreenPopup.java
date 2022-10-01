package me.bymartrixx.vtd.gui.popup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class MessageScreenPopup extends AbstractScreenPopup {
    private static final int TITLE_MARGIN = 4;
    private static final int MESSAGE_MARGIN = 2;

    private final Text title;
    private final int maxWidth;
    private final int maxHeight;
    private MultilineText message;

    public MessageScreenPopup(MinecraftClient client, int centerX, int centerY, int maxWidth, int maxHeight, Text title) {
        super(client, centerX, centerY, maxWidth, maxHeight);
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

    private MultilineText createMultilineText(Text text) {
        return MultilineText.create(this.client.textRenderer, text, this.maxWidth, this.getMaxLines());
    }

    public void show(float time, Text message) {
        this.message = this.createMultilineText(message);

        this.updateSize(this.maxWidth, this.getHeight(this.message.count()));
        this.show(time);
    }

    @Override
    protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = this.client.textRenderer;
        drawCenteredText(matrices, textRenderer, this.title, this.centerX, this.getTop() + TITLE_MARGIN, 0xFFFFFFFF);

        this.message.drawCenterWithShadow(matrices, this.centerX, this.getTop() + TITLE_MARGIN * 2 + textRenderer.fontHeight);
    }
}
