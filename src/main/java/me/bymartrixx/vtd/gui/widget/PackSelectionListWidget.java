package me.bymartrixx.vtd.gui.widget;

import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Dynamic rendering
public class PackSelectionListWidget extends EntryListWidget<PackSelectionListWidget.PackEntry> {
    private static final boolean SHOW_DEBUG_INFO = true;
    private static final List<String> HARD_INCOMPATIBLE_CATEGORIES = List.of("Menu Panoramas", "Options Backgrounds", "Colorful Slime");

    private final Map<Category, List<PackEntry>> packCache = new HashMap<>();
    private Category category;

    private final Map<Category, List<Pack>> selectedPacks;

    public PackSelectionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                                   Map<Category, List<Pack>> selectedPacks, Category category) {
        super(client, width, height, top, bottom, itemHeight);
        this.selectedPacks = selectedPacks;
        this.category = category;
        // TODO
    }

    public void setCategory(Category category) {
        this.category = category;
        // TODO: Update pack entries
    }

    // region render
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if (SHOW_DEBUG_INFO) {
            this.renderDebugInfo(matrices);
        }
    }

    private void renderDebugInfo(MatrixStack matrices) {
        TextRenderer textRenderer = this.client.textRenderer;

        List<String> debugInfo = List.of(
                "C = " + (this.category != null ? this.category.getName() : "null")
        );

        // Make text half its size
        matrices.push();
        matrices.scale(0.5f, 0.5f, 0.5f);

        float lineHeight = textRenderer.fontHeight + 2;
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
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // TODO
        }
    }
}
