package io.github.bymartrixx.vtd.gui;

import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class VTDScreen extends Screen {
    private static int getMaxTabNum(int width) {
        // The first 120 is because of the two 40 px wide buttons and the spacing between
        return (width - 120) / 120 + 1;
    }

    private final Screen previousScreen;
    private int tabIndex = 0;
    private int selectedTab = 0;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
    }

    public void onClose() {
        this.client.openScreen(this.previousScreen);
    }

    protected void init() {
        this.addButton(new ButtonWidget(this.width - 130, this.height - 30, 120, 20, new LiteralText("Done"), button -> this.onClose()));

        this.addButton(new ButtonWidget(10, 10, 40, 20, new LiteralText("<-"), button -> {
            if (this.tabIndex > 0) {
                --this.tabIndex;

                this.buttons.clear();
                this.children.clear();
                this.init();
            }
        }));
        this.addButton(new ButtonWidget(60, 10, 40, 20, new LiteralText("->"), button -> {
            if (this.tabIndex <= VTDMod.categories.size() - getMaxTabNum(this.width)) {
                ++this.tabIndex;

                this.buttons.clear();
                this.children.clear();
                this.init();
            }
        }));

        for (int i = 0; i < getMaxTabNum(this.width); ++i) {
            int index = i + tabIndex;
            if (index >= VTDMod.categories.size()) break;

            JsonObject category = VTDMod.categories.get(index).getAsJsonObject();
            String categoryName = category.get("category").getAsString();

            this.addButton(new ButtonWidget(i * 130 + 110, 10, 120, 20, new LiteralText(categoryName), button -> {
                if (this.selectedTab != index)
                    this.selectedTab = index;
                // TODO: Init entries
            }));
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
