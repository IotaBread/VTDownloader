package io.github.bymartrixx.vtd.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.Iterator;

public class VTDScreen extends Screen {
    private static int getMaxTabNum(int width) {
        // The first 120 is because of the two 40 px wide buttons and the spacing between
        return (width - 120) / 120 + 1;
    }

    private final Screen previousScreen;
    private PackListWidget packSelectionList;
    private int tabIndex = 0;
    private int selectedTabIndex = 0;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
    }

    public void onClose() {
        this.client.openScreen(this.previousScreen);
    }

    protected void init() {
        this.children.add(packSelectionList);
        this.addButton(new ButtonWidget(this.width - 130, this.height - 30, 120, 20, new LiteralText("Done"), button -> this.onClose()));

        this.addButton(new ButtonWidget(10, 30, 40, 20, new LiteralText("<-"), button -> {
            if (this.tabIndex > 0) {
                --this.tabIndex;

                this.buttons.clear();
                this.children.clear();
                this.init();
            }
        }));
        this.addButton(new ButtonWidget(60, 30, 40, 20, new LiteralText("->"), button -> {
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

            this.addButton(new ButtonWidget(i * 130 + 110, 30, 120, 20, new LiteralText(categoryName), button -> {
                if (this.selectedTabIndex != index) {
                    this.selectedTabIndex = index;
                    this.loadPacks(category.get("packs").getAsJsonArray());
                }
            }));
        }

        // Load the selected category packs
        this.loadPacks(VTDMod.categories.get(selectedTabIndex).getAsJsonObject().get("packs").getAsJsonArray());
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.packSelectionList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void loadPacks(JsonArray packs) {
        this.packSelectionList = new PackListWidget(this.client, packs);
    }

    class PackListWidget extends EntryListWidget<VTDScreen.PackListWidget.PackEntry> {
        public PackListWidget(MinecraftClient client, JsonArray packs) {
            super(client, VTDScreen.this.width, VTDScreen.this.height, 42, VTDScreen.this.height - 61, 20);

            Iterator<JsonElement> packIterator = packs.iterator();

            while (packIterator.hasNext()) {
                JsonObject pack = packIterator.next().getAsJsonObject();
                PackEntry entry = new PackEntry(pack);

                this.addEntry(entry);
            }
        }

        class PackEntry extends EntryListWidget.Entry<PackListWidget.PackEntry> {
            private final String name;
            private final String displayName;
            private final String description;
            private final String[] incompatiblePacks;

            PackEntry(JsonObject pack) {
                this.name = pack.get("name").getAsString();

                this.displayName = pack.get("display").getAsString();
                this.description = pack.get("description").getAsString();

                Iterator<JsonElement> incompatiblePackIterator = pack.get("incompatible").getAsJsonArray().iterator();
                ArrayList<String> incompatiblePacks = new ArrayList<String>();

                while (incompatiblePackIterator.hasNext()) {
                    incompatiblePacks.add(incompatiblePackIterator.next().getAsString());
                }

                this.incompatiblePacks = incompatiblePacks.toArray(new String[incompatiblePacks.size()]);
            }

            PackEntry(String name, String displayName, String description, String[] incompatiblePacks) {
                this.name = name;

                this.displayName = displayName;
                this.description = description;
                this.incompatiblePacks = incompatiblePacks;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                VTDScreen.this.textRenderer.drawWithShadow(matrices, this.name, VTDScreen.this.width / 2 - VTDScreen.this.textRenderer.getWidth(this.name) / 2, y + 1, 16777215);
            }
        }
    }
}
