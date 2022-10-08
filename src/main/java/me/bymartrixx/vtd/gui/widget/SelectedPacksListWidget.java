package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

public class SelectedPacksListWidget extends EntryListWidget<SelectedPacksListWidget.AbstractEntry> {
    private static final Text HEADER = Text.translatable("vtd.selectedPacks")
            .formatted(Formatting.BOLD, Formatting.UNDERLINE);

    private static final float BACKGROUND_TEXTURE_SIZE = 32.0F;
    private static final int ITEM_HEIGHT = 16;
    private static final int HEADER_HEIGHT = 16;
    private static final int ROW_LEFT_RIGHT_MARGIN = 2;
    private static final int SCROLLBAR_LEFT_MARGIN = 4;

    private static final int HORIZONTAL_SHADOWS_BACKGROUND_Z = -100;
    private static final int HORIZONTAL_SHADOWS_SIZE = 4;

    private final VTDownloadScreen screen;
    private final PackSelectionHelper selectionHelper;
    private boolean extended = false;

    public SelectedPacksListWidget(VTDownloadScreen screen, MinecraftClient client, int width, int top,
                                   int bottom, int left, PackSelectionHelper selectionHelper) {
        super(client, width, screen.height, top, bottom, ITEM_HEIGHT);
        this.screen = screen;
        this.selectionHelper = selectionHelper;

        this.setLeftPos(left);
        this.setRenderHorizontalShadows(false); // Rendered at #renderHorizontalShadows
        this.setRenderHeader(true, HEADER_HEIGHT);

        selectionHelper.addCallback(this::updateSelection);
        this.addPacks(selectionHelper.getSelectedPacks());
    }

    public boolean isExtended() {
        return this.extended;
    }

    public boolean toggleExtended() {
        this.extended = !this.extended;
        return this.extended;
    }

    public void update() {
        this.clearEntries();

        this.addPacks(this.selectionHelper.getSelectedPacks());
    }

    private void updateSelection(Pack pack, Category category, boolean selected) {
        if (selected) {
            CategoryEntry categoryEntry = this.getOrCreateCategoryEntry(category);
            PackEntry entry = this.getPackEntry(pack, category);
            int i = this.getLastChildIndex(category);
            if (i == -1) {
                i = this.children().indexOf(categoryEntry);
            }

            this.children().add(i + 1, entry);
            return;
        }

        int i = this.getPackEntryIndex(pack);
        if (i == -1) {
            return;
        }

        this.children().remove(i);

        int categoryIndex = this.getCategoryEntryIndex(category);
        if (categoryIndex != -1) {
            int lastChildIndex = this.getLastChildIndex(category);
            if (lastChildIndex == -1) {
                this.children().remove(categoryIndex);
            }
        }

        // Update scrollbar
        this.setScrollAmount(this.getScrollAmount());
    }

    private void addPacks(Map<Category, List<Pack>> packs) {
        for (Category category : packs.keySet()) {
            packs.get(category).forEach(pack -> updateSelection(pack, category, true));
        }
    }

    private int getCategoryEntryIndex(Category category) {
        for (int i = 0; i < this.children().size(); i++) {
            if (this.children().get(i).equals(category)) {
                return i;
            }
        }

        return -1;
    }

    private CategoryEntry getOrCreateCategoryEntry(Category category) {
        int i = this.getCategoryEntryIndex(category);
        if (i == -1) {
            CategoryEntry entry = new CategoryEntry(this, category);
            this.addEntry(entry);
            return entry;
        }

        return (CategoryEntry) this.children().get(i);
    }

    private int getPackEntryIndex(Pack pack) {
        for (int i = 0; i < this.children().size(); i++) {
            if (this.children().get(i).equals(pack)) {
                return i;
            }
        }

        return -1;
    }

    private PackEntry getPackEntry(Pack pack, Category category) {
        int i = this.getPackEntryIndex(pack);
        if (i == -1) {
            return new PackEntry(this, category, pack);
        }

        return (PackEntry) this.children().get(i);
    }

    private int getLastChildIndex(Category category) {
        int index = -1;
        int i = this.getCategoryEntryIndex(category);
        if (i != -1) {
            for (i++; i < this.children().size(); i++) {
                AbstractEntry entry = this.children().get(i);
                if (entry instanceof PackEntry packEntry) {
                    if (packEntry.category.equals(category)) {
                        index = i;
                    }
                    continue;
                }

                break;
            }
        }

        return index;
    }

    @Override
    protected boolean isSelectedEntry(int index) {
        return this.getFocused() == this.getEntry(index);
    }

    @Override
    public int getRowWidth() {
        return this.width - ROW_LEFT_RIGHT_MARGIN * 2;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.left + this.getRowWidth() + SCROLLBAR_LEFT_MARGIN;
    }

    private void moveFocus(MoveDirection direction) {
        int offset = direction == MoveDirection.UP ? -1 : 1;
        if (!this.children().isEmpty()) {
            AbstractEntry current = this.getFocused();
            int currentIndex = current != null ? this.children().indexOf(current) : -1;

            int index = MathHelper.clamp(currentIndex + offset, 0, this.getEntryCount() - 1);
            if (index != currentIndex) {
                AbstractEntry entry = this.getEntry(index);
                this.setFocused(entry);
                this.ensureVisible(entry);
            }
        }
    }

