package me.bymartrixx.vtd.gui.popup;

import com.mojang.blaze3d.platform.InputConstants;
import me.bymartrixx.vtd.VTDMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VersionSelectionPopup extends AbstractScreenPopup implements GuiEventListener, NarratableEntry {
    private static final int TITLE_MARGIN = 4;
    private static final int ENTRY_MARGIN = 4;
    private static final int ENTRY_HEIGHT = 20;
    private static final int WIDTH = 120;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SELECTED_COLOR = 0xFF55FF55;
    private static final int HOVERED_BACKGROUND_COLOR = 0x66FFFFFF;

    private final Component title;
    private final Consumer<String> onSelect;
    private final List<String> versions = new ArrayList<>();

    private String hoveredVersion;

    public VersionSelectionPopup(Minecraft client, int centerX, int centerY, Component title, Consumer<String> onSelect) {
        super(client, centerX, centerY, WIDTH, WIDTH);
        this.title = title;
        this.onSelect = onSelect;
    }

    public void updateVersions(List<String> versions) {
        this.versions.clear();
        this.versions.addAll(versions);

        if (this.shouldShow()) {
            this.updateSize();
        }
    }

    private void updateSize() {
        // Make sure the popup is wide enough for the title text
        int width = Math.max(WIDTH, this.client.font.width(this.title) + TITLE_MARGIN * 4);
        int height = TITLE_MARGIN * 2 + this.client.font.lineHeight
                + ENTRY_MARGIN + this.versions.size() * ENTRY_HEIGHT;
        this.updateSize(width, height);
    }

    public void show() {
        this.updateSize();
        this.show(-1.0F);
    }

    @Override
    protected boolean shouldUpdateTime() {
        // The popup stays open until the user selects a version or it is hidden manually
        return false;
    }

    private int getEntriesTop() {
        return this.getTop() + TITLE_MARGIN * 2 + this.client.font.lineHeight + ENTRY_MARGIN;
    }

    private int getEntryIndexAt(double mouseY) {
        return (int) ((mouseY - this.getEntriesTop()) / ENTRY_HEIGHT);
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.centeredText(this.client.font, this.title, this.centerX, this.getTop() + TITLE_MARGIN, TEXT_COLOR);

        this.hoveredVersion = null;
        int entriesTop = this.getEntriesTop();
        for (int i = 0; i < this.versions.size(); i++) {
            String version = this.versions.get(i);
            int y = entriesTop + i * ENTRY_HEIGHT;
            boolean hovered = mouseX >= this.getLeft() && mouseX < this.getRight()
                    && mouseY >= y && mouseY < y + ENTRY_HEIGHT;
            if (hovered) {
                this.hoveredVersion = version;
                graphics.fill(this.getLeft() + 2, y, this.getRight() - 2, y + ENTRY_HEIGHT,
                        HOVERED_BACKGROUND_COLOR);
            }

            int color = version.equals(VTDMod.VT_VERSION) ? SELECTED_COLOR : TEXT_COLOR;
            graphics.centeredText(this.client.font, Component.literal(version), this.centerX,
                    y + (ENTRY_HEIGHT - this.client.font.lineHeight) / 2, color);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (!this.shouldShow() || event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        double mouseX = event.x();
        double mouseY = event.y();
        if (this.isMouseOver(mouseX, mouseY)) {
            int index = this.getEntryIndexAt(mouseY);
            if (index >= 0 && index < this.versions.size()) {
                this.onSelect.accept(this.versions.get(index));
                this.hide();
            }

            return true;
        }

        return this.shouldShow();
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.title);
        if (this.hoveredVersion != null) {
            builder.add(NarratedElementType.USAGE, Component.literal(this.hoveredVersion));
        }
    }
}
