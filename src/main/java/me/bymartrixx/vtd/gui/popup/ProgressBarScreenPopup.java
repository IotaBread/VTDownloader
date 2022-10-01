package me.bymartrixx.vtd.gui.popup;

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

    public ProgressBarScreenPopup(int centerX, int centerY, int width, int height, int color) {
        super(centerX, centerY, width, height);

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
        return this.width - BAR_MARGIN * 2;
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
        int progressWidth = Math.round((barWidth - BAR_OUTLINE_SIZE * 4) * p);
        int x1 = this.centerX - barWidth / 2;
        int y1 = this.centerY - BAR_HEIGHT / 2;
        int x2 = x1 + barWidth;
        int y2 = y1 + BAR_HEIGHT;

        // Outline
        fill(matrices, x1 + BAR_OUTLINE_SIZE, y1, x2 - BAR_OUTLINE_SIZE, y1 + BAR_OUTLINE_SIZE, this.color); // Top line
        fill(matrices, x1 + BAR_OUTLINE_SIZE, y2 - BAR_OUTLINE_SIZE, x2 - BAR_OUTLINE_SIZE, y2, this.color); // Bottom line
        fill(matrices, x1, y1, x1 + BAR_OUTLINE_SIZE, y2, this.color); // Left line
        fill(matrices, x2 - BAR_OUTLINE_SIZE, y1, x2, y2, this.color); // Right line

        // Progress line
        fill(matrices, x1 + BAR_OUTLINE_SIZE * 2, y1 + BAR_OUTLINE_SIZE * 2,
                x1 + BAR_OUTLINE_SIZE * 2 + progressWidth, y2 - BAR_OUTLINE_SIZE * 2, this.color);
    }
}
