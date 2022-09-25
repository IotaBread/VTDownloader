package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategorySelectionWidget extends AbstractParentElement implements Drawable, Selectable {
    private static final boolean SHOW_DEBUG_INFO = false;
    private static final float BACKGROUND_TEXTURE_SIZE = 32.0F;

    private static final int LEFT_RIGHT_PADDING = 2;
    private static final int TOP_BOTTOM_PADDING = 2;
    private static final int LEFT_RIGHT_MARGIN = 4;

    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_MARGIN = 10;

    private static final int SCROLLBAR_HEIGHT = 6;
    private static final int SCROLLBAR_MARGIN = 2;
    private static final int SCROLLBAR_MIN_WIDTH = 32;

    private final List<CategoryButtonWidget> children = new ArrayList<>();
    private final Map<Category, CategoryButtonWidget> categoryButtons = new HashMap<>();
    private List<Category> categories;
    private final VTDownloadScreen screen;
    private final int y;

    private int height;
    private int width;
    private int left;
    private int top;
    private int right;
    private int bottom;

    private int startX;
    private int endX;

    private double scrollAmount;
    private boolean scrolling;

    public CategorySelectionWidget(VTDownloadScreen screen, int y) {
        this.screen = screen;
        this.y = y;

        this.calculateDimensions();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void initCategoryButtons() {
        for (Category category : this.categories) {
            CategoryButtonWidget button = getOrCreateCategoryButton(category);
            this.children.add(button);
        }

        this.calculateDimensions();
    }

    private CategoryButtonWidget getOrCreateCategoryButton(Category category) {
        if (categoryButtons.containsKey(category)) {
            return categoryButtons.get(category);
        }

        Text text = Text.literal(category.getName());
        CategoryButtonWidget button = new CategoryButtonWidget(this.screen, BUTTON_WIDTH, BUTTON_HEIGHT, text, category);
        categoryButtons.put(category, button);
        return button;
    }

    public void setSelectedCategory(Category category) {
        categoryButtons.forEach((c, button) -> button.setSelected(c == category));
    }

    private int getButtonsWidth() {
        if (this.categories == null) {
            return 0;
        }

        int x = this.categories.size();
        if (x > 0) {
            return x * BUTTON_WIDTH + (x - 1) * BUTTON_MARGIN;
        }

        return 0;
    }

    private boolean shouldHaveScrollbar() {
        return getButtonsWidth() > width;
    }

    public void updateScreenWidth() {
        double scroll = this.getScrollAmount() / this.getMaxScroll();
        this.calculateDimensions();
        this.setScrollAmount(Math.round(scroll * this.getMaxScroll()));
    }

    private void calculateDimensions() {
        this.width = this.screen.getLeftWidth() - LEFT_RIGHT_MARGIN * 2;
        boolean scrollbar = shouldHaveScrollbar();
        this.height = TOP_BOTTOM_PADDING * 2 + BUTTON_HEIGHT + (scrollbar ? SCROLLBAR_HEIGHT + SCROLLBAR_MARGIN : 0);
        this.left = LEFT_RIGHT_MARGIN;
        this.top = this.y;
        this.right = this.left + this.width;
        this.bottom = this.top + this.height;

        this.startX = 0;
        this.endX = this.right + LEFT_RIGHT_MARGIN
                // Extend right margin to hide buttons if the right margin doesn't reach the screen border
                + (this.screen.width != this.screen.getLeftWidth() ? BUTTON_WIDTH : 0);
    }

    private int getMaxScroll() {
        return Math.max(0, getButtonsWidth() - this.width + LEFT_RIGHT_PADDING * 2);
    }

    private double getScrollAmount() {
        return this.scrollAmount;
    }

    private void setScrollAmount(double scrollAmount) {
        this.scrollAmount = MathHelper.clamp(scrollAmount, 0.0, getMaxScroll());
    }

    private void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == GLFW.GLFW_MOUSE_BUTTON_1 &&
                mouseX >= this.getScrollbarStartX() && mouseX < this.getScrollbarEndX() &&
                mouseY >= this.getScrollbarStartY() && mouseY < this.getScrollbarEndY();
    }

    // region input callbacks
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);

        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            return super.mouseClicked(mouseX, mouseY, button) || this.scrolling;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.scrolling) {
            // Dragging scrollbar
            if (mouseX < this.left) {
                this.setScrollAmount(0);
            } else if (mouseX > this.right) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double maxScroll = Math.max(1, this.getMaxScroll());
                int width = this.getScrollbarEndX() - this.getScrollbarStartX();
                int barSize = (this.width * this.width) / this.getButtonsWidth();
                barSize = MathHelper.clamp(barSize, SCROLLBAR_MIN_WIDTH, width);
                double factor = Math.max(1, maxScroll / (this.width - barSize));
                this.setScrollAmount(this.getScrollAmount() + deltaX * factor);
            }

            return true;
        }

        return false;
    }

    // Only called if isMouseOver is true; from Screen#mouseScrolled
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.setScrollAmount(this.getScrollAmount() - amount * BUTTON_WIDTH / 2);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // TODO: Allow selecting categories from the keyboard
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Needed to allow scrolling
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.left && mouseX < this.right &&
                mouseY >= this.top && mouseY < this.bottom;
    }
    // endregion

    // region render
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.renderCategories(matrices, mouseX, mouseY, delta);
        this.renderScrollbar();
        this.renderMargin();

        if (SHOW_DEBUG_INFO) {
            this.renderDebugInfo(matrices);
        }
    }

    private void renderBackground() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.bottom, 0.0)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0)
                .uv(this.right / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.right, this.top, 0.0)
                .uv(this.right / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.left, this.top, 0.0)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        tessellator.draw();
    }

    private void renderCategories(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        for (int i = 0; i < this.children.size(); i++) {
            CategoryButtonWidget button = this.children.get(i);
            int left = getButtonLeft(i);
            int right = getButtonRight(i);

            // Render only if the button is at least partially visible (else it'd be out of the screen)
            if (right > this.left && left < this.right) {
                button.render(matrices, left, this.top + TOP_BOTTOM_PADDING, mouseX, mouseY, delta);
            }
        }
    }

    private void renderScrollbar() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        if (shouldHaveScrollbar()) {
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            int startX = this.getScrollbarStartX();
            int endX = this.getScrollbarEndX();
            int startY = this.getScrollbarStartY();
            int endY = this.getScrollbarEndY();

            int width = endX - startX;
            int size = (this.width * this.width) / this.getButtonsWidth();
            size = MathHelper.clamp(size, SCROLLBAR_MIN_WIDTH, width);

            int x = (int) this.getScrollAmount() * (width - size) / this.getMaxScroll() + startX;
            if (x < startX) {
                x = startX;
            }

            // Draw slider area
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(startX, endY, 0.0)
                    .color(0, 0, 0, 255)
                    .next();
            bufferBuilder.vertex(endX, endY, 0.0)
                    .color(0, 0, 0, 255)
                    .next();
            bufferBuilder.vertex(endX, startY, 0.0)
                    .color(0, 0, 0, 255)
                    .next();
            bufferBuilder.vertex(startX, startY, 0.0)
                    .color(0, 0, 0, 255)
                    .next();

            // Draw scroll bar
            bufferBuilder.vertex(x, endY, 0.0)
                    .color(128, 128, 128, 255)
                    .next();
            bufferBuilder.vertex(x + size, endY, 0.0)
                    .color(128, 128, 128, 255)
                    .next();
            bufferBuilder.vertex(x + size, startY, 0.0)
                    .color(128, 128, 128, 255)
                    .next();
            bufferBuilder.vertex(x, startY, 0.0)
                    .color(128, 128, 128, 255)
                    .next();

            // Draw scroll bar highlight
            bufferBuilder.vertex(x, endY - 1, 0.0)
                    .color(192, 192, 192, 255)
                    .next();
            bufferBuilder.vertex(x + size - 1, endY - 1, 0.0)
                    .color(192, 192, 192, 255)
                    .next();
            bufferBuilder.vertex(x + size - 1, startY, 0.0)
                    .color(192, 192, 192, 255)
                    .next();
            bufferBuilder.vertex(x, startY, 0.0)
                    .color(192, 192, 192, 255)
                    .next();
            tessellator.draw();
        }

        RenderSystem.enableTexture();
    }

    // Render a margin over the category buttons to make them look as if they were partially under the background
    private void renderMargin() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Left side
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.startX, this.bottom, 0.0)
                .uv(this.startX / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, this.bottom, 0.0)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, this.top, 0.0)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.startX, this.top, 0.0)
                .uv(this.startX / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();

        // Right side
        bufferBuilder.vertex(this.right, this.bottom, 0.0)
                .uv(this.right / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.endX, this.bottom, 0.0)
                .uv(this.endX / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.endX, this.top, 0.0)
                .uv(this.endX / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.right, this.top, 0.0)
                .uv(this.right / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        tessellator.draw();
    }

    private void renderDebugInfo(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int last = categories.size() - 1;
        List<String> debugInfo = List.of(
                "WxH = " + this.width + "x" + this.height,
                "T/B = " + this.top + "/" + this.bottom,
                "L/R = " + this.left + "/" + this.right,
                "PL/PR = " + (this.left + LEFT_RIGHT_PADDING) + "/" + (this.right - LEFT_RIGHT_PADDING),
                "S = " + this.scrolling,
                "SA/MS = " + this.getScrollAmount() + "/" + this.getMaxScroll(),
                "BW = " + this.getButtonsWidth(),
                "BL0/BR0 = " + this.getButtonLeft(0) + "/" + this.getButtonRight(0),
                "BL-1/BR-1 = " + this.getButtonLeft(last) + "/" + this.getButtonRight(last)
        );

        // Make text half its size
        matrices.push();
        matrices.scale(0.5f, 0.5f, 0.5f);

        float lineHeight = textRenderer.fontHeight + 2;
        float startY = this.screen.height * 2 - lineHeight * debugInfo.size();
        for (int i = 0; i < debugInfo.size(); i++) {
            String text = debugInfo.get(i);
            textRenderer.draw(matrices, text, 0, startY + i * lineHeight, 0xFFCCCCCC);
        }

        matrices.pop();
    }
    // endregion

    // region positions
    private int getButtonLeft(int index) {
        return this.left + LEFT_RIGHT_PADDING - (int) this.getScrollAmount() + index * (BUTTON_WIDTH + BUTTON_MARGIN);
    }

    private int getButtonRight(int index) {
        return getButtonLeft(index) + BUTTON_WIDTH + LEFT_RIGHT_PADDING;
    }

    private int getScrollbarStartX() {
        return this.left + LEFT_RIGHT_PADDING;
    }

    private int getScrollbarEndX() {
        return this.right - LEFT_RIGHT_PADDING;
    }

    private int getScrollbarStartY() {
        return this.bottom - SCROLLBAR_HEIGHT - SCROLLBAR_MARGIN;
    }

    private int getScrollbarEndY() {
        return this.bottom - SCROLLBAR_MARGIN;
    }
    // endregion

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    @Override
    public List<? extends Element> children() {
        return this.children;
    }

    @Override
    public SelectionType getType() {
        // TODO
        return SelectionType.NONE;
    }
}
