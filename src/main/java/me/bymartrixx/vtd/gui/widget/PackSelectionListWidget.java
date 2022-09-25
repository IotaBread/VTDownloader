package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.ColorUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PackSelectionListWidget extends EntryListWidget<PackSelectionListWidget.AbstractEntry> {
    private static final boolean SHOW_DEBUG_INFO = true;
    private static final boolean DISABLE_ICONS = true;

    private static final Text ERROR_URL = Text.of(VTDMod.BASE_URL).copy()
            .formatted(Formatting.UNDERLINE, Formatting.ITALIC, Formatting.BLUE)
            .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VTDMod.BASE_URL)));
    private static final Text ERROR_HEADER_TEXT = Text.translatable("vtd.packError.title.1").formatted(Formatting.BOLD, Formatting.ITALIC);
    private static final Text ERROR_HEADER_TEXT_2 = Text.translatable("vtd.packError.title.2").formatted(Formatting.BOLD, Formatting.ITALIC);
    private static final Text ERROR_TEXT = Text.translatable("vtd.packError.body.1");
    private static final Text ERROR_TEXT_2 = Text.translatable("vtd.packError.body.2");
    private static final Text ERROR_TEXT_3 = Text.translatable("vtd.packError.body.3", ERROR_URL);
    private static final List<Text> ERROR_LINES = List.of(ERROR_HEADER_TEXT, ERROR_HEADER_TEXT_2,
            ERROR_TEXT, ERROR_TEXT_2, ERROR_TEXT_3);

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

    private final MultilineText errorText;

    private final PackSelectionHelper selectionHelper = new PackSelectionHelper();

    public PackSelectionListWidget(MinecraftClient client, VTDownloadScreen screen, int width, int height, int top, int bottom,
                                   Map<Category, List<Pack>> selectedPacks, Category category) {
        super(client, width, height, top, bottom, ITEM_HEIGHT);
        this.screen = screen;
        this.category = category;

        this.errorText = MultilineText.create(client.textRenderer, ERROR_LINES);

        this.children().addAll(getPackEntries(category));
    }

    public void setCategory(Category category) {
        this.category = category;

        this.replaceEntries(getPackEntries(category));
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
            entries.add(new PackEntry(this, pack));
        }

        this.entryCache.put(category, entries);

        return entries;
    }

    private void toggleSelection(PackEntry entry) {
        this.selectionHelper.toggleSelection(entry);
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

    @Override
    public int getRowWidth() {
        return this.width - ROW_LEFT_RIGHT_MARGIN * 2;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.left + getRowWidth() + SCROLLBAR_LEFT_MARGIN;
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
        return this.width / 2;
    }

    @Nullable
    private Style getErrorStyleAt(double mouseX, int line) {
        TextRenderer textRenderer = this.client.textRenderer;
        Text text = ERROR_LINES.get(line);
        int width = textRenderer.getWidth(text);
        int startX = this.getCenterX() - width / 2;
        int endX = startX + width;

        return mouseX >= startX && mouseX < endX ?
                textRenderer.getTextHandler().getStyleAt(text, (int) mouseX - startX) : null;
    }

    // region input callbacks
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.children().isEmpty()) {
            // Handle clicks when the error is shown
            int x = this.getCenterX();
            int textWidth = this.errorText.m_crhihbev();
            int startX = x - textWidth / 2;
            int endX = x + textWidth / 2;

            int y = this.getCenterY();
            int lineHeight = getLineHeight(this.client.textRenderer);
            int startY = y - lineHeight * 2;
            int endY = y + lineHeight * 3;

            if (mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY) {
                int line = (int) ((mouseY - startY) / lineHeight);
                Style style = this.getErrorStyleAt(mouseX, line);

                if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                    this.screen.handleTextClick(style);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
    // endregion

    // region render
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if (this.children().isEmpty()) {
            this.renderError(matrices);
        }

        if (SHOW_DEBUG_INFO) {
            this.renderDebugInfo(matrices, mouseX, mouseY);
        }
    }

    @Override // renderEntry
    protected void m_enzpxkzi(MatrixStack matrices, int mouseX, int mouseY, float delta, int index, int entryX, int entryY, int width, int height) {
        AbstractEntry entry = this.getEntry(index);

        if (this.isSelectedEntry(index)) {
            int outlineColor = this.isFocused() ? -1 : SELECTION_OUTLINE_COLOR;
            int color = this.getEntrySelectionColor(entry);
            this.m_ugejzjin(matrices, entryY, width, height, outlineColor, color);
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

    private void renderDebugInfo(MatrixStack matrices, int mouseX, int mouseY) {
        TextRenderer textRenderer = this.client.textRenderer;

        boolean hasCategory = this.category != null;
        List<String> debugInfo = List.of(
                "WxH = " + this.width + "x" + this.height,
                "EW = " + this.getRowWidth(),
                "C = " + (hasCategory ? this.category.getName() : "null"),
                "HI = " + (hasCategory ? this.category.isHardIncompatible() : "N/A"),
                "S = " + this.selectionHelper.getSelection(),
                "IC = " + this.selectionHelper.usedColors,
                "MX/MY = " + mouseX + "/" + mouseY
        );

        // Make text half its size
        matrices.push();
        matrices.scale(0.5f, 0.5f, 0.5f);

        float lineHeight = textRenderer.fontHeight + TEXT_MARGIN;
        float startY = this.height * 2 - lineHeight * debugInfo.size();
        for (int i = 0; i < debugInfo.size(); i++) {
            String text = debugInfo.get(i);
            textRenderer.draw(matrices, text, this.left, startY + i * lineHeight, 0xFFCCCCCC);
        }

        matrices.pop();
    }

    public void renderTooltips(MatrixStack matrices, int mouseX, int mouseY) {
        int width = this.getTooltipWidth();
        for (AbstractEntry entry : this.children()) {
            if (entry.renderTooltip(matrices, mouseX, mouseY, width)) {
                break;
            }
        }
    }
    // endregion

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // TODO
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
    }

    public static class WarningEntry extends AbstractEntry {
        /**
         * Parse an 0xAARRGGBB color from `rgba(red, green, blue, alpha)`
         */
        private static int parseColor(String color) {
            String format = color.substring(0, color.indexOf("("));
            if (color.endsWith(")")) {
                List<String> components = Arrays.stream(color.substring(color.indexOf("(") + 1, color.length() - 1)
                        .split(",")).map(String::trim).toList();

                if (format.equals("rgba")) {
                    if (components.size() == 4) {
                        int red = Integer.parseInt(components.get(0));
                        int green = Integer.parseInt(components.get(1));
                        int blue = Integer.parseInt(components.get(2));
                        float alpha = Float.parseFloat(components.get(3));

                        return ColorUtil.ARGB32.getArgb((int) (alpha * 255), red, green, blue);
                    }
                }
            }

            VTDMod.LOGGER.warn("Unknown color format: " + color);
            return 0x00000000;
        }

        private final Category.Warning warning;
        private final int color;

        private List<Text> textLines;
        private MultilineText text;

        public WarningEntry(MinecraftClient client, VTDownloadScreen screen, Category.Warning warning) {
            super(client, screen);
            this.warning = warning;

            this.color = parseColor(warning.getColor());
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
    }

    public static abstract class AbstractEntry extends EntryListWidget.Entry<AbstractEntry> {
        protected final MinecraftClient client;
        protected final VTDownloadScreen screen;

        protected AbstractEntry(MinecraftClient client, VTDownloadScreen screen) {
            this.client = client;
            this.screen = screen;
        }

        protected final List<Text> wrapEscapedText(String text, int maxWidth) {
            return wrapText(escapeText(text), maxWidth);
        }

        private String escapeText(String text) {
            // Remove html tags
            return StringUtils.normalizeSpace(text.replaceAll("(?!<br>)<[^>]*>", " "))
                    .replaceAll("<br>", "\n"); // Replace <br> after normalizing to keep new lines
        }

        private List<Text> wrapText(String text, int maxWidth) {
            TextHandler textHandler = this.client.textRenderer.getTextHandler();
            List<StringVisitable> visitableLines = textHandler.wrapLines(text, maxWidth, Style.EMPTY);
            return visitableLines.stream().map(StringVisitable::getString).map(Text::of).toList();
        }

        protected final MultilineText createMultilineText(List<Text> lines) {
            if (lines.size() > 2) {
                lines = lines.subList(0, 2);
            }

            return MultilineText.create(this.client.textRenderer, lines);
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
