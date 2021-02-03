package io.github.bymartrixx.vtd.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A gui widget containing a parentWidget of available and selected resource/data
 * packs.
 */
public class MainWidget  extends AbstractParentElement
        implements Drawable {
    private final MinecraftClient client;
    protected final int entryHeight;
    /**
     * The entries of the available pack parentWidget.
     */
    private final List<Entry> entries = Lists.newArrayList();
    protected int width;
    protected int height;
    /**
     * The Y start of the widget main area.
     */
    protected int top;
    /**
     * The Y end of the widget main area.
     */
    protected int bottom;
    /**
     * The widget right X position/The widget x end pos.
     */
    protected int right;
    /**
     * The widget left X position/The widget x start pos.
     */
    protected int left;
    private double scrollAmount;
    private boolean scrolling;
    /**
     * If to call the {@link #renderHeader(MatrixStack, int, int, Tessellator)} method.
     */
    private boolean renderHeader;

    /**
     * Create an instance of the widget.
     *
     * @param client the {@link MinecraftClient} instance.
     * @param width the widget width.
     * @param height the widget height.
     * @param top the height of the widget top bar.
     * @param bottom the height of the widget bottom bar.
     * @param entryHeight the height of each entry.
     */
    public MainWidget(MinecraftClient client, int width, int height, int top, int bottom,
                      int entryHeight) {
        this.client = client;
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.entryHeight = entryHeight;
        this.left = 0;
        this.right = width;
    }

    /**
     * Create an instance of the widget.
     *
     * @param client the {@link MinecraftClient} instance.
     * @param width the widget width.
     * @param height the widget height.
     * @param top the height of the widget top bar.
     * @param bottom the height of the widget bottom bar.
     * @param entryHeight the height of each entry.
     * @param left the widget x start pos.
     */
    public MainWidget(MinecraftClient client, int width, int height, int top, int bottom,
                      int entryHeight, int left) {
        this.client = client;
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.entryHeight = entryHeight;
        this.left = left;
        this.right = left + width;
    }

    public void setRenderHeader(boolean renderHeader) {
        this.renderHeader = renderHeader;
    }

    /**
     * Get the width of each row.
     *
     * @see #getScrollbarPosX()
     *
     * @return {@linkplain #width this.width} - 16.
     *     20 because the scrollbar is 6 px wide, and has a margin of 2 px to
     *     the sides. 10 pixels on each side to keep symmetry.
     */
    public int getRowWidth() {
        return this.width - 20;
    }

    /**
     * Mark an entry as selected.
     */
    public void setSelected(Entry entry) {
        // TODO
    }

    /**
     * Mark an entry as not selected.
     */
    public void setNotSelected(Entry entry) {
        // TODO
    }

    /**
     * Same as {@link #entries()}. Implemented from
     * {@link net.minecraft.client.gui.ParentElement}.
     */
    public List<Entry> children() {
        return this.entries();
    }

    /**
     * Get {@linkplain #entries this.entries}.
     */
    public List<Entry> entries() {
        return this.entries;
    }

    /**
     * Remove every entry from {@linkplain #entries this.entries}.
     */
    protected void clearEntries() {
        this.entries.clear();
    }

    protected void replaceEntries(Collection<Entry> newEntries) {
        this.entries.clear();
        this.entries.addAll(newEntries);
    }

    protected Entry getEntry(int index) {
        return this.entries.get(index);
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public int getEntryCount() {
        return this.entries.size();
    }

    protected boolean isEntrySelected(int index) {
        return false; // TODO
    }

    protected Entry getEntryAtPosition(double x, double y) {
        int rowCenter = this.getRowWidth() / 2;
        int widgetCenter = this.getWidgetCenter();
        int rowStartX = widgetCenter - rowCenter;
        int rowEndX = widgetCenter + rowCenter;

        int localY = MathHelper.floor(y - this.top) + (int) this.getScrollAmount() - 4;
        int entryIndex = localY / this.entryHeight; // Not totally accurate

        // Check that x is over an entry
        if (x < this.getScrollbarPosX() && x >= rowStartX && x <= rowEndX
                // Check that the entryIndex and localY are valid
                && entryIndex >= 0 && localY >= 0 && entryIndex < this.getEntryCount()) {
            return this.entries.get(entryIndex);
        } else {
            return null;
        }
    }

    public void setLeftPos(int left) {
        this.left = left;
        this.right = left + this.width;
    }

    protected int getMaxPosition() {
        return this.getEntryCount() * this.entryHeight;
    }

    @SuppressWarnings("unused")
    protected void clickedHeader(int x, int y) {}

    @SuppressWarnings("unused")
    protected void renderHeader(MatrixStack matrices, int x, int y, Tessellator tessellator) {}

    /**
     * Render the widget. Implemented from {@link Drawable}
     */
    @SuppressWarnings("deprecation")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        // Render background texture?
        this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.bottom, 0.0D)
                .texture(this.left / 32.0F, (this.bottom + (int) this.getScrollAmount()) / 32.0F)
                .color(32, 32, 32, 255).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D)
                .texture(this.right / 32.0F, (this.bottom + (int) this.getScrollAmount()) / 32.0F)
                .color(32, 32, 32, 255).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D)
                .texture(this.right / 32.0F, (this.top + (int) this.getScrollAmount()) / 32.0F)
                .color(32, 32, 32, 255).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D)
                .texture(this.left / 32.0F, (this.top + (int) this.getScrollAmount()) / 32.0F)
                .color(32, 32, 32, 255).next();
        tessellator.draw();

        int rowLeft = this.getRowLeft();
        int rowsStart = this.top + 4 - (int) this.getScrollAmount();
        if (this.renderHeader) {
            this.renderHeader(matrices, rowLeft, rowsStart, tessellator);
        }

        this.renderList(matrices, rowLeft, rowsStart, mouseX, mouseY, delta);

        // Render background texture for top and bottom bar?
        this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);

        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.top, -100.0D)
                .texture(0.0F, (float) this.top / 32.0F)
                .color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left + this.width, this.top, -100.0D)
                .texture((float) this.width / 32.0F, (float) this.top / 32.0F)
                .color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left + this.width, 0.0D, -100.0D)
                .texture((float) this.width / 32.0F, 0.0F)
                .color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left, 0.0D, -100.0D)
                .texture(0.0F, 0.0F)
                .color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left, this.height, -100.0D)
                .texture(0.0F, (float) this.height / 32.0F)
                .color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left + this.width, this.height, -100.0D)
                .texture((float) this.width / 32.0F, (float) this.height / 32.0F)
                .color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left + this.width, this.bottom, -100.0D)
                .texture((float) this.width / 32.0F, (float) this.bottom / 32.0F)
                .color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left, this.bottom, -100.0D)
                .texture(0.0F, (float) this.bottom / 32.0F)
                .color(64, 64, 64, 255).next();
        tessellator.draw();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO,
                GlStateManager.DstFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();

        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.top + 4, 0.0D)
                .texture(0.0F, 1.0F)
                .color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, this.top + 4, 0.0D)
                .texture(1.0F, 1.0F)
                .color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D)
                .texture(1.0F, 0.0F)
                .color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D)
                .texture(0.0F, 0.0F)
                .color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.bottom, 0.0D)
                .texture(0.0F, 1.0F)
                .color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D)
                .texture(1.0F, 1.0F)
                .color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, this.bottom - 4, 0.0D)
                .texture(1.0F, 0.0F)
                .color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.left, this.bottom - 4, 0.0D)
                .texture(0.0F, 0.0F)
                .color(0, 0, 0, 0).next();
        tessellator.draw();

        int scrollbarPosX = this.getScrollbarPosX();
        int scrollbarPosX2 = scrollbarPosX + 6; // 6 px wide

        // Render scroll bar?
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            RenderSystem.disableTexture();
            int p = ((this.bottom - this.top) * (this.bottom - this.top)) / this.getMaxPosition();
            p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
            int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / maxScroll
                    + this.top;
            if (q < this.top) {
                q = this.top;
            }

            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(scrollbarPosX, this.bottom, 0.0D)
                    .texture(0.0F, 1.0F)
                    .color(0, 0, 0, 255).next();
            bufferBuilder.vertex(scrollbarPosX2, this.bottom, 0.0D)
                    .texture(1.0F, 1.0F)
                    .color(0, 0, 0, 255).next();
            bufferBuilder.vertex(scrollbarPosX2, this.top, 0.0D)
                    .texture(1.0F, 0.0F)
                    .color(0, 0, 0, 255).next();
            bufferBuilder.vertex(scrollbarPosX, this.top, 0.0D)
                    .texture(0.0F, 0.0F)
                    .color(0, 0, 0, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q + p, 0.0D)
                    .texture(0.0F, 1.0F)
                    .color(128, 128, 128, 255).next();
            bufferBuilder.vertex(scrollbarPosX2, q + p, 0.0D)
                    .texture(1.0F, 1.0F)
                    .color(128, 128, 128, 255).next();
            bufferBuilder.vertex(scrollbarPosX2, q, 0.0D)
                    .texture(1.0F, 0.0F)
                    .color(128, 128, 128, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q, 0.0D)
                    .texture(0.0F, 0.0F)
                    .color(128, 128, 128, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q + p - 1, 0.0D)
                    .texture(0.0F, 1.0F)
                    .color(192, 192, 192, 255).next();
            bufferBuilder.vertex(scrollbarPosX2 - 1, q + p - 1, 0.0D)
                    .texture(1.0F, 1.0F)
                    .color(192, 192, 192, 255).next();
            bufferBuilder.vertex(scrollbarPosX2 - 1, q, 0.0D)
                    .texture(1.0F, 0.0F)
                    .color(192, 192, 192, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q, 0.0D)
                    .texture(0.0F, 0.0F)
                    .color(192, 192, 192, 255).next();
            tessellator.draw();
        }

        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    protected void centerScrollOnEntry(Entry entry) {
        this.setScrollAmount(this.entries.indexOf(entry) * this.entryHeight
                + (float) this.entryHeight / 2 - (float) (this.bottom - this.top) / 2);
    }

    protected void ensureEntryVisible(Entry entry) {
        int rowTop = this.getRowTop(this.entries.indexOf(entry));
        int i = rowTop - this.top - 4 - this.entryHeight;
        if (i < 0) {
            this.scroll(i);
        }

        int j = this.bottom - rowTop - this.entryHeight * 2;
        if (j < 0) {
            this.scroll(-j);
        }
    }

    private void scroll(int amount) {
        this.setScrollAmount(this.getScrollAmount() + amount);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double scrollAmount) {
        this.scrollAmount = scrollAmount;
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
    }

    protected void updateScrollingState(double mouseX, @SuppressWarnings("unused") double mouseY,
                                        int button) {
        this.scrolling = button == 0 && mouseX >= this.getScrollbarPosX()
                && mouseX <= this.getScrollbarPosX() + 6;
    }

    /**
     * Get the X position of the scroll bar.
     *
     * @see #getRowWidth()
     *
     * @return {@linkplain #width this.width - 8}.
     *     8 pixels because the scrollbar is 6 px wide and has 2 px of margin
     *     to the right (and left).
     */
    public int getScrollbarPosX() {
        return this.width - 8;
    }

    /**
     * Mouse clicked.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);

        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            Entry entry = this.getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (button == 0) {
                this.clickedHeader((int) mouseX - this.left + this.width / 2
                        - this.getRowWidth() / 2, (int) mouseY - this.top
                        + (int) this.getScrollAmount() - 4);
                return true;
            }

            return this.scrolling;
        }
    }

    /**
     * Mouse released.
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    /**
     * Mouse dragged.
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX,
                                double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        } else if (button == 0 && this.scrolling) {
            if (mouseY < this.top) {
                this.setScrollAmount(0.0D);
            } else if (mouseY > this.bottom) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double d = Math.max(1, this.getMaxScroll());
                int i = this.bottom - this.top;
                int j = MathHelper.clamp(i * i / this.getMaxPosition(), 32, i - 8);
                double e = Math.max(1.0D, d / i - j);
                this.setScrollAmount(this.getScrollAmount() + deltaY * e);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.setScrollAmount(this.getScrollAmount() - amount * this.entryHeight / 2.0D);
        return true;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= this.top && mouseY <= this.bottom && mouseX >= this.left
                && mouseX <= this.right;
    }

    @SuppressWarnings("deprecation")
    protected void renderList(MatrixStack matrices, @SuppressWarnings("unused") int x, int y,
                              int mouseX, int mouseY, float delta) {
        int entryCount = this.getEntryCount();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        for (int i = 0; i < entryCount; ++i) {
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowBottom(i);

            if (rowBottom >= this.top && rowTop <= this.bottom) {
                Entry entry = this.getEntry(i);

                int y2 = y + i * this.entryHeight;
                int y3 = this.entryHeight - 4;
                int rowWidth = this.getRowWidth();
                int rowLeft = this.getRowLeft();

                // Render entry as selected
                if (this.isEntrySelected(i)) {
                    RenderSystem.disableTexture();
                    float f = 0.5F;
                    RenderSystem.color4f(f, f, f, 1.0F);
                    bufferBuilder.begin(7, VertexFormats.POSITION);

                    int rowRight = this.getRowRight();
                    bufferBuilder.vertex(rowLeft, y2 + y3 + 2, 0.0D).next();
                    bufferBuilder.vertex(rowRight, y2 + y3 + 2, 0.0D).next();
                    bufferBuilder.vertex(rowRight, y2 - 2, 0.0D).next();
                    bufferBuilder.vertex(rowLeft, y2 - 2, 0.0D).next();
                    tessellator.draw();
                    RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferBuilder.begin(7, VertexFormats.POSITION);
                    bufferBuilder.vertex(rowLeft + 1, y2 + y3 + 1, 0.0D).next();
                    bufferBuilder.vertex(rowRight - 1, y2 + y3 + 1, 0.0D).next();
                    bufferBuilder.vertex(rowRight - 1, y2 - 1, 0.0D).next();
                    bufferBuilder.vertex(rowLeft + 1, y2 - 1, 0.0D).next();
                    tessellator.draw();
                    RenderSystem.enableTexture();
                }

                entry.render(matrices, i, rowTop, rowLeft, rowWidth, y3, mouseX, mouseY,
                        this.isMouseOver(mouseX, mouseY)
                                && Objects.equals(this.getEntryAtPosition(mouseX,
                                mouseY), entry), delta);
            }
        }
    }

    /**
     * Get the X start position for rows.
     */
    public int getRowLeft() {
        return this.getWidgetCenter() - this.getRowWidth() / 2 + 2;
    }

    /**
     * Get the X end position for rows.
     */
    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    public int getRowTop(int index) {
        return this.top + 4 - (int) this.getScrollAmount() + index * this.entryHeight;
    }

    public int getRowBottom(int index) {
        return this.getRowTop(index) + this.entryHeight;
    }

    protected Entry remove(int index) {
        Entry entry = this.entries.get(index);
        return this.removeEntry(this.entries.get(index)) ? entry : null;
    }

    protected boolean removeEntry(Entry entry) {
        boolean removed = this.entries.remove(entry);
        if (removed) {
            this.setNotSelected(entry);
        }

        return removed;
    }

    public int getWidgetCenter() {
        return this.left + this.width / 2;
    }

    private void setEntryParent(MainWidget.Entry entry) {
        entry.parentWidget = this;
    }

    /**
     * Entry.
     */
    public static class Entry implements Element {
        private final TextRenderer textRenderer;
        private MainWidget parentWidget;
        private final String displayName;

        // TODO: The displayName parameter is just a placeholder, replace
        protected Entry(TextRenderer textRenderer, MainWidget parentWidget, String displayName) {
            this.textRenderer = textRenderer;
            this.parentWidget = parentWidget;
            this.displayName = displayName;
        }


        @SuppressWarnings("unused")
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth,
                           int entryHeight, int mouseX, int mouseY, boolean hovered,
                           float tickDelta) {
            this.textRenderer.drawWithShadow(matrices, this.displayName,
                    this.parentWidget.getWidgetCenter(), y, 16777215);
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(this.parentWidget.getEntryAtPosition(mouseX, mouseY), this);
        }
    }
}
