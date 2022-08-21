package me.bymartrixx.vtd.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Dynamic rendering
public class PackSelectionListWidget extends EntryListWidget<PackSelectionListWidget.PackEntry> {
    private static final boolean SHOW_DEBUG_INFO = true;
    private static final List<String> HARD_INCOMPATIBLE_CATEGORIES = List.of("Menu Panoramas", "Options Backgrounds", "Colorful Slime");

    private static final Text ERROR_URL = Text.of(VTDMod.BASE_URL).copy()
            .formatted(Formatting.UNDERLINE, Formatting.ITALIC, Formatting.BLUE)
            .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VTDMod.BASE_URL)));
    private static final Text ERROR_HEADER_TEXT = Text.translatable("vtd.packError.title.1").formatted(Formatting.BOLD, Formatting.ITALIC);
    private static final Text ERROR_HEADER_TEXT_2 = Text.translatable("vtd.packError.title.2").formatted(Formatting.BOLD, Formatting.ITALIC);
    private static final Text ERROR_TEXT = Text.translatable("vtd.packError.body.1");
    private static final Text ERROR_TEXT_2 = Text.translatable("vtd.packError.body.2");
    private static final Text ERROR_TEXT_3 = Text.translatable("vtd.packError.body.3", ERROR_URL);

    private static final int ICON_TEXTURE_SIZE = 90;
    private static final int TEXT_MARGIN = 2;
    private static final int ICON_MARGIN = 1;

    private final Map<Category, List<PackEntry>> entryCache = new HashMap<>();
    private Category category;

    private final Map<Category, List<Pack>> selectedPacks;

    public PackSelectionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                                   Map<Category, List<Pack>> selectedPacks, Category category) {
        super(client, width, height, top, bottom, itemHeight);
        this.selectedPacks = selectedPacks;
        this.category = category;

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
            entries.add(new PackEntry(this.client, pack));
        }

        this.entryCache.put(category, entries);

        return entries;
    }

    // region input callbacks
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.children().isEmpty() &&
                mouseX >= this.left && mouseX <= this.right &&
                mouseY >= this.top && mouseY < this.bottom) {
            // Handle clicks when the error is shown
            // TODO
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
            this.renderDebugInfo(matrices);
        }
    }

    private void renderError(MatrixStack matrices) {
        TextRenderer textRenderer = this.client.textRenderer;

        int x = this.left + this.width / 2;
        int y = this.height / 2;
        int fontHeight = textRenderer.fontHeight;

        drawCenteredText(matrices, textRenderer, ERROR_HEADER_TEXT, x, (int) (y - fontHeight * 2.2 - TEXT_MARGIN), 0xFFFFFF);
        drawCenteredText(matrices, textRenderer, ERROR_HEADER_TEXT_2, x, (int) (y - fontHeight * 1.2 - TEXT_MARGIN), 0xFFFFFF);
        drawCenteredText(matrices, textRenderer, ERROR_TEXT, x, y, 0xFFFFFF);
        drawCenteredText(matrices, textRenderer, ERROR_TEXT_2, x, y + fontHeight + TEXT_MARGIN, 0xFFFFFF);
        drawCenteredText(matrices, textRenderer, ERROR_TEXT_3, x, y + fontHeight * 2 + TEXT_MARGIN, 0xFFFFFF);
    }

    private void renderDebugInfo(MatrixStack matrices) {
        TextRenderer textRenderer = this.client.textRenderer;

        boolean hasCategory = this.category != null;
        boolean hasSelection = hasCategory && this.selectedPacks.containsKey(this.category);
        List<String> debugInfo = List.of(
                "C = " + (hasCategory ? this.category.getName() : "null"),
                "S = " + (hasSelection ? this.selectedPacks.get(this.category).stream()
                        .map(Pack::getId).reduce("", (a, b) -> a + ", " + b) : "")
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
        private final Pack pack;
        private final Text name;

        private final Identifier icon;
        private boolean downloadedIcon = false;
        private boolean iconExists;

        public PackEntry(MinecraftClient client, Pack pack) {
            this.client = client;
            this.pack = pack;
            this.name = Text.of(pack.getName());

            this.icon = VTDMod.getIconId(pack);

            this.iconExists = this.client.getTextureManager().getOrDefault(this.icon, null) != null;
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
                }
            });
        }

        // region entryRender
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = this.client.textRenderer;
            int centerX = x + entryWidth / 2;
            drawCenteredText(matrices, textRenderer, this.name, centerX, y, 0xFFFFFF);

            this.renderDescription(matrices, centerX, y + textRenderer.fontHeight + TEXT_MARGIN);
            this.renderIcon(matrices, x + ICON_MARGIN, y + ICON_MARGIN, entryHeight - ICON_MARGIN);
        }

        private void renderDescription(MatrixStack matrices, int x, int y) {
            // TODO
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

    // TODO: Warning entry
}
