package io.github.bymartrixx.vtd.gui.widget;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import io.github.bymartrixx.vtd.gui.VTDScreen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Set;

public class SelectedPacksListWidget extends EntryListWidget<SelectedPacksListWidget.Entry> {
    public SelectedPacksListWidget() {
        super(VTDScreen.getInstance().getClient(), 160, VTDScreen.getInstance().height, 80, VTDScreen.getInstance().height - 60, 16);
        this.setRenderHeader(true, 16);

        this.updateEntries();
    }

    public int getRowWidth() {
        return this.width - 20;
    }

    protected int getScrollbarPositionX() {
        return VTDScreen.getInstance().width - 10;
    }

    protected void renderHeader(MatrixStack matrices, int x, int y, Tessellator tessellator) {
        Text text = new LiteralText("Selected packs").formatted(Formatting.BOLD, Formatting.UNDERLINE);
        VTDScreen.getInstance().getTextRenderer().draw(matrices, text, ((float) (this.width / 2 - VTDScreen.getInstance().getTextRenderer().getWidth(text) / 2) + (VTDScreen.getInstance().width - 170)), Math.min(this.top + 3, y), 16777215);
    }

    public void updateEntries() {
        this.children().clear();

        JsonObject selectedPacks = VTDMod.GSON.toJsonTree(VTDScreen.getInstance().selectedPacks).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> selectedPacksCategories = selectedPacks.entrySet();

        for (Map.Entry<String, JsonElement> category : selectedPacksCategories) {
            String categoryName = category.getKey();
            JsonArray packs = category.getValue().getAsJsonArray();

            this.addEntry(new Entry(true, categoryName));

            for (int i = 0; i < packs.size(); ++i) {
                String pack = packs.get(i).getAsString();

                this.addEntry(new Entry(false, categoryName, pack));
            }
        }
    }

    public class Entry extends EntryListWidget.Entry<SelectedPacksListWidget.Entry> {
        // TODO: Add buttons to move the entry up/down
        /**
         * If the entry is a category or a pack under one category.
         */
        public final boolean isCategory;
        /**
         * If {@link #isCategory} is true, the name of the category, if it is false, the name of the parent category.
         */
        public final String categoryName;
        /**
         * If {@link #isCategory} is false, keeps the name of the pack.
         * Use {@link #getPackName()} to get the value.
         */
        private String packName = "";

        Entry(boolean isCategory, String categoryName, String packName) {
            this(isCategory, categoryName);
            this.packName = packName;
        }

        Entry(boolean isCategory, String categoryName) {
            this.isCategory = isCategory;
            this.categoryName = categoryName;
        }

        /**
         * @return the pack name if {@link #isCategory} is false, or {@code ""} if it is true.
         */
        @SuppressWarnings("unused")
        public String getPackName() {
            return packName;
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String text = this.isCategory ? this.categoryName : this.packName;
            int textWidth = VTDScreen.getInstance().getTextRenderer().getWidth(text);

            if (textWidth > 136) {
                text = VTDScreen.getInstance().getTextRenderer().trimToWidth(text, 136 - VTDScreen.getInstance().getTextRenderer().getWidth("...")) + "...";
            }

            VTDScreen.getInstance().getTextRenderer().drawWithShadow(matrices, text, (this.isCategory ? 0 : 16) + VTDScreen.getInstance().width - 170, y + 1, 16777215);
        }
    }
}
