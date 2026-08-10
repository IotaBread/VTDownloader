package me.bymartrixx.vtd.gui.widget;

import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategorySelectionWidget extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
    // DEBUG
    private static final boolean SHOW_DEBUG_INFO = false;

    private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final int BACKGROUND_TEXTURE_SIZE = 32;

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

    private double scrollAmount;
    private boolean scrolling;

    public CategorySelectionWidget(VTDownloadScreen screen, int y) {
        this.screen = screen;
        this.y = y;

        this.calculateDimensions();
    }

    public void init(List<Category> categories, Category selectedCategory) {
        this.setCategories(categories);
        this.initCategoryButtons();
        this.setSelectedCategory(selectedCategory);
    }

    public void updateCategories(List<Category> categories) {
        this.categories = new ArrayList<>(categories);
        this.children.clear();
        this.initCategoryButtons();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void initCategoryButtons() {
        for (Category category : this.categories) {
            CategoryButtonWidget button = this.getOrCreateCategoryButton(category);
            this.children.add(button);
        }

        this.calculateDimensions();
    }

    private CategoryButtonWidget getOrCreateCategoryButton(Category category) {
        if (this.categoryButtons.containsKey(category)) {
            return this.categoryButtons.get(category);
        }

        Component text = Component.literal(category.getName());
        CategoryButtonWidget button = new CategoryButtonWidget(this.screen, BUTTON_WIDTH, BUTTON_HEIGHT, text, category);
        this.categoryButtons.put(category, button);
        return button;
    }

    public void setSelectedCategory(Category category) {
        this.categoryButtons.forEach((c, button) -> button.setSelected(c == category));
        CategoryButtonWidget button = this.categoryButtons.get(category);
        if (button != null) {
            this.ensureVisible(button);
        }
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
        return this.getButtonsWidth() > this.width;
    }

    public void updateScreenWidth() {
        double scroll = this.getScrollAmount() / this.getMaxScroll();
        this.calculateDimensions();
        this.setScrollAmount(Math.round(scroll * this.getMaxScroll()));
    }

    private void calculateDimensions() {
        this.width = this.screen.getLeftWidth() - LEFT_RIGHT_MARGIN * 2;
        boolean scrollbar = this.shouldHaveScrollbar();
        this.height = TOP_BOTTOM_PADDING * 2 + BUTTON_HEIGHT + (scrollbar ? SCROLLBAR_HEIGHT + SCROLLBAR_MARGIN : 0);
        this.left = LEFT_RIGHT_MARGIN;
        this.top = this.y;
        this.right = this.left + this.width;
        this.bottom = this.top + this.height;
    }

    private void scroll(int amount) {
        this.setScrollAmount(this.getScrollAmount() + amount);
    }

    private int getMaxScroll() {
        return Math.max(0, this.getButtonsWidth() - this.width + LEFT_RIGHT_PADDING * 2);
    }

    private double getScrollAmount() {
        return this.scrollAmount;
    }

    private void setScrollAmount(double scrollAmount) {
        this.scrollAmount = Mth.clamp(scrollAmount, 0.0, this.getMaxScroll());
    }

    private void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == GLFW.GLFW_MOUSE_BUTTON_1 &&
                mouseX >= this.getScrollbarStartX() && mouseX < this.getScrollbarEndX() &&
                mouseY >= this.getScrollbarStartY() && mouseY < this.getScrollbarEndY();
    }

    private void ensureVisible(CategoryButtonWidget button) {
        int buttonLeft = this.getButtonLeft(this.children.indexOf(button));
        int scrollAmount = buttonLeft - this.left - LEFT_RIGHT_PADDING * 2 - BUTTON_WIDTH;
        if (scrollAmount < 0) {
            this.scroll(scrollAmount);
        }

        scrollAmount = this.right - buttonLeft - BUTTON_WIDTH * 2;
        if (scrollAmount < 0) {
            this.scroll(-scrollAmount);
        }
    }

    // @Override
    // public boolean changeFocus(boolean lookForwards) {
    //     boolean focused = super.changeFocus(lookForwards);
    //     if (focused) {
    //         this.ensureVisible((CategoryButtonWidget) this.getFocused());
    //     }
    //
    //     return focused;
    // }

    // region input callbacks
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        this.updateScrollingState(event.x(), event.y(), event.button());

        if (!this.isMouseOver(event.x(), event.y())) {
            return false;
        } else {
            return super.mouseClicked(event, bl) || this.scrolling;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1 && this.scrolling) {
            // Dragging scrollbar
            if (event.x() < this.left) {
                this.setScrollAmount(0);
            } else if (event.y() > this.right) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double maxScroll = Math.max(1, this.getMaxScroll());
                int width = this.getScrollbarEndX() - this.getScrollbarStartX();
                int barSize = (this.width * this.width) / this.getButtonsWidth();
                barSize = Mth.clamp(barSize, SCROLLBAR_MIN_WIDTH, width);

                double factor = Math.max(1, maxScroll / (this.width - barSize));

                this.setScrollAmount(this.getScrollAmount() + deltaX * factor);
            }

            return true;
        }

        return false;
    }

    // Only called if isMouseOver is true; from Screen#mouseScrolled
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double amount = Math.abs(horizontalAmount) > Math.abs(verticalAmount) ? horizontalAmount : verticalAmount;
        this.setScrollAmount(this.getScrollAmount() - amount * BUTTON_WIDTH / 2);
        return true;
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        this.renderListBackground(graphics);
        graphics.enableScissor(this.left, this.top, this.right, this.bottom);
        this.renderCategories(graphics, mouseX, mouseY, delta);
        graphics.disableScissor();
        this.renderSeparators(graphics);
        this.renderScrollbar(graphics);
    }

    // @see EntryListWidget#drawBackground
    private void renderListBackground(GuiGraphicsExtractor graphics) {
        Identifier texture = Minecraft.getInstance().level == null ? BACKGROUND_TEXTURE : INWORLD_BACKGROUND_TEXTURE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                this.left, this.top, this.right + (int) this.getScrollAmount(), this.bottom,
                this.width, this.height, BACKGROUND_TEXTURE_SIZE, BACKGROUND_TEXTURE_SIZE);
    }

    private void renderCategories(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        for (int i = 0; i < this.children.size(); i++) {
            CategoryButtonWidget button = this.children.get(i);
            int left = getButtonLeft(i);
            int right = getButtonRight(i);

            // Render only if the button is at least partially visible (else it'd be out of the screen)
            if (right > this.left && left < this.right) {
                button.render(graphics, left, this.top + TOP_BOTTOM_PADDING, mouseX, mouseY, delta);
            }
        }
    }

    private void renderSeparators(GuiGraphicsExtractor graphics) {
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.rotate((float) Math.PI / 2.0f); // 90 degrees

        Identifier leftSeparator = Minecraft.getInstance().level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        Identifier rightSeparator = Minecraft.getInstance().level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        graphics.blit(RenderPipelines.GUI_TEXTURED, leftSeparator, this.top, -this.left, 0.0f, 0.0f, this.height, 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, rightSeparator, this.top, -this.right - 2, 0.0f, 0.0f, this.height, 2, 32, 2);

        matrices.popMatrix();
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics) {
        if (this.shouldHaveScrollbar()) {
            int startX = this.getScrollbarStartX();
            int endX = this.getScrollbarEndX();
            int startY = this.getScrollbarStartY();
            int endY = this.getScrollbarEndY();

            int width = endX - startX;
            int size = (this.width * this.width) / this.getButtonsWidth();
            size = Mth.clamp(size, SCROLLBAR_MIN_WIDTH, width);

            int x = (int) this.getScrollAmount() * (width - size) / this.getMaxScroll() + startX;
            if (x < startX) {
                x = startX;
            }

            graphics.fill(startX, startY, endX, endY, 0xFF000000); // Slider area
            graphics.fill(x, startY, x + size, endY, 0xFF808080); // Scroll bar
            graphics.fill(x, startY, x + size - 1, endY - 1, 0xFFC0C0C0); // Scroll bar highlight
        }
    }

    public void renderDebugInfo(GuiGraphicsExtractor graphics) {
        if (!SHOW_DEBUG_INFO) return;
        Minecraft client = Minecraft.getInstance();
        Font textRenderer = client.font;

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

        RenderUtil.renderDebugInfo(graphics, textRenderer, this.left, this.height, debugInfo);
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
    public void updateNarration(NarrationElementOutput builder) {
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }
}
