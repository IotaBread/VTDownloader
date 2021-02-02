package io.github.bymartrixx.vtd.gui.screen;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

/**
 * VanillaTweaks Resource Pack downloading screen.
 */
public class VTResourcePackScreen extends Screen {
    private final Screen previousScreen;
    private final Text subtitle;

    /**
     * Create a new {@link VTResourcePackScreen}.
     *
     * @param previousScreen the screen that was opened before this one.
     * @param subtitle the screen subtitle.
     */
    public VTResourcePackScreen(Screen previousScreen, Text subtitle) {
        super(new TranslatableText("vtd.title"));
        this.previousScreen = previousScreen;
        this.subtitle = subtitle;
    }

    @Override
    public void onClose() {
        // Return to the previous screen
        this.client.openScreen(this.previousScreen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackgroundTexture(0);

        // Render title and subtitle
        DrawableHelper.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2,
                8, 16777215);
        DrawableHelper.drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2,
                20, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }
}
