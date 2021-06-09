package me.bymartrixx.vtd.gui.widget;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.gui.VTDScreen;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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
        return this.getRowRight();
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

    /**
     * A cursed fix to #2. Bedrockify mixins into
     * {@link EntryListWidget#render(MatrixStack, int, int, float)} and "disables" calls to
     * {@link #renderBackground(MatrixStack)}. In order to "enable" it again, I have copied
     * the method and, because it's a different method on a different class Bedrockify won't
     * affect it with a mixin.
     */
    @Deprecated
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        int i = this.getScrollbarPositionX();
        int j = i + 6;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
//        if (this.field_26846) { // Private field, true
        this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.bottom, 0.0D).texture((float)this.left / 32.0F, (float)(this.bottom + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D).texture((float)this.right / 32.0F, (float)(this.bottom + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D).texture((float)this.right / 32.0F, (float)(this.top + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D).texture((float)this.left / 32.0F, (float)(this.top + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
        tessellator.draw();
//        }

        int k = this.getRowLeft();
        int l = this.top + 4 - (int)this.getScrollAmount();
//        if (this.renderHeader) { // Private field, true
        this.renderHeader(matrices, k, l, tessellator);
//        }

        this.renderList(matrices, k, l, mouseX, mouseY, delta);
//        if (this.field_26847) { // Private field, true
        this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        float g = 32.0F;
//            int m = true; // What
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, this.top, -100.0D).texture(0.0F, (float)this.top / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex((this.left + this.width), this.top, -100.0D).texture((float)this.width / 32.0F, (float)this.top / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex((this.left + this.width), 0.0D, -100.0D).texture((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left, 0.0D, -100.0D).texture(0.0F, 0.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left, this.height, -100.0D).texture(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex((this.left + this.width), this.height, -100.0D).texture((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex((this.left + this.width), this.bottom, -100.0D).texture((float)this.width / 32.0F, (float)this.bottom / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.left, this.bottom, -100.0D).texture(0.0F, (float)this.bottom / 32.0F).color(64, 64, 64, 255).next();
        tessellator.draw();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
//            int n = true; What
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, (this.top + 4), 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, (this.top + 4), 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, (this.bottom - 4), 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.left, (this.bottom - 4), 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 0).next();
        tessellator.draw();
//        }

        int o = this.getMaxScroll();
        if (o > 0) {
            RenderSystem.disableTexture();
            int p = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
            p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
            int q = (int)this.getScrollAmount() * (this.bottom - this.top - p) / o + this.top;
            if (q < this.top) {
                q = this.top;
            }

            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(i, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(j, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(j, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(i, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(i, (q + p), 0.0D).texture(0.0F, 1.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(j, (q + p), 0.0D).texture(1.0F, 1.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(j, q, 0.0D).texture(1.0F, 0.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(i, q, 0.0D).texture(0.0F, 0.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(i, (q + p - 1), 0.0D).texture(0.0F, 1.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex((j - 1), (q + p - 1), 0.0D).texture(1.0F, 1.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex((j - 1), q, 0.0D).texture(1.0F, 0.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex(i, q, 0.0D).texture(0.0F, 0.0F).color(192, 192, 192, 255).next();
            tessellator.draw();
        }

        this.renderDecorations(matrices, mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
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
