package me.bymartrixx.vtd.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import me.bymartrixx.vtd.util.Util;

public class UnsavedPackWarningScreen extends Screen {
    private static final Component HEADER = Component.translatable("vtd.unsavedPackWarning.header").withStyle(ChatFormatting.BOLD);
    private static final Component MESSAGE = Component.translatable("vtd.unsavedPackWarning.text");

    private final VTDownloadScreen parent;
    private final Screen next;
    private MultiLineLabel message;

    protected UnsavedPackWarningScreen(VTDownloadScreen parent, Screen next) {
        super(HEADER);
        this.parent = parent;
        this.next = next;
    }

    @Override
    protected void init() {
        this.message = Util.createMultilineText(this.font, MESSAGE, 10, 300);

        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 8;
        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height / 2 + 40;

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_PROCEED, button -> this.minecraft.setScreen(this.next))
                        .pos(startX, buttonY)
                        .size(buttonWidth, buttonHeight)
                        .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
                .pos(startX + buttonWidth + spacing, buttonY)
                .size(buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, HEADER, this.width / 2, this.height / 2 - 50, 0xFFFFFFFF);

        int y = this.height / 2 - 20;
        TextAlignment alignment = TextAlignment.CENTER;
        this.message.visitLines(alignment, this.width / 2, y, this.font.lineHeight, graphics.textRenderer());
    }
}
