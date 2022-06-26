package me.bymartrixx.vtd.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Text;

public class VTDownloadScreen extends Screen {
    private final Screen parent;

    public VTDownloadScreen(Screen parent, Text title) {
        super(title);
        this.parent = parent;
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width - 90, this.height - 30, 80, 20, ScreenTexts.DONE, button -> this.closeScreen()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
