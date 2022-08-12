package me.bymartrixx.vtd.gui;

import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.gui.widget.CategorySelectionWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Text;

public class VTDownloadScreen extends Screen {
    private final Screen parent;
    private final Text subtitle;

    private CategorySelectionWidget categorySelector;

    public VTDownloadScreen(Screen parent, Text subtitle) {
        super(Text.literal("VTDownloader"));
        this.parent = parent;
        this.subtitle = subtitle;
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width - 90, this.height - 30, 80, 20, ScreenTexts.DONE, button -> this.closeScreen()));

        this.categorySelector = this.addDrawableChild(new CategorySelectionWidget(this, 32));
        this.categorySelector.setCategories(VTDMod.rpCategories.getCategories());
        this.categorySelector.initCategoryButtons();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2, 20, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
