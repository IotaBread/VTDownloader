package me.bymartrixx.vtd.gui;

import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import me.bymartrixx.vtd.util.Util;

public class UnsavedPackWarningScreen extends Screen {
    private static final Text HEADER = Text.translatable("vtd.unsavedPackWarning.header").formatted(Formatting.BOLD);
    private static final Text MESSAGE = Text.translatable("vtd.unsavedPackWarning.text");

    private final VTDownloadScreen parent;
    private final Screen next;
    private MultilineText message;

    protected UnsavedPackWarningScreen(VTDownloadScreen parent, Screen next) {
        super(HEADER);
        this.parent = parent;
        this.next = next;
    }

    @Override
    protected void init() {
        this.message = Util.createMultilineText(this.textRenderer, MESSAGE, 10, 300);

        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 8;
        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height / 2 + 40;

        this.addDrawableSelectableElement(
                ButtonWidget.builder(CommonTexts.PROCEED, button -> this.client.setScreen(this.next))
                        .position(startX, buttonY)
                        .size(buttonWidth, buttonHeight)
                        .build());

        this.addDrawableSelectableElement(ButtonWidget.builder(CommonTexts.BACK, button -> this.closeScreen())
                .position(startX + buttonWidth + spacing, buttonY)
                .size(buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredShadowedText(this.textRenderer, HEADER, this.width / 2, this.height / 2 - 50, 0xFFFFFFFF);

        int y = this.height / 2 - 20;
        MultilineText.C_wvhjqegh alignment = MultilineText.C_wvhjqegh.CENTER;
        this.message.method_73212(graphics, alignment, this.width / 2, y, this.textRenderer.fontHeight, false, 0xFFFFFFFF);
    }
}
