package io.github.bymartrixx.vtd.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;

public class VTDScreen extends Screen {
    private final Screen previousScreen;
    private final ArrayList<ButtonWidget> tabButtons = Lists.newArrayList();
    private ButtonWidget tabLeftButton;
    private ButtonWidget tabRightButton;
    private ButtonWidget downloadButton;
    private ButtonWidget doneButton;
    private int tabIndex = 0;
    private int selectedTabIndex = 0;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
    }

    /**
     * Get the number of "tabs" that should be generated/rendered.
     *
     * @param width the width of the screen.
     * @return The max number of "tabs"
     */
    private static int getTabNum(int width) {
        return (width - 80) / 120 + 1;
    }

    protected void init() {
        this.tabLeftButton = this.addButton(new ButtonWidget(10, 30, 20, 20, new LiteralText("<="), button -> {
            --this.tabIndex;
            this.updateTabButtons();
        }));
        this.tabRightButton = this.addButton(new ButtonWidget(40, 30, 20, 20, new LiteralText("=>"), button -> {
            ++this.tabIndex;
            this.updateTabButtons();
        }));

        this.doneButton = this.addButton(new ButtonWidget(this.width - 130, this.height - 30, 120, 20, new LiteralText("Done"), button -> this.onClose()));
        this.downloadButton = this.addButton(new ButtonWidget(this.width - 260, this.height - 30, 120, 20, new LiteralText("Download"), button -> {
            System.out.println("Placeholder button!");
            // TODO
        }));

        this.updateTabButtons();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);

        // Render tabButtons
        for (int i = 0; i < this.tabButtons.size(); ++i) {
            this.tabButtons.get(i).render(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    public void onClose() {
        this.client.openScreen(this.previousScreen);
    }

    private void updateTabButtons() {
        this.tabLeftButton.active = this.tabIndex > 0;
        this.tabRightButton.active = this.tabIndex <= VTDMod.categories.size() - getTabNum(this.width);

        this.downloadButton.active = false; // TODO

        this.tabButtons.clear();
        for (int i = 0; i < getTabNum(this.width); ++i) {
            int index = i + this.tabIndex;
            if (index >= VTDMod.categories.size()) break;

            JsonObject category = VTDMod.categories.get(index).getAsJsonObject();
            String categoryName = category.get("category").getAsString();
            ButtonWidget buttonWidget = new ButtonWidget(i * 130 + 70, 30, 120, 20, new LiteralText(categoryName), button -> {
                if (this.selectedTabIndex != index)
                    this.selectedTabIndex = index;
            });

            this.tabButtons.add(i, buttonWidget);
        }
    }
}
