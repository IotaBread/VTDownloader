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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Dynamic rendering
public class PackSelectionListWidget extends EntryListWidget<PackSelectionListWidget.PackEntry> {
    private static final boolean SHOW_DEBUG_INFO = true;
    private static final boolean DISABLE_ICONS = true;
    private static final List<String> HARD_INCOMPATIBLE_CATEGORIES = List.of("Menu Panoramas", "Options Backgrounds", "Colorful Slime");

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

    private static final int ROW_LEFT_RIGHT_MARGIN = 10;
    private static final int SCROLLBAR_LEFT_MARGIN = 4;
    private static final int TEXT_MARGIN = 2;
    private static final int ICON_MARGIN = 1;

    private final Map<Category, List<PackEntry>> entryCache = new HashMap<>();
    private final VTDownloadScreen screen;
    private final Map<Category, List<Pack>> selectedPacks;
    private Category category;

    private final MultilineText errorText;

    public PackSelectionListWidget(MinecraftClient client, VTDownloadScreen screen, int width, int height, int top, int bottom, int itemHeight,
                                   Map<Category, List<Pack>> selectedPacks, Category category) {
        super(client, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.selectedPacks = selectedPacks;
        this.category = category;

        this.errorText = MultilineText.create(client.textRenderer, ERROR_LINES);

        this.children().addAll(getPackEntries(category));
    }

    public void setCategory(Category category) {
        this.category = category;

        this.replaceEntries(getPackEntries(category));
    }

    private List<PackEntry> getPackEntries(Category category) {
        if (category == null) {
            return Collections.emptyList();
        }

        if (this.entryCache.containsKey(category)) {
            return this.entryCache.get(category);
        }

        List<PackEntry> entries = new ArrayList<>();
        for (Pack pack : category.getPacks()) {
            entries.add(new PackEntry(this.client, this.screen, pack));
        }

        this.entryCache.put(category, entries);

        return entries;
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

    @Override
    public int getRowWidth() {
        return this.width - ROW_LEFT_RIGHT_MARGIN * 2;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.left + getRowWidth() + SCROLLBAR_LEFT_MARGIN;
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
        boolean hasSelection = hasCategory && this.selectedPacks.containsKey(this.category);
        List<String> debugInfo = List.of(
                "WxH = " + this.width + "x" + this.height,
                "C = " + (hasCategory ? this.category.getName() : "null"),
                "S = " + (hasSelection ? this.selectedPacks.get(this.category).stream()
                        .map(Pack::getId).reduce("", (a, b) -> a + ", " + b) : ""),
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
    // endregion

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    public static class PackEntry extends EntryListWidget.Entry<PackEntry> {
        private final MinecraftClient client;
        private final VTDownloadScreen screen;
        private final Pack pack;
        private final Text name;

        private final Identifier icon;
        private boolean downloadedIcon = false;
        private boolean iconExists;

        private List<Text> description;
        private MultilineText shortDescription;

        public PackEntry(MinecraftClient client, VTDownloadScreen screen, Pack pack) {
            this.client = client;
            this.screen = screen;
            this.pack = pack;
            this.name = Text.of(pack.getName()).copy().formatted(Formatting.BOLD);

            this.icon = VTDMod.getIconId(pack);

            this.iconExists = this.client.getTextureManager().getOrDefault(this.icon, null) != null;
        }

        private List<Text> getDescriptionLines(int maxWidth) {
            TextHandler textHandler = this.client.textRenderer.getTextHandler();
            List<StringVisitable> visitableLines = textHandler.wrapLines(this.pack.getDescription(), maxWidth, Style.EMPTY);
            return visitableLines.stream().map(StringVisitable::getString).map(Text::of).toList();
        }

        private List<Text> getDescription(int maxWidth) {
            if (this.description != null) {
                return this.description;
            }

            this.description = getDescriptionLines(maxWidth);

            return this.description;
        }

        private MultilineText getShortDescription(int maxWidth) {
            if (this.shortDescription != null) {
                return this.shortDescription;
            }

            List<Text> lines = this.getDescriptionLines(maxWidth);
            if (lines.size() > 2) {
                lines = lines.subList(0, 2);
            }

            this.shortDescription = MultilineText.create(this.client.textRenderer, lines);

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

        // region entryRender
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = this.client.textRenderer;
            int iconSize = entryHeight - ICON_MARGIN;
            int centerX = x + (iconSize + entryWidth) / 2; // center over area left to the icon
            drawCenteredText(matrices, textRenderer, this.name, centerX, y, 0xFFFFFF);

            this.renderDescription(matrices, centerX, y + getLineHeight(textRenderer), entryWidth - iconSize);
            if (!DISABLE_ICONS) this.renderIcon(matrices, x + ICON_MARGIN, y + ICON_MARGIN, iconSize);

            this.renderTooltip(matrices, mouseX, mouseY, entryWidth / 2);
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

        private void renderTooltip(MatrixStack matrices, int mouseX, int mouseY, int width) {
            if (this.isMouseOver(mouseX, mouseY)) {
                this.screen.renderTooltip(matrices, this.getDescription(width), mouseX, mouseY);
            }
        }
        // endregion
    }

    // TODO: Warning entry
}
