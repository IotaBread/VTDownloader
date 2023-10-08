package me.bymartrixx.vtd.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UnsavedPackWarningScreen extends WarningScreen {
    private static final Text HEADER = Text.translatable("vtd.unsavedPackWarning.header").formatted(Formatting.BOLD);
    private static final Text MESSAGE = Text.translatable("vtd.unsavedPackWarning.text");
    private static final Text CONFIRM = HEADER.copy().append("\n").append(MESSAGE);

    private final VTDownloadScreen parent;
    private final Screen next;

    protected UnsavedPackWarningScreen(VTDownloadScreen parent, Screen next) {
        super(HEADER, MESSAGE, CONFIRM);
        this.parent = parent;
        this.next = next;
    }

    @Override
    protected void initButtons(int textHeight) {
        this.addDrawableSelectableElement(ButtonWidget.builder(CommonTexts.PROCEED, button -> this.client.setScreen(this.next))
                .positionAndSize(this.width / 2 - 155, 100 + textHeight, 150, 20).build());
        this.addDrawableSelectableElement(ButtonWidget.builder(CommonTexts.BACK, button -> this.client.setScreen(this.parent))
                .positionAndSize(this.width / 2 - 155 + 160, 100 + textHeight, 150, 20).build());
    }
}
