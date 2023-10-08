package me.bymartrixx.vtd.gui.widget;

import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.list.EntryListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

public class SelectedPacksListWidget extends EntryListWidget<SelectedPacksListWidget.AbstractEntry> {
    private static final Text HEADER = Text.translatable("vtd.selectedPacks")
            .formatted(Formatting.BOLD, Formatting.UNDERLINE);

    private static final int ITEM_HEIGHT = 16;
    private static final int HEADER_HEIGHT = 16;
    private static final int ROW_LEFT_RIGHT_MARGIN = 2;
    private static final int SCROLLBAR_LEFT_MARGIN = 4;

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
        this.setRenderBackground(false); // Rendered at #renderBackground
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
            int i = this.getLastChildEntryIndex(category);
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
            //noinspection EqualsBetweenInconvertibleTypes - CategoryEntry overrides equals()
            if (this.children().get(i).equals(category)) {
                return i;
            }
        }

        return -1;
    }

    private CategoryEntry getOrCreateCategoryEntry(Category category) {
        int i = this.getCategoryEntryIndex(category);
        if (i == -1) {
            CategoryEntry entry;
            if (category instanceof Category.SubCategory subCategory) {
                entry = new SubCategoryEntry(this, subCategory);
                CategoryEntry parentEntry = this.getOrCreateCategoryEntry(subCategory.getParent());
                int index = this.getLastChildIndex(parentEntry.getCategory());
                if (index != -1) {
                    this.children().add(index + 1, entry);
                } else {
                    this.addEntry(entry);
                }
            } else {
                entry = new CategoryEntry(this, category);
                this.addEntry(entry);
            }
            return entry;
        }

        return (CategoryEntry) this.children().get(i);
    }

    private int getPackEntryIndex(Pack pack) {
        for (int i = 0; i < this.children().size(); i++) {
            //noinspection EqualsBetweenInconvertibleTypes - PackEntry overrides equals()
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

    private int getLastChildEntryIndex(Category category) {
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

    private int getLastSubCategoryIndex(Category category) {
        int index = -1;
        int i = this.getCategoryEntryIndex(category);
        if (i != -1 && !(category instanceof Category.SubCategory)) {
            for (i++; i < this.children().size(); i++) {
                AbstractEntry entry = this.children().get(i);
                if (entry instanceof SubCategoryEntry subCategoryEntry) {
                    if (subCategoryEntry.getParentCategory().equals(category)) {
                        index = i;
                    }
                    continue;
                }

                break;
            }
        }

        return index;
    }

    private int getLastChildIndex(Category category) {
        int subCat = this.getLastSubCategoryIndex(category);
        if (subCat != -1) {
            SubCategoryEntry entry = (SubCategoryEntry) this.children().get(subCat);
            int subCatIndex = this.getLastChildEntryIndex(entry.getParentCategory());
            if (subCatIndex != -1) {
                return subCatIndex;
            }
        }

        int index = this.getLastChildEntryIndex(category);
        if (index == -1 && subCat != -1) {
            return subCat;
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

    // private void moveFocus(MoveDirection direction) {
    //     int offset = direction == MoveDirection.UP ? -1 : 1;
    //     if (!this.children().isEmpty()) {
    //         AbstractEntry current = this.getFocused();
    //         int currentIndex = current != null ? this.children().indexOf(current) : -1;
    //
    //         int index = MathHelper.clamp(currentIndex + offset, 0, this.getEntryCount() - 1);
    //         if (index != currentIndex) {
    //             AbstractEntry entry = this.getEntry(index);
    //             this.setFocused(entry);
    //             this.ensureVisible(entry);
    //         }
    //     }
    // }

    @Override
    public boolean isFocused() {
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
            // if (keyCode == GLFW.GLFW_KEY_DOWN) {
            //     this.moveFocus(MoveDirection.DOWN);
            //     return true;
            // } else if (keyCode == GLFW.GLFW_KEY_UP) {
            //     this.moveFocus(MoveDirection.UP);
            //     return true;
            // }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                AbstractEntry focusedEntry = this.getFocused();
                if (focusedEntry != null) {
                    focusedEntry.selectEntry();
                }
            }
        }

        return false;
    }

    // @Override
    // public boolean changeFocus(boolean lookForwards) {
    //     if (this.extended) {
    //         return !this.isFocused();
    //     }
    //
    //     return false;
    // }
    // endregion

    // region render
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!this.extended) {
            return;
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    protected void renderHeader(GuiGraphics graphics, int x, int y) {
        graphics.drawCenteredShadowedText(this.client.textRenderer, HEADER, this.getRowLeft() + this.width / 2, y, 0xFFFFFFFF);
    }

    @Override
    protected void renderList(GuiGraphics graphics, int x, int y, float delta) {
        super.renderList(graphics, x, y, delta);

        this.renderBackground(graphics);
    }

    private void renderBackground(GuiGraphics graphics) {
        // @see EntryListWidget#render -> if (this.renderBackground)
        int size = HORIZONTAL_SHADOWS_SIZE;
        graphics.fillGradient(RenderLayer.getGuiOverlay(), this.left, this.top, this.right, this.top + size, 0xFF000000, 0x00000000, 0);
        graphics.fillGradient(RenderLayer.getGuiOverlay(), this.left, this.bottom - size, this.right, this.bottom, 0x00000000, 0xFF000000, 0);
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
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            graphics.drawShadowedText(this.client.textRenderer, this.getText(), x, y, 0xFFFFFF);
        }
    }

    public static class CategoryEntry extends AbstractEntry {
        protected final Category category;
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

        public Category getCategory() {
            return this.category;
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

    public static class SubCategoryEntry extends CategoryEntry {
        public SubCategoryEntry(SelectedPacksListWidget widget, Category.SubCategory category) {
            super(widget, category);
        }

        @Override
        protected String getTextString() {
            return "| " + super.getTextString();
        }

        @Override
        public Category.SubCategory getCategory() {
            return (Category.SubCategory) super.getCategory();
        }

        public Category getParentCategory() {
            return this.getCategory().getParent();
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
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            graphics.drawShadowedText(this.client.textRenderer, this.getText(), x, y, this.getColor());
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
