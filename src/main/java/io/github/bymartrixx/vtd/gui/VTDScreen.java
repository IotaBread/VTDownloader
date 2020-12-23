package io.github.bymartrixx.vtd.gui;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import io.github.bymartrixx.vtd.gui.widget.DownloadButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class VTDScreen extends Screen {
    private static final Gson GSON = new Gson();

    private final Screen previousScreen;
    private final ArrayList<ButtonWidget> tabButtons = Lists.newArrayList();
    private final JsonObject selectedPacks; // {"$category":["$pack","$pack"],"$category":["$pack"]}
    private ButtonWidget tabLeftButton;
    private ButtonWidget tabRightButton;
    private DownloadButtonWidget downloadButton;
    private PackListWidget listWidget;
    private int tabIndex = 0;
    private int selectedTabIndex = 0;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
        this.selectedPacks = new JsonObject();
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

    private static void download(JsonObject selectedPacks, MinecraftClient minecraftClient) throws IOException {
        // Get the download link
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://vanillatweaks.net/assets/server/zipresourcepacks.php");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("version", "1.16"));
            params.add(new BasicNameValuePair("packs", GSON.toJson(selectedPacks)));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = client.execute(httpPost);

            int responseStatus = response.getStatusLine().getStatusCode();

            if (responseStatus != 200) {
                VTDMod.log(Level.WARN, "The download link request responded with an unexpected status code: {}", responseStatus);
                return;
            }

            StringBuilder responseBody = new StringBuilder();
            Scanner scanner = new Scanner(response.getEntity().getContent());

            while (scanner.hasNext()) {
                responseBody.append(scanner.nextLine());
            }

            String downloadLink = GSON.fromJson(responseBody.toString(), JsonObject.class).get("link").getAsString();
            String fileName = downloadLink.split("/")[downloadLink.split("/").length - 1];

            // Download the resource pack
            FileUtils.copyURLToFile(new URL("https://vanillatweaks.net" + downloadLink), new File(minecraftClient.getResourcePackDir(), fileName), 500, 4000);
        }
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

        // Done button
        this.addButton(new ButtonWidget(this.width - 130, this.height - 30, 120, 20, new LiteralText("Done"), button -> this.onClose()));

        this.downloadButton = this.addButton(new DownloadButtonWidget(this.width - 300, this.height - 30, 160, 20, new LiteralText("Download"), new LiteralText("Pack downloaded!"), new LiteralText("Unexpected error!"), button -> {
            // Save current category before downloading
            this.savePacks(this.listWidget);

            try {
                download(this.selectedPacks, this.client);
                this.downloadButton.setSuccess(true);
            } catch (IOException e) {
                VTDMod.logError("Encountered an exception while trying to download the resource pack.", e);
                this.downloadButton.setSuccess(false);
            }
        }));

        JsonObject category = VTDMod.categories.get(selectedTabIndex).getAsJsonObject();

        if (this.listWidget != null)
            this.savePacks(this.listWidget);

        this.listWidget = this.addChild(new VTDScreen.PackListWidget(category.get("packs").getAsJsonArray(), category.get("category").getAsString()));

        this.updateTabButtons();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.listWidget.render(matrices, mouseX, mouseY, delta); // Render pack list
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215); // Render title

        // Render tabButtons
        for (ButtonWidget tabButton : this.tabButtons) {
            tabButton.render(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    public void tick() {
        this.downloadButton.tick();
    }

    public void onClose() {
        this.client.openScreen(this.previousScreen);
    }

    private void updateTabButtons() {
        this.tabLeftButton.active = this.tabIndex > 0;
        this.tabRightButton.active = this.tabIndex <= VTDMod.categories.size() - getTabNum(this.width);

        this.updateDownloadButton();

        this.tabButtons.clear();

        // Remove old buttons from this.children
        for (JsonElement category : VTDMod.categories) {
            String categoryName = category.getAsJsonObject().get("category").getAsString();
            this.children.removeIf(element -> element instanceof ButtonWidget && ((ButtonWidget) element).getMessage().asString().equals(categoryName));
        }

        for (int i = 0; i < getTabNum(this.width); ++i) {
            int index = i + this.tabIndex;
            if (index >= VTDMod.categories.size()) break;

            JsonObject category = VTDMod.categories.get(index).getAsJsonObject();
            String categoryName = category.get("category").getAsString();
            ButtonWidget buttonWidget = new ButtonWidget(i * 130 + 70, 30, 120, 20, new LiteralText(categoryName), button -> {
                if (this.selectedTabIndex != index) {
                    this.selectedTabIndex = index;

                    this.children.remove(this.listWidget);
                    // Doesn't work as expected :/
//                    this.listWidget.replaceEntries(VTDMod.categories.get(selectedTabIndex).getAsJsonObject().get("packs").getAsJsonArray());

                    this.savePacks(this.listWidget);
                    this.updateDownloadButton();
                    JsonObject category2 = VTDMod.categories.get(selectedTabIndex).getAsJsonObject();
                    this.listWidget = this.addChild(new VTDScreen.PackListWidget(category2.get("packs").getAsJsonArray(), category2.get("category").getAsString()));
                }
            });

            this.tabButtons.add(i, buttonWidget);
            this.children.add(buttonWidget);
        }
    }

    private void updateDownloadButton() {
        this.downloadButton.active = this.selectedPacks.size() > 0;
    }

    private void savePacks(PackListWidget packListWidget) {
        List<PackListWidget.PackEntry> selectedEntries = packListWidget.selectedEntries;

        JsonArray packsArray = new JsonArray();
        if (this.selectedPacks.has(packListWidget.categoryName)) {
            this.selectedPacks.remove(packListWidget.categoryName);
        }

        if (selectedEntries.size() == 0) return;

        for (PackListWidget.PackEntry entry : selectedEntries) {
            packsArray.add(entry.name);
        }

        this.selectedPacks.add(packListWidget.categoryName, packsArray);
    }

    class PackListWidget extends EntryListWidget<VTDScreen.PackListWidget.PackEntry> {
        public final String categoryName;
        private final List<VTDScreen.PackListWidget.PackEntry> selectedEntries = new ArrayList<>();

        public PackListWidget(JsonArray packs, String categoryName) {
            super(VTDScreen.this.client, VTDScreen.this.width, VTDScreen.this.height, 60, VTDScreen.this.height - 40, 32);

            this.categoryName = categoryName;

            for (int i = 0; i < packs.size(); ++i) {
                JsonObject pack = packs.get(i).getAsJsonObject();
                boolean selected = VTDScreen.this.selectedPacks.has(this.categoryName) && VTDScreen.this.selectedPacks.get(this.categoryName).getAsJsonArray().contains(pack.get("name"));

                this.addEntry(new PackEntry(pack, selected));
            }
        }

        public int getRowWidth() {
            return this.width - 20;
        }

        protected int getScrollbarPositionX() {
            return this.width - 10;
        }

        // TODO: Incompatible packs warning

        protected void replaceEntries(JsonArray newPacks) {
            this.selectedEntries.clear();

            List<PackEntry> newEntries = new ArrayList<>();

            for (int i = 0; i < newPacks.size(); ++i) {
                JsonObject pack = newPacks.get(i).getAsJsonObject();

                newEntries.add(new PackEntry(pack));
            }

            super.replaceEntries(newEntries);
        }

        public void setSelected(@Nullable VTDScreen.PackListWidget.PackEntry entry) {
            this.setSelected(entry, true);
        }

        public void setSelected(@Nullable VTDScreen.PackListWidget.PackEntry entry, boolean child) {
            if (this.children().contains(entry) || !child) {
                if (!this.selectedEntries.contains(entry))
                    this.selectedEntries.add(entry);
                else
                    this.selectedEntries.remove(entry);

                VTDScreen.this.savePacks(this);
                VTDScreen.this.updateDownloadButton();
            }
        }

        public boolean isSelected(VTDScreen.PackListWidget.PackEntry entry) {
            if (!this.children().contains(entry)) return false;

            return this.selectedEntries.contains(entry);
        }

        protected boolean isSelectedItem(int index) {
            return this.isSelected(this.children().get(index));
        }

        public List<PackEntry> getSelectedEntries() {
            return this.selectedEntries;
        }

        class PackEntry extends EntryListWidget.Entry<VTDScreen.PackListWidget.PackEntry> {
            private final String name;
            private final String displayName;
            private final String description;
            private final String[] incompatiblePacks;

            PackEntry(JsonObject pack) {
                this(pack, false);
            }

            PackEntry(JsonObject pack, boolean selected) {
                this.name = pack.get("name").getAsString();

                this.displayName = pack.get("display").getAsString();
                this.description = pack.get("description").getAsString();

                Iterator<JsonElement> incompatiblePacksIterator = pack.get("incompatible").getAsJsonArray().iterator();
                ArrayList<String> incompatiblePacks = new ArrayList<>();

                while (incompatiblePacksIterator.hasNext()) {
                    incompatiblePacks.add(incompatiblePacksIterator.next().getAsString());
                }

                this.incompatiblePacks = incompatiblePacks.toArray(new String[0]);

                if (selected)
                    PackListWidget.this.setSelected(this, false);
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    this.setSelected();
                }

                return false;
            }

            private void setSelected() {
                PackListWidget.this.setSelected(this);
            }

            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                VTDScreen.this.textRenderer.drawWithShadow(matrices, this.displayName, ((float) (VTDScreen.this.width / 2 - VTDScreen.this.textRenderer.getWidth(this.displayName) / 2)), y + 1, 16777215);
                this.renderDescription(matrices, y);
            }

            private void renderDescription(MatrixStack matrices, int y) {
                int textWidth = VTDScreen.this.textRenderer.getWidth(this.description);

                if (textWidth > 245) {
                    String description = VTDScreen.this.textRenderer.trimToWidth(this.description, 245 - VTDScreen.this.textRenderer.getWidth("...")) + "...";
                    VTDScreen.this.textRenderer.drawWithShadow(matrices, description, ((float) (VTDScreen.this.width / 2 - VTDScreen.this.textRenderer.getWidth(description) / 2)), y + 13, 16777215);
                } else {
                    VTDScreen.this.textRenderer.drawWithShadow(matrices, this.description, ((float) (VTDScreen.this.width / 2 - VTDScreen.this.textRenderer.getWidth(this.description) / 2)), y + 13, 16777215);
                }
            }
        }
    }
}