    @Override
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    // region input callbacks
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.extended && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.moveFocus(MoveDirection.DOWN);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                this.moveFocus(MoveDirection.UP);
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                AbstractEntry focusedEntry = this.getFocused();
                if (focusedEntry != null) {
                    focusedEntry.selectEntry();
                }
            }
        }

        return false;
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        if (this.extended) {
            return !this.isFocused();
        }

        return false;
    }
    // endregion

    // region render
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.extended) {
            return;
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void renderHeader(MatrixStack matrices, int x, int y, Tessellator tessellator) {
        drawCenteredText(matrices, this.client.textRenderer, HEADER, this.getRowLeft() + this.width / 2, y, 0xFFFFFFFF);
    }

    @Override
    protected void renderList(MatrixStack matrices, int x, int y, float delta) {
        super.renderList(matrices, x, y, delta);

        this.renderHorizontalShadows();
    }

    private void renderHorizontalShadows() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GlConst.GL_ALWAYS);

        int z = HORIZONTAL_SHADOWS_BACKGROUND_Z;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.top, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), this.top, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, this.top / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), 0.0, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, 0.0F)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, 0.0, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, 0.0F)
                .color(64, 64, 64, 255)
                .next();

        bufferBuilder.vertex(this.left, this.height, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.height / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), this.height, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, this.height / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex((this.left + this.width), this.bottom, z)
                .uv((this.left + this.width) / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        bufferBuilder.vertex(this.left, this.bottom, z)
                .uv(this.left / BACKGROUND_TEXTURE_SIZE, this.bottom / BACKGROUND_TEXTURE_SIZE)
                .color(64, 64, 64, 255)
                .next();
        tessellator.draw();

        RenderSystem.depthFunc(GlConst.GL_LEQUAL);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int size = HORIZONTAL_SHADOWS_SIZE;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(this.left, (this.top + size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        bufferBuilder.vertex(this.right, (this.top + size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        bufferBuilder.vertex(this.right, this.top, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.left, this.top, 0.0)
                .color(0, 0, 0, 255)
                .next();

        bufferBuilder.vertex(this.left, this.bottom, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0)
                .color(0, 0, 0, 255)
                .next();
        bufferBuilder.vertex(this.right, (this.bottom - size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        bufferBuilder.vertex(this.left, (this.bottom - size), 0.0)
                .color(0, 0, 0, 0)
                .next();
        tessellator.draw();
    }
    // endregion

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, HEADER);
    }

    public static abstract class AbstractEntry extends Entry<AbstractEntry> {
        protected final MinecraftClient client;
        protected final SelectedPacksListWidget widget;
        private Text text;

        protected AbstractEntry(SelectedPacksListWidget widget) {
            this.client = widget.client;
            this.widget = widget;
        }

        protected abstract String getTextString();

        protected abstract void selectEntry();

        protected Text getText() {
            if (this.text != null) {
                return this.text;
            }

            this.text = Text.of(this.getTextString());
            return this.text;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            drawTextWithShadow(matrices, this.client.textRenderer, this.getText(), x, y, 0xFFFFFF);
        }
    }

    public static class CategoryEntry extends AbstractEntry {
        private final Category category;
        private long lastClickTime = -1;

        public CategoryEntry(SelectedPacksListWidget widget, Category category) {
            super(widget);
            this.category = category;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                long time = System.currentTimeMillis();
                if (time <= this.lastClickTime + DOUBLE_CLICK_THRESHOLD) {
                    this.selectEntry();
                }

                this.lastClickTime = time;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected String getTextString() {
            return this.category.getName();
        }

        @Override
        protected void selectEntry() {
            this.widget.screen.selectCategory(this.category);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof CategoryEntry entry) {
                return this.category.equals(entry.category);
            } else if (obj instanceof Category c) {
                return this.category.equals(c);
            } else {
                return super.equals(obj);
            }
        }
    }

    public static class PackEntry extends AbstractEntry {
        private final Category category;
        private final Pack pack;

        private int color = -1;
        private int lastChildrenCount = -1;
        private long lastClickTime = -1;

        public PackEntry(SelectedPacksListWidget widget, Category category, Pack pack) {
            super(widget);
            this.category = category;
            this.pack = pack;
        }

        private int calculateColor() {
            int color = this.widget.selectionHelper.getSelectionColor(this.pack);

            return color != PackSelectionHelper.DEFAULT_SELECTION_COLOR ? color : 0xFFFFFFFF;
        }

        private int getColor() {
            if (this.color != -1 && this.widget.children().size() == this.lastChildrenCount) {
                return this.color;
            }

            this.lastChildrenCount = this.widget.children().size();
            this.color = this.calculateColor();
            return this.color;
        }

        @Override
        protected String getTextString() {
            return "> " + this.pack.getName();
        }

        @Override
        protected void selectEntry() {
            this.widget.screen.goToPack(this.pack, this.category);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                long time = System.currentTimeMillis();
                if (time <= this.lastClickTime + DOUBLE_CLICK_THRESHOLD) {
                    this.selectEntry();
                }

                this.lastClickTime = time;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            drawTextWithShadow(matrices, this.client.textRenderer, this.getText(), x, y, this.getColor());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof PackEntry entry) {
                return this.pack.equals(entry.pack);
            } else if (obj instanceof Pack p) {
                return this.pack.equals(p);
            } else {
                return super.equals(obj);
            }
        }
    }
}
