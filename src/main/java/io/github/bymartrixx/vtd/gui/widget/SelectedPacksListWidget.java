package io.github.bymartrixx.vtd.gui.widget;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import io.github.bymartrixx.vtd.gui.VTDScreen;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class SelectedPacksListWidget extends EntryListWidget<SelectedPacksListWidget.Entry> {
    private static final Identifier RESOURCE_PACKS_TEXTURE = new Identifier("textures/gui/resource_packs.png");

    public SelectedPacksListWidget() {
        super(VTDScreen.getInstance().getClient(), 160, VTDScreen.getInstance().height, 80, VTDScreen.getInstance().height - 60, 16);
        this.setRenderHeader(true, 16);

        this.updateEntries();
    }

    public int getRowWidth() {
        return this.width - 20;
    }

    protected int getScrollbarPositionX() {
        return this.method_31383(); // this.getRowLeft() + this.getRowWidth();
    }

    protected void renderHeader(MatrixStack matrices, int x, int y, Tessellator tessellator) {
        Text text = new TranslatableText("vtd.selectedPacks").formatted(Formatting.BOLD, Formatting.UNDERLINE);
        VTDScreen.getInstance().getTextRenderer().draw(matrices, text, ((float) (this.width / 2 - VTDScreen.getInstance().getTextRenderer().getWidth(text) / 2) + (VTDScreen.getInstance().width - 170)), Math.min(this.top + 3, y), 16777215);
    }

    public void updateEntries() {
        this.children().clear();

        JsonObject selectedPacks = VTDMod.GSON.toJsonTree(VTDScreen.getInstance().selectedPacks).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> selectedPacksCategories = selectedPacks.entrySet();

        for (Map.Entry<String, JsonElement> category : selectedPacksCategories) {
            String categoryName = category.getKey();
            JsonArray packs = category.getValue().getAsJsonArray();

            this.addEntry(new Entry(this, true, categoryName));

            for (int i = 0; i < packs.size(); ++i) {
                String pack = packs.get(i).getAsString();

                this.addEntry(new Entry(this, false, categoryName, pack));
            }
        }

        this.setScrollAmount(this.getScrollAmount()); // Update scrollbar
    }

    public class Entry extends EntryListWidget.Entry<SelectedPacksListWidget.Entry> {
        /**
         * If the entry is a category or a pack under one category.
         */
        public final boolean isCategory;
        /**
         * If {@link #isCategory} is true, the name of the category, if it is false, the name of the parent category.
         */
        public final String categoryName;
        private final SelectedPacksListWidget widget;
        /**
         * If {@link #isCategory} is false, keeps the name of the pack.
         * Use {@link #getPackName()} to get the value.
         */
        private String packName = "";

        Entry(SelectedPacksListWidget widget, boolean isCategory, String categoryName, String packName) {
            this(widget, isCategory, categoryName);
            this.packName = packName;
        }

        Entry(SelectedPacksListWidget widget, boolean isCategory, String categoryName) {
            this.widget = widget;
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
            if (hovered) {
                fill(matrices, x, y, x + entryWidth, y + 16, -1601138544);
            }

            String text = this.isCategory ? this.categoryName : this.packName;
            int textWidth = VTDScreen.getInstance().getTextRenderer().getWidth(text);

            if (textWidth > this.widget.getRowWidth() - (this.isCategory ? 24 : 36)) {
                text = VTDScreen.getInstance().getTextRenderer().trimToWidth(text, this.widget.getRowWidth() - (this.isCategory ? 24 : 36) - VTDScreen.getInstance().getTextRenderer().getWidth("...")) + "...";
            }

            VTDScreen.getInstance().getTextRenderer().drawWithShadow(matrices, text, x + (this.isCategory ? 4 : 16), y + 4, 16777215);

            // Render up/down buttons
            if (hovered) {
                int localMouseX = mouseX - x;
                int localMouseY = mouseY - y;

                VTDScreen.getInstance().getClient().getTextureManager().bindTexture(RESOURCE_PACKS_TEXTURE);

                if (this.canMoveUp()) {
                    if (localMouseX > entryWidth - 8 && localMouseY < 8) {
                        DrawableHelper.drawTexture(matrices, x + entryWidth - 16, y, 16, 16, 96.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        DrawableHelper.drawTexture(matrices, x + entryWidth - 16, y, 16, 16, 96.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (this.canMoveDown()) {
                    if (localMouseX > entryWidth - 8 && localMouseY > 8) {
                        DrawableHelper.drawTexture(matrices, x + entryWidth - 16, y, 16, 16, 64.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        DrawableHelper.drawTexture(matrices, x + entryWidth - 16, y, 16, 16, 64.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double x = mouseX - this.widget.getRowLeft();
            double y = mouseY - this.widget.getRowTop(this.widget.children().indexOf(this));

            if (x > this.widget.getRowWidth() - 8) {
                if (y < 8 && this.canMoveUp()) {
                    this.moveUp();
                    this.widget.updateEntries();
                    return true;
                }

                if (y > 8 && this.canMoveDown()) {
                    this.moveDown();
                    this.widget.updateEntries();
                    return true;
                }
            }

            return false;
        }

        private boolean canMoveUp() {
            if (this.isCategory) {
                return !VTDScreen.getInstance().selectedPacks.keySet().toArray()[0].equals(this.categoryName);
            } else {
                return !VTDScreen.getInstance().selectedPacks.get(this.categoryName).toArray()[0].equals(this.packName);
            }
        }

        private boolean canMoveDown() {
            if (this.isCategory) {
                Object[] categories = VTDScreen.getInstance().selectedPacks.keySet().toArray();
                return !categories[categories.length - 1].equals(this.categoryName);
            } else {
                Object[] packs = VTDScreen.getInstance().selectedPacks.get(this.categoryName).toArray();
                return !packs[packs.length - 1].equals(this.packName);
            }
        }

        private void moveUp() {
            if (this.isCategory) {
                Map<String, List<String>> selectedPacks = VTDScreen.getInstance().selectedPacks;
                Iterator<String> categoriesIterator = selectedPacks.keySet().iterator();

                String previousCategoryName = "";
                // Save previous category name, if this category name is found, keep previous category name
                while (categoriesIterator.hasNext()) {
                    String categoryName = categoriesIterator.next();

                    if (categoryName.equals(this.categoryName)) {
                        break;
                    }

                    previousCategoryName = categoryName;
                }

                if (!previousCategoryName.equals("")) {
                    Map<String, List<String>> newSelectedPacks = new LinkedHashMap<>();

                    // Copy values to new map, where the order is changed
                    // When the previous entry is found, put this entry (in the place of the previous entry), skip, and put the previous entry
                    for (int i = 0; i < selectedPacks.size(); ++i) {
                        String categoryName = (String) selectedPacks.keySet().toArray()[i];

                        if (!categoryName.equals(previousCategoryName)) {
                            newSelectedPacks.put(categoryName, selectedPacks.get(categoryName));
                        } else {
                            newSelectedPacks.put(this.categoryName, selectedPacks.get(this.categoryName)); // Put this entry
                            ++i; // Skip
                            newSelectedPacks.put(previousCategoryName, selectedPacks.get(previousCategoryName)); // Put the previous entry
                        }
                    }

                    VTDScreen.getInstance().selectedPacks.clear();
                    VTDScreen.getInstance().selectedPacks.putAll(newSelectedPacks);
                }
            } else {
                List<String> selectedPacksOfCategory = VTDScreen.getInstance().selectedPacks.get(this.categoryName);
                Iterator<String> packsIterator = selectedPacksOfCategory.iterator();

                String previousPackName = "";
                // Save previous category name, if this category name is found, keep previous category name
                while (packsIterator.hasNext()) {
                    String packName = packsIterator.next();

                    if (packName.equals(this.packName)) {
                        break;
                    }

                    previousPackName = packName;
                }

                if (!previousPackName.equals("")) {
                    List<String> newSelectedPacksOfCategory = new ArrayList<>();

                    // Copy values to new list, where the order is changed
                    // When the previous entry is found, put this entry (in the place of the previous entry), skip, and put the previous entry
                    for (int i = 0; i < selectedPacksOfCategory.size(); ++i) {
                        String packName = selectedPacksOfCategory.get(i);

                        if (!packName.equals(previousPackName)) {
                            newSelectedPacksOfCategory.add(packName);
                        } else {
                            newSelectedPacksOfCategory.add(this.packName); // Put this entry
                            ++i; // Skip
                            newSelectedPacksOfCategory.add(previousPackName); // Put the previous entry
                        }
                    }

                    VTDScreen.getInstance().selectedPacks.replace(this.categoryName, newSelectedPacksOfCategory);
                }
            }
        }

        private void moveDown() {
            if (this.isCategory) {
                Map<String, List<String>> selectedPacks = VTDScreen.getInstance().selectedPacks;
                Iterator<String> categoriesIterator = selectedPacks.keySet().iterator();

                String nextCategoryName = "";
                while (categoriesIterator.hasNext()) {
                    String categoryName = categoriesIterator.next();

                    if (categoryName.equals(this.categoryName)) {
                        nextCategoryName = categoriesIterator.next();
                        break;
                    }
                }

                if (!nextCategoryName.equals("")) {
                    Map<String, List<String>> newSelectedPacks = new LinkedHashMap<>();

                    // Copy values to new map, where the order is changed
                    // When this entry is found, put next entry (in the place of this entry), skip, and put this entry
                    for (int i = 0; i < selectedPacks.size(); ++i) {
                        String categoryName = (String) selectedPacks.keySet().toArray()[i];

                        if (!categoryName.equals(this.categoryName)) {
                            newSelectedPacks.put(categoryName, selectedPacks.get(categoryName));
                        } else {
                            newSelectedPacks.put(nextCategoryName, selectedPacks.get(nextCategoryName)); // Put next entry
                            ++i; // Skip
                            newSelectedPacks.put(this.categoryName, selectedPacks.get(this.categoryName)); // Put this entry
                        }
                    }

                    VTDScreen.getInstance().selectedPacks.clear();
                    VTDScreen.getInstance().selectedPacks.putAll(newSelectedPacks);
                }
            } else {
                List<String> selectedPacksOfCategory = VTDScreen.getInstance().selectedPacks.get(this.categoryName);
                Iterator<String> packsIterator = selectedPacksOfCategory.iterator();

                String nextPackName = "";
                while (packsIterator.hasNext()) {
                    String packName = packsIterator.next();

                    if (packName.equals(this.packName)) {
                        nextPackName = packsIterator.next();
                        break;
                    }
                }

                if (!nextPackName.equals("")) {
                    List<String> newSelectedPacksOfCategory = new ArrayList<>();

                    // Copy values to new list, where the order is changed
                    // When this entry is found, put next entry (in the place of this entry), skip, and put this entry
                    for (int i = 0; i < selectedPacksOfCategory.size(); ++i) {
                        String packName = selectedPacksOfCategory.get(i);

                        if (!packName.equals(this.packName)) {
                            newSelectedPacksOfCategory.add(packName);
                        } else {
                            newSelectedPacksOfCategory.add(nextPackName); // Put next entry
                            ++i; // Skip
                            newSelectedPacksOfCategory.add(this.packName); // Put this entry
                        }
                    }

                    VTDScreen.getInstance().selectedPacks.replace(this.categoryName, newSelectedPacksOfCategory);
                }
            }
        }
    }
}
