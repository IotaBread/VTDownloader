package me.bymartrixx.vtd.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;

public class ReloadButtonWidget extends ButtonWidget {
    private static final Text ICON = Text.literal("\u21BB"); // Clockwise arrow ↻

    public static final int BUTTON_SIZE = 20;

    public ReloadButtonWidget(int x, int y, Text message, PressAction onPress) {
        super(x, y, BUTTON_SIZE, BUTTON_SIZE, message, onPress, ButtonWidget.DEFAULT_NARRATION);
    }

    protected Text getIconText() {
        return ICON;
    }

    @Override
    protected void method_75752(GuiGraphics graphics, int mouseX, int mouseY, float f) {
        this.method_75794(graphics);
        int scale = 2;
        int centerX = this.width / 2 + this.getX();

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        graphics.getMatrices().pushMatrix();
        graphics.getMatrices().scale(scale, scale);
        graphics.drawCenteredShadowedText(textRenderer, this.getIconText(), centerX / scale, this.getY() / scale, 0xFFFFFFFF);
        graphics.getMatrices().popMatrix();
    }
}
