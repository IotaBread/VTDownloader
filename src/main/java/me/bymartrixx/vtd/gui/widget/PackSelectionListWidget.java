package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.RenderUtil;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PackSelectionListWidget extends EntryListWidget<PackSelectionListWidget.AbstractEntry> {
    // DEBUG
    private static final boolean SHOW_DEBUG_INFO = false;
    private static final boolean DISABLE_ICONS = false;

    private static final Text ERROR_URL = Util.urlText(VTDMod.BASE_URL);
    private static final Text ERROR_HEADER = Text.translatable("vtd.packError.title")
            .formatted(Formatting.BOLD, Formatting.ITALIC);;
    private static final Text ERROR_BODY = Text.translatable("vtd.packError.body", ERROR_URL);
    private static final Text ERROR_TEXT = Text.empty().append(ERROR_HEADER).append("\n").append(ERROR_BODY);

    public static final int ITEM_HEIGHT = 32;
    private static final int WARNING_MARGIN = 6;
    private static final int WARNING_BG_MARGIN = 4;
    private static final int ROW_LEFT_RIGHT_MARGIN = 10;
    private static final int SCROLLBAR_LEFT_MARGIN = 4;
    private static final int TEXT_MARGIN = 2;
    private static final int ICON_MARGIN = 1;

    private static final int SELECTION_OUTLINE_COLOR = -0x7F7F80;

    private final Map<Category, List<AbstractEntry>> entryCache = new HashMap<>();
    private final VTDownloadScreen screen;
    private Category category;
    private boolean editable = true;

    private final List<OrderedText> errorLines;
    private final MultilineText errorText;

    private final PackSelectionHelper selectionHelper;

    public PackSelectionListWidget(MinecraftClient client, VTDownloadScreen screen, int width, int height, int top, int bottom,
                                   Category category, PackSelectionHelper selectionHelper) {
        super(client, width, height, top, bottom, ITEM_HEIGHT);
        this.screen = screen;
        this.category = category;
        this.selectionHelper = selectionHelper;

        this.errorLines = Util.getMultilineTextLines(client.textRenderer, ERROR_TEXT, 8, (int) (width / 1.5));
        this.errorText = Util.createMultilineText(client.textRenderer, ERROR_TEXT, 8, (int) (width / 1.5));

        this.children().addAll(getPackEntries(category));
    }

    public void setCategory(Category category) {
        this.category = category;

        this.replaceEntries(this.getPackEntries(category));
        this.setScrollAmount(this.getScrollAmount()); // Clamp scroll amount to the new max value
    }

    public void updateCategories(List<Category> categories) {
        this.selectionHelper.buildIncompatibilityGroups(categories);
    }

    private List<AbstractEntry> getPackEntries(Category category) {
        if (category == null) {
            return Collections.emptyList();
        }

        if (this.entryCache.containsKey(category)) {
            return this.entryCache.get(category);
        }

        List<AbstractEntry> entries = new ArrayList<>();

        if (category.hasWarning()) {
            //noinspection ConstantConditions
            entries.add(new WarningEntry(this.client, this.screen, category.getWarning()));
        }

        for (Pack pack : category.getPacks()) {
            PackEntry entry = new PackEntry(this, pack);
            entries.add(entry);
            if (this.selectionHelper.isSelected(pack)) {
                entry.selectionData.toggleSelection();
            }
        }

        this.entryCache.put(category, entries);

        return entries;
    }

    public void updateSelection() {
        for (List<AbstractEntry> categoryEntries : this.entryCache.values()) {
            for (AbstractEntry entry : categoryEntries) {
                if (entry instanceof PackEntry packEntry) {
                    if (this.selectionHelper.isSelected(packEntry.pack) != packEntry.selectionData.isSelected()) {
                        packEntry.selectionData.toggleSelection();
                    }
                }
            }
        }
    }

    private void toggleSelection(PackEntry entry) {
        if (this.editable) {
            this.selectionHelper.toggleSelection(entry);
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    private int getEntrySelectionColor(AbstractEntry entry) {
        if (entry instanceof PackEntry packEntry) {
            return this.selectionHelper.getSelectionColor(packEntry.getPack());
        }

        return PackSelectionHelper.DEFAULT_SELECTION_COLOR;
    }

    private int getCenterX() {
        return this.left + this.width / 2;
    }

    private int getCenterY() {
        return this.height / 2;
    }

    private static int getLineHeight(TextRenderer textRenderer) {
        return textRenderer.fontHeight + TEXT_MARGIN;
    }

    public void updateScreenWidth() {
        this.updateSize(this.screen.getLeftWidth(), this.height, this.top, this.bottom);
    }

    public void focusPack(Pack pack) {
        PackEntry entry = null;
        for (int i = 0; i < this.children().size(); i++) {
            AbstractEntry e = this.children().get(i);
            if (e instanceof PackEntry packEntry && packEntry.pack == pack) {
                entry = packEntry;
            }
        }

        if (entry != null) {
            this.centerScrollOn(entry);
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - ROW_LEFT_RIGHT_MARGIN * 2;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.left + getRowWidth() + ROW_LEFT_RIGHT_MARGIN + SCROLLBAR_LEFT_MARGIN;
    }

    @Override
    protected boolean isSelectedEntry(int index) {
        AbstractEntry entry = this.children().get(index);
        if (entry instanceof PackEntry packEntry) {
            return packEntry.selectionData.isSelected();
        }

        return false;
    }

    private int getTooltipWidth() {
        return (int) (this.width / 2.5);
    }

    private void moveFocus(MoveDirection direction) {
        int offset = direction == MoveDirection.UP ? -1 : 1;
        if (!this.children().isEmpty()) {
            int start = this.children().get(0) instanceof WarningEntry ? 1 : 0;
            AbstractEntry current = this.getFocused();
            int currentIndex = current != null ? this.children().indexOf(current) : -1;

            int index = MathHelper.clamp(currentIndex + offset, start, this.getEntryCount() - 1);
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

    @Override
    public void setFocused(@Nullable Element focused) {
        super.setFocused(focused);

        // Set focused element as list when focusing an entry
        if (focused != null && !this.isFocused()) {
            this.screen.setFocused(this);
        }
    }

    // region input callbacks
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.children().isEmpty()) {
            // Handle clicks when the error is shown
            int x = this.getCenterX();
            int textWidth = this.errorText.getMaxWidth();
            int startX = x - textWidth / 2;
            int endX = x + textWidth / 2;

            int y = this.getCenterY();
            TextRenderer textRenderer = this.client.textRenderer;
            int lineHeight = getLineHeight(textRenderer);
            int startY = y - lineHeight * 2;
            int endY = y + lineHeight * 3;

            if (mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY) {
                int l = (int) ((mouseY - startY) / lineHeight);
                OrderedText line = this.errorLines.get(l);
                Style style = Util.getStyleAt(textRenderer, x, mouseX, line);

                if (style != null && style.getClickEvent() != null
                        && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                    this.screen.handleTextClick(style);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
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
                if (focusedEntry instanceof PackEntry entry) {
                    this.toggleSelection(entry);
                }
            }
        }

        return false;
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        return !this.isFocused();
    }

    // endregion

    // region render
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if (this.children().isEmpty()) {
            this.renderError(matrices);
        }
    }

    @Override
    protected void renderEntry(MatrixStack matrices, int mouseX, int mouseY, float delta, int index, int entryX, int entryY, int width, int height) {
        AbstractEntry entry = this.getEntry(index);

        boolean focused = this.isFocused() && this.getFocused() == entry;
        if (this.isSelectedEntry(index)) {
            int outlineColor = focused ? 0xFFFFFFFF : SELECTION_OUTLINE_COLOR;
            int color = this.getEntrySelectionColor(entry);
            this.drawEntrySelectionHighlight(matrices, entryY, width, height, outlineColor, color);
        } else if (focused) {
            RenderUtil.drawOutline(matrices, entryX - 1, entryY - 1, width - 2, height + 2, 1, 0xFFFFFFFF);
        }

        entry.render(matrices, index, entryY, entryX, width, height, mouseX, mouseY,
                Objects.equals(this.getHoveredEntry(), entry), delta);
    }

    private void renderError(MatrixStack matrices) {
        TextRenderer textRenderer = this.client.textRenderer;

        int x = this.getCenterX();
        int y = this.getCenterY();
        int lineHeight = getLineHeight(textRenderer);

        this.errorText.drawCenterWithShadow(matrices, x, y - lineHeight * 2, lineHeight, 0xFFFFFF);
    }

    public void renderDebugInfo(MatrixStack matrices, int mouseX, int mouseY) {
        if (!SHOW_DEBUG_INFO) return;
        TextRenderer textRenderer = this.client.textRenderer;

        boolean hasCategory = this.category != null;
        List<String> debugInfo = List.of(
                "WxH = " + this.width + "x" + this.height,
                "EW = " + this.getRowWidth(),
                "C = " + (hasCategory ? this.category.getName() : "null"),
                "HI = " + (hasCategory ? this.category.isHardIncompatible() : "N/A"),
                "S = " + this.selectionHelper.getSelection(),
                "F = " + this.getFocused(),
                "E = " + this.editable,
                "IC = " + this.selectionHelper.usedColors,
                "MX/MY = " + mouseX + "/" + mouseY
        );

        RenderUtil.renderDebugInfo(matrices, textRenderer, this.left, this.height, debugInfo);
    }

    public void renderTooltips(MatrixStack matrices, int mouseX, int mouseY) {
        if (mouseY >= this.top && mouseY < this.bottom
                && mouseX >= this.left && mouseX < this.right
                && !this.screen.isCoveredByPopup(mouseX, mouseY)) {
            int width = this.getTooltipWidth();
            for (AbstractEntry entry : this.children()) {
                if (entry.renderTooltip(matrices, mouseX, mouseY, width)) {
                    break;
                }
            }
        }
    }
    // endregion

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Constants.RESOURCE_PACK_SCREEN_SUBTITLE);
    }

    public static class PackEntry extends AbstractEntry {
        private final Pack pack;
        private final Text name;

        private final PackSelectionListWidget widget;
        private final Identifier icon;
        private boolean downloadedIcon = false;
        private boolean iconExists;

        private List<Text> description;
        private MultilineText shortDescription;
        private int lastDescriptionWidth;

        protected PackSelectionData selectionData;

        public PackEntry(PackSelectionListWidget widget, Pack pack) {
            super(widget.client, widget.screen);
            this.pack = pack;
            this.name = Text.of(pack.getName()).copy().formatted(Formatting.BOLD);
            this.widget = widget;

            this.icon = VTDMod.getIconId(pack);

            this.iconExists = this.client.getTextureManager().getOrDefault(this.icon, null) != null;

            this.selectionData = new PackSelectionData(this.pack, widget.category);
        }

        private List<Text> getDescriptionLines(int maxWidth) {
            return this.wrapEscapedText(this.pack.getDescription(), maxWidth);
        }

        private List<Text> getDescription(int maxWidth) {
            if (this.description != null) {
                return this.description;
            }

            this.description = this.getDescriptionLines(maxWidth);

            return this.description;
        }

        private MultilineText getShortDescription(int maxWidth) {
            if (maxWidth == this.lastDescriptionWidth && this.shortDescription != null) {
                return this.shortDescription;
            }

            this.shortDescription = this.createMultilineText(this.getDescriptionLines(maxWidth));
            this.lastDescriptionWidth = maxWidth;

            return this.shortDescription;
        }

        public Pack getPack() {
            return this.pack;
        }

        private void downloadIcon() {
            if (this.downloadedIcon || this.iconExists) return;

            this.downloadedIcon = true;

            VTDMod.downloadIcon(this.pack).whenCompleteAsync((success, throwable) -> {
                if (throwable != null) {
                    VTDMod.LOGGER.error("Failed to download icon for pack {}", this.pack.getName(), throwable);
                    return;
                }

                if (success) {
                    this.iconExists = this.client.getTextureManager().getOrDefault(this.icon, null) != null;
                } else {
                    VTDMod.LOGGER.error("Failed to download icon for pack {}", this.pack.getName());
                }
            });
        }

        @Override
        protected List<Text> getTooltipText(int width) {
            return this.getDescription(width);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                this.widget.toggleSelection(this);
                return true;
            }

            return false;
        }

        // region entryRender
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = this.client.textRenderer;
            int iconSize = entryHeight - ICON_MARGIN * 2;
            int centerX = x + (iconSize + entryWidth) / 2; // center over area left to the icon
            drawCenteredText(matrices, textRenderer, this.name, centerX, y, 0xFFFFFF);

            this.renderDescription(matrices, centerX, y + getLineHeight(textRenderer), entryWidth - iconSize);
            if (!DISABLE_ICONS) this.renderIcon(matrices, x + ICON_MARGIN, y + ICON_MARGIN, iconSize);
        }

        private void renderDescription(MatrixStack matrices, int x, int y, int width) {
            getShortDescription(width - TEXT_MARGIN).drawCenterWithShadow(matrices, x, y);
        }

        private void renderIcon(MatrixStack matrices, int x, int y, int size) {
            downloadIcon();
            if (!this.iconExists) return;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, this.icon);
            RenderSystem.enableBlend();

            drawTexture(matrices, x, y, 0.0F, 0.0F, size, size, size, size);

            RenderSystem.disableBlend();
        }
        // endregion


        @Override
        public String toString() {
            return "Pack " + this.pack.getName();
        }
    }

    public static class WarningEntry extends AbstractEntry {
        private final Category.Warning warning;
        private final int color;

        private List<Text> textLines;
        private MultilineText text;

        public WarningEntry(MinecraftClient client, VTDownloadScreen screen, Category.Warning warning) {
            super(client, screen);
            this.warning = warning;

            this.color = Util.parseColor(warning.getColor());
        }

        private List<Text> getWrappedText(int maxWidth) {
            return this.wrapEscapedText(this.warning.getText(), maxWidth);
        }

        private List<Text> getTextLines(int maxWidth) {
            if (this.textLines != null) {
                return this.textLines;
            }

            this.textLines = this.getWrappedText(maxWidth);
            return this.textLines;
        }

        private MultilineText getText(int maxWidth) {
            if (this.text != null) {
                return this.text;
            }

            this.text = this.createMultilineText(this.getWrappedText(maxWidth));
            return this.text;
        }

        @Override
        protected List<Text> getTooltipText(int width) {
            return this.getTextLines(width);
        }

        // region warningRender
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.renderBackground(matrices, x + WARNING_BG_MARGIN, y + WARNING_BG_MARGIN, entryWidth - WARNING_BG_MARGIN * 2, entryHeight - WARNING_BG_MARGIN * 2);

            int width = entryWidth - WARNING_MARGIN * 2;
            this.renderText(matrices, x + WARNING_MARGIN + width / 2, y + WARNING_MARGIN, width);
        }

        private void renderBackground(MatrixStack matrices, int x, int y, int width, int height) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            fill(matrices, x, y, x + width, y + height, this.color);
        }

        private void renderText(MatrixStack matrices, int x, int y, int width) {
            this.getText(width).drawCenterWithShadow(matrices, x, y);
        }
        // endregion


        @Override
        public String toString() {
            return "Warning";
        }
    }

    public static abstract class AbstractEntry extends EntryListWidget.Entry<AbstractEntry> {
        protected final MinecraftClient client;
        protected final VTDownloadScreen screen;

        protected AbstractEntry(MinecraftClient client, VTDownloadScreen screen) {
            this.client = client;
            this.screen = screen;
        }

        protected final List<Text> wrapEscapedText(String text, int maxWidth) {
            return Util.wrapText(this.client.textRenderer, Util.removeHtmlTags(text), maxWidth);
        }

        protected final MultilineText createMultilineText(List<Text> lines) {
            return Util.createMultilineText(this.client.textRenderer, lines, 2);
        }

        protected abstract List<Text> getTooltipText(int width);

        // region baseEntryRender
        protected boolean renderTooltip(MatrixStack matrices, int mouseX, int mouseY, int width) {
            if (this.isMouseOver(mouseX, mouseY)) {
                this.screen.renderTooltip(matrices, this.getTooltipText(width), mouseX, mouseY);
                return true;
            }

            return false;
        }
        // endregion
    }
}
