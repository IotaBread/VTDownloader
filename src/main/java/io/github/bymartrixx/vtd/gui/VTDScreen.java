package io.github.bymartrixx.vtd.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class VTDScreen extends Screen {
    private final Screen previousScreen;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
