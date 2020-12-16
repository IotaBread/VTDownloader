package io.github.bymartrixx.vtd;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;

public class VTDScreen extends Screen {
    private final Screen previousScreen;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
    }

    public void onClose() {
        this.client.openScreen(this.previousScreen);
    }

    protected void init() {
        this.addButton(new ButtonWidget(0, 0, 150, 20, new LiteralText("Done"), button -> this.onClose()));
    }
}
