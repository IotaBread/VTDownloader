package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.texture.NativeImage;
import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.access.TextureManagerAccess;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.RenderUtil;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.list.EntryListWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.render.RenderPipelines;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_emchntzr;
import net.minecraft.unmapped.C_gitqeeaa;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PackSelectionListWidget extends EntryListWidget<PackSelectionListWidget.AbstractEntry> {
    // DEBUG
    private static final boolean SHOW_DEBUG_INFO = false;
    private static final boolean DEBUG_ERROR_TEXT = false;
    private static final boolean DISABLE_ICONS = false;

    private static final Text ERROR_URL = Util.urlText(VTDMod.BASE_URL);
    private static final Text ERROR_HEADER = Text.translatable("vtd.packError.title")
            .formatted(Formatting.BOLD, Formatting.ITALIC);
    private static final Text ERROR_BODY = Text.translatable("vtd.packError.body", ERROR_URL);
    private static final Text ERROR_TEXT = Text.empty().append(ERROR_HEADER).append("\n").append(ERROR_BODY);

    public static final int ITEM_HEIGHT = 48;
    private static final int WARNING_MARGIN = 6;
    private static final int WARNING_BG_MARGIN = 4;
    private static final int ROW_LEFT_RIGHT_MARGIN = 10;
    private static final int SCROLLBAR_LEFT_MARGIN = 4;
    private static final int TEXT_MARGIN = 2;
    private static final int ICON_MARGIN = 1;

    private static final int SELECTION_OUTLINE_COLOR = 0xFF808080;

    private final Map<Category, List<AbstractEntry>> entryCache = new HashMap<>();
    private final VTDownloadScreen screen;
    private Category category;
    private boolean editable = true;

    private final MultilineText errorText;

    private final PackSelectionHelper selectionHelper;

    public PackSelectionListWidget(MinecraftClient client, VTDownloadScreen screen, int width, int height, int y,
                                   Category category, PackSelectionHelper selectionHelper) {
        super(client, width, height, y, ITEM_HEIGHT);
        this.screen = screen;
        this.category = category;
        this.selectionHelper = selectionHelper;

        this.errorText = Util.createMultilineText(client.textRenderer, ERROR_TEXT, 8, (int) (width / 1.5));

        // In 1.21.10+, children() returns an unmodifiable collection
        this.replaceEntries(this.getPackEntries(category));
    }

    public void setCategory(Category category) {
        this.category = category;

        this.setFocusedChild(null);
        this.replaceEntries(this.getPackEntries(category));
        this.method_44382(0.0);
    }

    public void updateCategories(List<Category> categories) {
        this.selectionHelper.buildIncompatibilityGroups(categories);
    }

    private List<AbstractEntry> getPackEntries(Category category) {
        if (category == null || DEBUG_ERROR_TEXT) {
            return Collections.emptyList();
        }

        if (this.entryCache.containsKey(category)) {
            return this.entryCache.get(category);
        }

        List<AbstractEntry> entries = new ArrayList<>();

        if (category.hasWarning()) {
            //noinspection ConstantConditions
            entries.add(new WarningEntry(this, category.getWarning()));
        }

        if (category instanceof Category.SubCategory subCategory) {
            entries.add(new ParentCategoryButtonEntry(this, subCategory));
        }

        for (Pack pack : category.getPacks()) {
            // Experimental packs aren't shown in the web page, at least for now
            if (pack.isExperimental()) {
                continue;
            }

            PackEntry entry = new PackEntry(this, pack);
            entries.add(entry);
            if (this.selectionHelper.isSelected(pack)) {
                entry.selectionData.toggleSelection();
            }
        }

        if (category.getSubCategories() != null) {
            for (Category.SubCategory subCategory : category.getSubCategories()) {
                entries.add(new SubCategoryButtonEntry(this, subCategory));
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
        return this.getX() + this.width / 2;
    }

    private int getCenterY() {
        return this.getY() + this.height / 2;
    }

    private static int getLineHeight(TextRenderer textRenderer) {
        return textRenderer.fontHeight + TEXT_MARGIN;
    }

    public void updateScreenWidth() {
        this.setWidth(this.screen.getLeftWidth());
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
    protected int method_65507() {
        return this.getX() + this.getRowWidth() + ROW_LEFT_RIGHT_MARGIN + SCROLLBAR_LEFT_MARGIN;
    }

    private int getTooltipWidth() {
        return (this.width / 5) * 2;
    }

    // private void moveFocus(MoveDirection direction) {
    //     int offset = direction == MoveDirection.UP ? -1 : 1;
    //     if (!this.children().isEmpty()) {
    //         int start = this.children().get(0) instanceof WarningEntry ? 1 : 0;
    //         AbstractEntry current = this.getFocused();
    //         int currentIndex = current != null ? this.children().indexOf(current) : -1;
    //
    //         int index = MathHelper.clamp(currentIndex + offset, start, this.getEntryCount() - 1);
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

    // @Override
    // public void setFocused(@Nullable Element focused) {
    //     super.setFocused(focused);
    //
    //     // Set focused element as list when focusing an entry
    //     if (focused != null && !this.isFocused()) {
    //         this.screen.setFocused(this);
    //     }
    // }

    void visitLines(C_gitqeeaa textCollector) {
        if (!this.children().isEmpty()) {
            return;
        }

        // visit error lines
        TextRenderer textRenderer = this.client.textRenderer;

        int x = this.getCenterX();
        int y = this.getCenterY();
        int lineHeight = getLineHeight(textRenderer);

        int textY = y - lineHeight * 2;
        C_emchntzr alignment = C_emchntzr.CENTER;
        this.errorText.method_75816(alignment, x, textY, lineHeight, textCollector);
    }

    // region input callbacks
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (event.method_74245() == GLFW.GLFW_MOUSE_BUTTON_1 && this.children().isEmpty()) {
            // Handle clicks when the error is shown
            double mouseX = event.x();
            double mouseY = event.y();
            if (mouseX >= this.getX() && mouseX < this.getXEnd()
                    && mouseY >= this.getY() && mouseY < this.getYEnd()) {
                TextRenderer textRenderer = this.client.textRenderer;

                C_gitqeeaa.C_abiemazc styleFinder = new C_gitqeeaa.C_abiemazc(textRenderer, (int) mouseX, (int) mouseY);
                this.visitLines(styleFinder);
                Style style = styleFinder.method_75777();

                if (style != null && style.getClickEvent() != null
                        && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                    URI uri = ((ClickEvent.OpenUrl) style.getClickEvent()).uri();
                    Util.openUri(this.client, this.screen, uri);

                    return true;
                }
            }
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.isFocused()) {
            // if (keyCode == GLFW.GLFW_KEY_DOWN) {
            //     this.moveFocus(MoveDirection.DOWN);
            //     return true;
            // } else if (keyCode == GLFW.GLFW_KEY_UP) {
            //     this.moveFocus(MoveDirection.UP);
            //     return true;
            // }

            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                AbstractEntry focusedEntry = this.getFocused();
                if (focusedEntry instanceof PackEntry entry) {
                    this.toggleSelection(entry);
                }
            }
        }

        return false;
    }

    // @Override
    // public boolean changeFocus(boolean lookForwards) {
    //     return !this.isFocused();
    // }

    // endregion

    // region render
    @Override
    public void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.drawWidget(graphics, mouseX, mouseY, delta);

        if (this.children().isEmpty()) {
            this.renderError(graphics);
        }
    }

    @Override
    protected void renderEntry(GuiGraphics graphics, int mouseX, int mouseY, float delta, AbstractEntry entry) {
        boolean focused = this.isFocused() && this.getFocused() == entry;
        boolean isSelected = entry instanceof PackEntry packEntry && packEntry.selectionData.isSelected();
        if (isSelected) {
            int outlineColor = focused ? 0xFFFFFFFF : SELECTION_OUTLINE_COLOR;
            int color = this.getEntrySelectionColor(entry);
            this.drawEntrySelectionHighlight(graphics, entry, outlineColor, color);
        /*} else if (focused) { // TODO: reimplement keyboard nav
            int x = entry.getX();
            int y = entry.getY();
            int width = entry.getWidth();
            int height = entry.getHeight();
            RenderUtil.drawOutline(graphics, x - 1, y - 1, width - 2, height - 2, 1, 0xFFFFFFFF);*/
        }

        entry.method_25343(graphics, mouseX, mouseY, Objects.equals(this.getHoveredEntry(), entry), delta);
    }

    protected void drawEntrySelectionHighlight(GuiGraphics graphics, AbstractEntry entry, int borderColor, int fillColor) {
        int x1 = entry.getX();
        int y1 = entry.getY();
        int x2 = x1 + entry.getWidth();
        int y2 = y1 + entry.getHeight();
        graphics.fill(x1, y1, x2, y2, borderColor);
        graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillColor);
    }

    private void renderError(GuiGraphics graphics) {
        this.visitLines(graphics.method_75788());
    }

    public void renderDebugInfo(GuiGraphics graphics, int mouseX, int mouseY) {
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

        RenderUtil.renderDebugInfo(graphics, textRenderer, this.getX(), this.getYEnd(), debugInfo);
    }

    public void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (mouseY >= this.getY() && mouseY < this.getYEnd()
                && mouseX >= this.getX() && mouseX < this.getXEnd()
                && !this.screen.isCoveredByPopup(mouseX, mouseY)) {
            int width = this.getTooltipWidth();
            for (AbstractEntry entry : this.children()) {
                if (entry.renderTooltip(graphics, mouseX, mouseY, width)) {
                    break;
                }
            }
        }
    }
    // endregion

    @Override
    public void updateNarration(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Constants.RESOURCE_PACK_SCREEN_SUBTITLE);
    }

    public static class PackEntry extends AbstractEntry {
        private final Pack pack;
        private final Text name;

        private final PackSelectionListWidget widget;
        private final Identifier icon;
        private boolean downloadedIcon = false;
        private boolean iconExists;

        private NativeImage downloadedIconImage;

        private List<Text> description;
        private MultilineText shortDescription;
        private int lastDescriptionWidth;

        protected PackSelectionData selectionData;

        public PackEntry(PackSelectionListWidget widget, Pack pack) {
            super(widget);
            this.pack = pack;
            this.name = Text.of(pack.getName()).copy().formatted(Formatting.BOLD);
            this.widget = widget;

            this.icon = VTDMod.getIconId(pack);

            this.iconExists = ((TextureManagerAccess) this.client.getTextureManager()).vtdownloader$hasTexture(this.icon);

            this.selectionData = new PackSelectionData(this.pack, widget.category);
        }

        protected List<Text> getDescriptionLines(int maxWidth) {
            return this.wrapEscapedText(this.pack.getDescription(), maxWidth);
        }

        private List<Text> getDescription(int maxWidth) {
            if (this.description != null) {
                return this.description;
            }

            this.description = this.getDescriptionLines(maxWidth);

            return this.description;
        }

        private MultilineText getShortDescription(int maxWidth, TextRenderer textRenderer) {
            if (maxWidth == this.lastDescriptionWidth && this.shortDescription != null) {
                return this.shortDescription;
            }

            List<Text> fullDescriptionLines =  this.getDescriptionLines(maxWidth);
            List<Text> lines = new ArrayList<>();
            if (!fullDescriptionLines.isEmpty()) {
                lines.add(fullDescriptionLines.getFirst());

                // truncate the second line at a punctuation
                if (fullDescriptionLines.size() > 1) {
                    Text secondLine = fullDescriptionLines.get(1);
                    int lineWidth = textRenderer.getWidth(secondLine);

                    // If there are more than 2 lines, or if the second line doesn't fit, truncate at last punctuation
                    if (fullDescriptionLines.size() > 2 || lineWidth > maxWidth) {
                        secondLine = truncateAtLastPunctuation(secondLine, maxWidth, textRenderer);
                    }

                    lines.add(secondLine);
                }
            }

            this.shortDescription = this.createMultilineText(lines);
            this.lastDescriptionWidth = maxWidth;

            return this.shortDescription;
        }

        public Pack getPack() {
            return this.pack;
        }

        private void downloadIcon() {
            if (this.downloadedIconImage != null) {
                TextureManager textureManager = this.client.getTextureManager();
                NativeImageBackedTexture iconTexture = new NativeImageBackedTexture(this.pack::getId, this.downloadedIconImage);
                textureManager.method_4616(this.icon, iconTexture);
                this.iconExists = true;
                this.downloadedIconImage = null;
            }

            if (this.downloadedIcon || this.iconExists) return;

            this.downloadedIcon = true;

            VTDMod.downloadIcon(this.pack).whenCompleteAsync((icon, throwable) -> {
                if (throwable != null) {
                    VTDMod.LOGGER.error("Failed to download icon for pack {}", this.pack.getName(), throwable);
                    return;
                }

                if (icon != null) {
                    this.downloadedIconImage = icon;
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
        public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
            if (event.method_74245() == GLFW.GLFW_MOUSE_BUTTON_1) {
                this.widget.toggleSelection(this);
                return true;
            }

            return false;
        }

        // region entryRender
        @Override
        public void method_25343(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getX();
            int y = this.getY();
            int height = this.getHeight();
            int width = this.getWidth();
            TextRenderer textRenderer = this.client.textRenderer;
            // Keep icon size fixed based on ITEM_HEIGHT, not entryHeight (reduced to 75%)
            int iconSize = ((ITEM_HEIGHT - ICON_MARGIN * 2) * 3)/4;

            // Calculate equal spacing: top, left, bottom between icon and border
            // Border is at x, so we want equal spacing on all sides
            int spacing = (height - iconSize) / 2; // Equal top and bottom spacing
            int iconX = x + spacing; // Left spacing equals top/bottom spacing
            int iconY = y + spacing; // Top spacing

            // Text area starts after icon with spacing on both sides
            int textAreaX = x + iconSize + spacing * 2;
            int textAreaWidth = width - (iconSize + spacing * 2);

            // Calculate vertical centering (title + max 2 description lines)
            int lineHeight = getLineHeight(textRenderer);
            int totalTextHeight = lineHeight * 2; // title + description (max 2 lines)
            int textStartY = y + (height - totalTextHeight) / 2;
            int centerX = textAreaX + textAreaWidth / 2; // center in text area

            graphics.drawCenteredShadowedText(textRenderer, this.name, centerX, textStartY, 0xFFFFFFFF);

            // Use textAreaWidth for description to ensure proper wrapping when menu is open
            this.renderDescription(graphics, centerX, textStartY + lineHeight, textAreaWidth);
            if (!DISABLE_ICONS) this.renderIcon(graphics, iconX, iconY, iconSize);
        }

        private void renderDescription(GuiGraphics graphics, int x, int y, int width) {
            TextRenderer textRenderer = this.client.textRenderer;
            MultilineText description = this.getShortDescription(width - TEXT_MARGIN, textRenderer);
            C_emchntzr alignment = C_emchntzr.CENTER;
            description.method_75816(alignment, x, y, textRenderer.fontHeight, graphics.method_75788());
        }

        private static Text truncateAtLastPunctuation(Text originalText, int maxWidth, TextRenderer textRenderer) {
            String text = originalText.getString();
            int lastPunctIndex = -1;

            // Find the last "." or "," that fits within maxWidth
            for (int i = text.length() - 1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '.' || c == ',') {
                    String candidate = text.substring(0, i + 1);
                    Text candidateText = Text.of(candidate).copy().setStyle(originalText.getStyle());
                    if (textRenderer.getWidth(candidateText) <= maxWidth) {
                        lastPunctIndex = i + 1;
                        break;
                    }
                }
            }

            // If we found a punctuation mark, truncate there
            if (lastPunctIndex > 0) {
                String truncated = text.substring(0, lastPunctIndex);
                return Text.of(truncated).copy().setStyle(originalText.getStyle());
            }

            // If no punctuation found, truncate to fit maxWidth
            for (int i = text.length(); i > 0; i--) {
                String candidate = text.substring(0, i);
                Text candidateText = Text.of(candidate).copy().setStyle(originalText.getStyle());
                if (textRenderer.getWidth(candidateText) <= maxWidth) {
                    return candidateText;
                }
            }

            // Fallback: return empty or first character
            return Text.empty();
        }

        private void renderIcon(GuiGraphics graphics, int x, int y, int size) {
            downloadIcon();
            if (!this.iconExists) return;

            graphics.method_25290(RenderPipelines.GUI_TEXTURED, this.icon, x, y, 0.0F, 0.0F, size, size, size, size);
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

        public WarningEntry(PackSelectionListWidget widget, Category.Warning warning) {
            super(widget);
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
        public void method_25343(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getX();
            int y = this.getY();
            int width = this.getWidth();
            int height = this.getHeight();

            this.renderBackground(graphics, x + WARNING_BG_MARGIN, y + WARNING_BG_MARGIN, width - WARNING_BG_MARGIN * 2, height - WARNING_BG_MARGIN * 2);

            int textWidth = width - WARNING_MARGIN * 2;
            int textHeight = height - WARNING_BG_MARGIN * 2;
            this.renderText(graphics, x + WARNING_MARGIN + textWidth / 2, y + WARNING_MARGIN, textWidth, textHeight);
        }

        private void renderBackground(GuiGraphics graphics, int x, int y, int width, int height) {
            graphics.fill(x, y, x + width, y + height, this.color);
        }

        private void renderText(GuiGraphics graphics, int x, int y, int width, int height) {
            MultilineText text = this.getText(width);
            C_emchntzr alignment = C_emchntzr.CENTER;
            int lineHeight = this.client.textRenderer.fontHeight;
            int textY = y + height / 2 - text.count() * lineHeight / 2;
            text.method_75816(alignment, x, textY, lineHeight, graphics.method_75788());
        }
        // endregion

        @Override
        public String toString() {
            return "Warning";
        }
    }

    public static class SubCategoryButtonEntry extends CategoryButtonEntry {
        protected SubCategoryButtonEntry(PackSelectionListWidget widget, Category.SubCategory category) {
            super(widget, category);
        }

        @Override
        public String toString() {
            return "Sub category " + this.category.getName();
        }
    }

    public static class ParentCategoryButtonEntry extends CategoryButtonEntry {
        protected ParentCategoryButtonEntry(PackSelectionListWidget widget, Category.SubCategory subCategory) {
            super(widget, subCategory.getParent());
        }

        @Override
        public String toString() {
            return "Parent category " + this.category.getName();
        }
    }

    public abstract static class CategoryButtonEntry extends AbstractEntry {
        protected static final int BUTTON_HEIGHT = 20;
        protected static final int BUTTON_HORIZONTAL_PADDING = 32;
        protected static final Identifier TEXTURE = Identifier.ofDefault("widget/button");

        protected final Category category;
        protected final Text name;

        protected CategoryButtonEntry(PackSelectionListWidget widget, Category category) {
            super(widget);
            this.category = category;
            this.name = Text.literal(category.getName()).formatted(Formatting.BOLD);
        }

        @Override
        protected List<Text> getTooltipText(int width) {
            return Collections.emptyList();
        }

        private void playDownSound(SoundManager soundManager) {
            soundManager.play(PositionedSoundInstance.create(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
            if (event.method_74245() == GLFW.GLFW_MOUSE_BUTTON_1) {
                this.screen.selectCategory(this.category);
                this.playDownSound(this.client.getSoundManager());
                return true;
            }

            return false;
        }

        @Override
        public void method_25343(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getX();
            int y = this.getY();
            int width = this.getWidth();
            int height = this.getHeight();

            // drawSprite
            graphics.method_52706(RenderPipelines.GUI_TEXTURED, TEXTURE,
                    x + BUTTON_HORIZONTAL_PADDING, y + (height - BUTTON_HEIGHT) / 2, width - BUTTON_HORIZONTAL_PADDING * 2, BUTTON_HEIGHT);
            graphics.drawCenteredShadowedText(this.client.textRenderer, this.name, x + width / 2, y + (height - this.client.textRenderer.fontHeight) / 2, 0xFFFFFFFF);
        }
    }

    public static abstract class AbstractEntry extends EntryListWidget.Entry<AbstractEntry> {
        protected final PackSelectionListWidget parentWidget;
        protected final MinecraftClient client;
        protected final VTDownloadScreen screen;

        protected AbstractEntry(PackSelectionListWidget widget) {
            this.parentWidget = widget;
            this.client = widget.client;
            this.screen = widget.screen;
        }

        protected final List<Text> wrapEscapedText(String text, int maxWidth) {
            return Util.wrapText(this.client.textRenderer, Util.removeHtmlTags(text), maxWidth);
        }

        protected final MultilineText createMultilineText(List<Text> lines) {
            return Util.createMultilineText(this.client.textRenderer, lines, 4);
        }

        protected abstract List<Text> getTooltipText(int width);

        // region baseEntryRender
        protected boolean renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, int width) {
            if (this.isMouseOver(mouseX, mouseY)) {
                graphics.deferDrawingTooltip(this.client.textRenderer, this.getTooltipText(width), mouseX, mouseY);
                return true;
            }

            return false;
        }
        // endregion
    }
}
