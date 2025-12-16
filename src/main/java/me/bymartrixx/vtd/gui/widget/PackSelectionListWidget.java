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
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.list.EntryListWidget;
import net.minecraft.client.render.RenderPipelines;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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
    private static final boolean DISABLE_ICONS = false;

    private static final Text ERROR_URL = Util.urlText(VTDMod.BASE_URL);
    private static final Text ERROR_HEADER = Text.translatable("vtd.packError.title")
            .formatted(Formatting.BOLD, Formatting.ITALIC);
    private static final Text ERROR_BODY = Text.translatable("vtd.packError.body", ERROR_URL);
    private static final Text ERROR_TEXT = Text.empty().append(ERROR_HEADER).append("\n").append(ERROR_BODY);

    public static final int ITEM_HEIGHT = 48; // Made it Bigger
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
    private PackEntry lastClickedEntry = null;
    private PackEntry lastUnselectedEntry = null;

    public PackSelectionListWidget(MinecraftClient client, VTDownloadScreen screen, int width, int height, int y,
                                   Category category, PackSelectionHelper selectionHelper) {
        super(client, width, height, y, ITEM_HEIGHT);
        this.screen = screen;
        this.category = category;
        this.selectionHelper = selectionHelper;

        this.errorLines = Util.getMultilineTextLines(client.textRenderer, ERROR_TEXT, 8, (int) (y / 1.5));
        this.errorText = Util.createMultilineText(client.textRenderer, ERROR_TEXT, 8, (int) (y / 1.5));

        // In 1.21.10+, children() returns an unmodifiable collection
        this.replaceEntries(getPackEntries(category));
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
        if (category == null) {
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
            boolean wasSelected = entry.selectionData.isSelected();
            this.selectionHelper.toggleSelection(entry);
            boolean isSelected = entry.selectionData.isSelected();
            
            if (wasSelected && !isSelected) {
                this.lastUnselectedEntry = entry;
                this.lastClickedEntry = null; 
            } else if (!wasSelected && isSelected) {
                this.lastClickedEntry = entry;
                this.lastUnselectedEntry = null; 
            } else {
                this.lastClickedEntry = entry;
            }
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
        return this.height / 2;
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
        return this.getX() + getRowWidth() + ROW_LEFT_RIGHT_MARGIN + SCROLLBAR_LEFT_MARGIN;
    }

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

    // region input callbacks
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = this.client.mouse.getX() * this.client.getWindow().getScaledWidth() / this.client.getWindow().getWidth();
        double mouseY = this.client.mouse.getY() * this.client.getWindow().getScaledHeight() / this.client.getWindow().getHeight();
        if (this.children().isEmpty()) {
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
                    URI uri = ((ClickEvent.OpenUrl) style.getClickEvent()).uri();
                    if (this.client.options.getChatLinksPrompt().get()) {
                        this.client.setScreen(new ConfirmLinkScreen(confirmed -> {
                            if (confirmed) {
                                net.minecraft.util.Util.getOperatingSystem().open(uri);
                            }

                            client.setScreen(this.screen);
                        }, uri.toString(), false));
                    } else {
                        net.minecraft.util.Util.getOperatingSystem().open(uri);
                    }

                    return true;
                }
            }
        }

        return super.mouseClicked(event, bl);
    }

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

    protected void renderEntry(GuiGraphics graphics, int mouseX, int mouseY, float delta, int index, int entryX, int entryY, int width, int height) {
        AbstractEntry entry = this.children().get(index);

        // Note: Selection highlighting is now handled in method_25343, so we don't draw it here to avoid conflicts with custom colors. Only draw focus outline for non-selected entries.
        boolean focused = this.isFocused() && this.getFocused() == entry;
        if (!this.isSelectedEntry(index) && focused) {
            RenderUtil.drawOutline(graphics, entryX - 1, entryY - 1, width - 2, height - 2, 1, 0xFFFFFFFF);
        }

        entry.renderEntry(graphics, index, entryY, entryX, width, height, mouseX, mouseY,
                Objects.equals(this.getHoveredEntry(), entry), delta);
    }

    private void renderError(GuiGraphics graphics) {
        TextRenderer textRenderer = this.client.textRenderer;

        int x = this.getCenterX();
        int y = this.getCenterY();
        int lineHeight = getLineHeight(textRenderer);

        int textY = y - lineHeight * 2;
        for (OrderedText line : this.errorLines) {
            int lineWidth = this.client.textRenderer.getWidth(line);
            graphics.drawShadowedText(this.client.textRenderer, line, x - lineWidth / 2, textY, 0xFFFFFFFF);
            textY += lineHeight;
        }
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
            this.widget.toggleSelection(this);
            return true;
        }

        // region entryRender
        @Override
        public void renderEntry(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = this.client.textRenderer;
            // Keep icon size fixed based on ITEM_HEIGHT, not entryHeight (reduced to 75%)
            int iconSize = (int) ((ITEM_HEIGHT - ICON_MARGIN * 2) * 0.75);
            
            // Calculate equal spacing: top, left, bottom between icon and border
            // Border is at x, so we want equal spacing on all sides
            int spacing = (entryHeight - iconSize) / 2; // Equal top and bottom spacing
            int iconX = x + spacing; // Left spacing equals top/bottom spacing
            int iconY = y + spacing; // Top spacing
            
            // Text area starts after icon with spacing
            int textAreaX = iconX + iconSize + spacing;
            int textAreaWidth = entryWidth - (textAreaX - x);
            
            // Calculate vertical centering (title + max 2 description lines)
            int lineHeight = getLineHeight(textRenderer);
            int totalTextHeight = lineHeight * 2; // title + description (max 2 lines)
            int textStartY = y + (entryHeight - totalTextHeight) / 2;
            int centerX = textAreaX + textAreaWidth / 2; // center in text area
            
            graphics.drawCenteredShadowedText(textRenderer, this.name, centerX, textStartY, 0xFFFFFFFF);

            // Use textAreaWidth for description to ensure proper wrapping when menu is open
            this.renderDescription(graphics, centerX, textStartY + lineHeight, textAreaWidth);
            
            // Render icon with equal spacing
            if (!DISABLE_ICONS) {
                this.renderIcon(graphics, iconX, iconY, iconSize);
            }
        }

        private void renderDescription(GuiGraphics graphics, int x, int y, int width) {
            List<Text> descLines = this.getDescriptionLines(width - TEXT_MARGIN);
            TextRenderer textRenderer = this.client.textRenderer;
            int maxWidth = width - TEXT_MARGIN;
            
            // Limit to maximum 2 lines, truncating at last "." or "," if needed
            int descY = y;
            if (descLines.size() == 0) {
                return;
            }
            
            // first line
            Text firstLine = descLines.get(0);
            int firstLineWidth = textRenderer.getWidth(firstLine);
            graphics.drawShadowedText(textRenderer, firstLine, x - firstLineWidth / 2, descY, 0xFFFFFFFF);
            descY += textRenderer.fontHeight;
            
            // second line with truncation
            if (descLines.size() >= 2) {
                Text secondLine = descLines.get(1);
                int secondLineWidth = textRenderer.getWidth(secondLine);
                
                // If there are more than 2 lines, or if the second line doesn't fit, truncate at last punctuation
                if (descLines.size() > 2 || secondLineWidth > maxWidth) {
                    Text truncatedLine = truncateAtLastPunctuation(secondLine, maxWidth, textRenderer);
                    int truncatedWidth = textRenderer.getWidth(truncatedLine);
                    graphics.drawShadowedText(textRenderer, truncatedLine, x - truncatedWidth / 2, descY, 0xFFFFFFFF);
                } else {
                    graphics.drawShadowedText(textRenderer, secondLine, x - secondLineWidth / 2, descY, 0xFFFFFFFF);
                }
            }
        }
        
        private Text truncateAtLastPunctuation(Text originalText, int maxWidth, TextRenderer textRenderer) {
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

            graphics.drawTexture(RenderPipelines.GUI_TEXTURED, this.icon, x, y, 0.0F, 0.0F, size, size, size, size);
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
        public void renderEntry(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.renderBackground(graphics, x + WARNING_BG_MARGIN, y + WARNING_BG_MARGIN, entryWidth - WARNING_BG_MARGIN * 2, entryHeight - WARNING_BG_MARGIN * 2);

            int width = entryWidth - WARNING_MARGIN * 2;
            this.renderText(graphics, x + WARNING_MARGIN + width / 2, y + WARNING_MARGIN, width);
        }

        private void renderBackground(GuiGraphics graphics, int x, int y, int width, int height) {
            graphics.fill(x, y, x + width, y + height, this.color);
        }

        private void renderText(GuiGraphics graphics, int x, int y, int width) {
            List<Text> lines = this.getTextLines(width);
            int textY = y;
            for (Text line : lines) {
                int lineWidth = this.client.textRenderer.getWidth(line);
                graphics.drawShadowedText(this.client.textRenderer, line, x - lineWidth / 2, textY, 0xFFFFFFFF);
                textY += this.client.textRenderer.fontHeight;
            }
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
            this.screen.selectCategory(this.category);
            this.playDownSound(this.client.getSoundManager());
            return true;
        }

        @Override
        public void renderEntry(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            graphics.drawSprite(RenderPipelines.GUI_TEXTURED, TEXTURE,
                    x + BUTTON_HORIZONTAL_PADDING, y + (entryHeight - BUTTON_HEIGHT) / 2, entryWidth - BUTTON_HORIZONTAL_PADDING * 2, BUTTON_HEIGHT);
            graphics.drawCenteredShadowedText(this.client.textRenderer, this.name, x + entryWidth / 2, y + (entryHeight - this.client.textRenderer.fontHeight) / 2, 0xFFFFFFFF);
        }
    }

    public static abstract class AbstractEntry extends EntryListWidget.Entry<AbstractEntry> {
        protected final MinecraftClient client;
        protected final VTDownloadScreen screen;
        protected final PackSelectionListWidget parentWidget;

        protected AbstractEntry(PackSelectionListWidget widget) {
            this.client = widget.client;
            this.screen = widget.screen;
            this.parentWidget = widget;
        }

        protected final List<Text> wrapEscapedText(String text, int maxWidth) {
            return Util.wrapText(this.client.textRenderer, Util.removeHtmlTags(text), maxWidth);
        }

        protected final MultilineText createMultilineText(List<Text> lines) {
            return Util.createMultilineText(this.client.textRenderer, lines, 2);
        }

        protected abstract List<Text> getTooltipText(int width);

        public abstract void renderEntry(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);

        // Required method for Minecraft 1.21.10+ API method_25343 receives mouse coordinates, not entry position
        @Override
        public void method_25343(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // Calculate entry position from widget
            int entryIndex = this.parentWidget.children().indexOf(this);
            if (entryIndex < 0) return;
            
            int x = this.parentWidget.getRowLeft();
            int y = this.parentWidget.getRowTop(entryIndex);
            int entryWidth = this.parentWidget.getRowWidth();
            int entryHeight = ITEM_HEIGHT;
            
            // Draw background and outline for selected entries
            // Draw background based on selection state for PackEntry
            if (this instanceof PackEntry) {
                int backgroundWidth = entryWidth;
                int color;

                if (this.parentWidget.isSelectedEntry(entryIndex)) {
                    // Selected: Green background
                    color = 0xFF006400; // Dark Green
                } else {
                    // Unselected: Dark Grey background
                    color = 0xFF222222; // Dark Grey
                }

                // Draw the background
                graphics.fill(x, y, x + backgroundWidth, y + entryHeight, color);

                // Draw outline if selected or focused
                if (this.parentWidget.isSelectedEntry(entryIndex)) {
                    boolean isLastClicked = this.parentWidget.lastClickedEntry == this;
                    int outlineColor = isLastClicked ? 0xFFFFFFFF : 0xFF808080;
                    RenderUtil.drawEntrySelectionHighlight(graphics, x, y, backgroundWidth, entryHeight, outlineColor,
                            color);
                }
            } else if (this.parentWidget.isSelectedEntry(entryIndex)) {
                int backgroundWidth = entryWidth;
                boolean isLastClicked = this instanceof PackEntry && this.parentWidget.lastClickedEntry == this;
                
                if (isLastClicked) {
                    // White outline for last clicked entry
                    int outlineColor = 0xFFFFFFFF; // White outline
                    int color = 0xE0000000; // Black fill
                    RenderUtil.drawEntrySelectionHighlight(graphics, x, y, backgroundWidth, entryHeight, outlineColor, color);
                } else {
                    // Grey outline for selected entries (not last clicked)
                    int outlineColor = 0xFF808080; // Grey outline
                    int color = 0xE0000000; // Black fill
                    RenderUtil.drawEntrySelectionHighlight(graphics, x, y, backgroundWidth, entryHeight, outlineColor, color);
                }
            }
            this.renderEntry(graphics, entryIndex, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }

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
