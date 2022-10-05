package me.bymartrixx.vtd.gui.popup;

import me.bymartrixx.vtd.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Supplier;

public class ProgressBarScreenPopup extends AbstractScreenPopup {
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_OUTLINE_SIZE = 1;
    private static final int BAR_MARGIN = 10;

    private final int color;
    private Supplier<Float> progress = () -> 0.0F;
    private Runnable onReset;

    private boolean aborted;

    public ProgressBarScreenPopup(MinecraftClient client, int centerX, int centerY, int width, int height, int color) {
        super(client, centerX, centerY, width, height);

        this.color = color;
    }

    public void show(float time, Supplier<Float> progress, Runnable onReset) {
        this.show(time);

        this.progress = progress;
        this.onReset = onReset;
    }

    public void abortWait() {
        this.aborted = true;
    }

    private int getBarWidth() {
        return this.getWidth() - BAR_MARGIN * 2;
    }

    @Override
    protected boolean shouldUpdateTime() {
        return this.aborted || this.progress.get() >= 1.0F;
    }

    @Override
    protected void reset() {
        this.aborted = false;
        this.onReset.run();
    }

    @Override
    protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float p = this.progress.get();
        if (p < 0.0F) {
            return;
        }

        int barWidth = this.getBarWidth();
        int progressWidth = Math.round((barWidth - BAR_OUTLINE_SIZE * 2) * p);
        int x = this.centerX - barWidth / 2;
        int y = this.centerY - BAR_HEIGHT / 2;
        int color = this.color | this.getFadeAlpha() << 24;

        RenderUtil.drawOutline(matrices, x, y, barWidth, BAR_HEIGHT, BAR_OUTLINE_SIZE, color);

        // Progress line
        fill(matrices, x + BAR_OUTLINE_SIZE, y + BAR_OUTLINE_SIZE,
                x + BAR_OUTLINE_SIZE + progressWidth, y + BAR_HEIGHT - BAR_OUTLINE_SIZE, color);
    }
}
