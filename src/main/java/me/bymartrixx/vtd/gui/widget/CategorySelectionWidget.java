package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectionWidget extends AbstractParentElement implements Drawable, Selectable {
    private static final float TEXTURE_SIZE = 32.0F;

    private static final int LEFT_RIGHT_PADDING = 2;
    private static final int TOP_BOTTOM_PADDING = 2;
    private static final int LEFT_RIGHT_MARGIN = 4;

    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_MARGIN = 10;

    private final List<CategoryButtonWidget> children = new ArrayList<>();
    private List<Category> categories;
    private final VTDownloadScreen screen;
    private final int y;

    private final int height;
    private final int width;
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;

    private final int startX;
    private final int endX;

    public CategorySelectionWidget(VTDownloadScreen screen, int y) {
        this.screen = screen;
        this.y = y;

        this.height = TOP_BOTTOM_PADDING * 2 + BUTTON_HEIGHT;
        this.width = this.screen.width - LEFT_RIGHT_MARGIN * 2;
        this.left = LEFT_RIGHT_MARGIN;
        this.top = this.y;
        this.right = this.left + this.width;
        this.bottom = this.top + this.height;

        this.startX = 0;
        this.endX = this.right + LEFT_RIGHT_MARGIN;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void initCategoryButtons() {
        for (Category category : this.categories) {
            CategoryButtonWidget button = createCategoryButton(category);
            this.children.add(button);
        }
    }

    private CategoryButtonWidget createCategoryButton(Category category) {
        Text text = Text.literal(category.getName());
        return new CategoryButtonWidget(BUTTON_WIDTH, BUTTON_HEIGHT, text, /*button -> {
            VTDMod.LOGGER.info("Clicked button for {}", category.getName());
        },*/ category);
    }

    private int getCategoryLeftOffset(int index) {
        // TODO: scrolling
        return LEFT_RIGHT_MARGIN + LEFT_RIGHT_PADDING + index * (BUTTON_WIDTH + BUTTON_MARGIN);
    }

    private int getCategoryRightOffset(int index) {
        return getCategoryLeftOffset(index) + BUTTON_WIDTH + LEFT_RIGHT_PADDING;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.renderCategories(matrices, mouseX, mouseY, delta);
        this.renderMargin();
    }

    private void renderBackground() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // TODO: Scrolling, V should be offset by scrolling

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.bottom, 0.0)
                .uv(0.0F, this.height / TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0)
                .uv(this.width / TEXTURE_SIZE, this.height / TEXTURE_SIZE)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.right, this.top, 0.0)
                .uv(this.width / TEXTURE_SIZE, 0.0F)
                .color(32, 32, 32, 255)
                .next();
        bufferBuilder.vertex(this.left, this.top, 0.0)
                .uv(0.0F, 0.0F)
                .color(32, 32, 32, 255)
                .next();
        tessellator.draw();
    }

    private void renderCategories(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        for (int i = 0; i < this.children.size(); i++) {
            CategoryButtonWidget button = this.children.get(i);
            int left = getCategoryLeftOffset(i);
            int right = getCategoryRightOffset(i);

            // Render only if the button is at least partially visible (else it'd be out of the screen)
            if (right > this.left && left < this.right) {
                button.render(matrices, left, this.top + TOP_BOTTOM_PADDING, mouseX, mouseY, delta);
            }
        }
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
                .uv(0.0F, height / TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, this.bottom, 0.0)
                .uv(LEFT_RIGHT_MARGIN / TEXTURE_SIZE, height / TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, this.top, 0.0)
                .uv(LEFT_RIGHT_MARGIN / TEXTURE_SIZE, 0.0F)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.startX, this.top, 0.0)
                .uv(0.0F, 0.0F)
                .color(64, 64, 64, 255)
                .next();

        // Right side
        bufferBuilder.vertex(this.right, this.bottom, 0.0)
                .uv(0.0F, height / TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.endX, this.bottom, 0.0)
                .uv(LEFT_RIGHT_MARGIN /  TEXTURE_SIZE, height / TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.endX, this.top, 0.0)
                .uv(LEFT_RIGHT_MARGIN / TEXTURE_SIZE, 0.0F)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.right, this.top, 0.0)
                .uv(0.0F, 0.0F)
                .color(64, 64, 64, 255)
                .next();
        tessellator.draw();
    }

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
