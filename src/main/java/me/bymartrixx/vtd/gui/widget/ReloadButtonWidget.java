package me.bymartrixx.vtd.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ReloadButtonWidget extends Button {
    private static final Component ICON = Component.literal("\u21BB");

    public static final int BUTTON_SIZE = 20;

    public ReloadButtonWidget(int x, int y, Component message, OnPress onPress) {
        super(x, y, BUTTON_SIZE, BUTTON_SIZE, message, onPress, Button.DEFAULT_NARRATION);
    }

    protected Component getIconText() {
        return ICON;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float f) {
        this.extractDefaultSprite(graphics);
        int scale = 2;
        int centerX = this.width / 2 + this.getX();

        Font textRenderer = Minecraft.getInstance().font;
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.centeredText(textRenderer, this.getIconText(), centerX / scale, this.getY() / scale, 0xFFFFFFFF);
        graphics.pose().popMatrix();
    }
}
