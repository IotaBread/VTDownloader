package io.github.bymartrixx.vtd.gui;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import io.github.bymartrixx.vtd.gui.widget.DownloadButtonWidget;
import io.github.bymartrixx.vtd.gui.widget.PackListWidget;
import io.github.bymartrixx.vtd.gui.widget.SelectedPacksListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VTDScreen extends Screen {
    private static final Gson GSON = new Gson();
    private static VTDScreen instance;
    public final JsonObject selectedPacks; // {"$category":["$pack","$pack"],"$category":["$pack"]}
    private final Screen previousScreen;
    private final ArrayList<ButtonWidget> tabButtons = Lists.newArrayList();
    private ButtonWidget tabLeftButton;
    private ButtonWidget tabRightButton;
    private DownloadButtonWidget downloadButton;
    private PackListWidget listWidget;
    private SelectedPacksListWidget selectedPacksListWidget;
    private int tabIndex = 0;
    private int selectedTabIndex = 0;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
        this.selectedPacks = new JsonObject();

        VTDScreen.instance = this;
    }

    /**
     * Get the number of "tabs" that should be generated/rendered.
     *
     * @param width the width of the screen.
     * @return The max number of "tabs"
     */
    private static int getTabNum(int width) {
        // 80 is the 2 buttons (20 * 2) and the margin between (10 between both buttons, 10 to the right of the right button and 10 * 2 for both sides of the screen)
        // The 130 is the width of the "tab" buttons and the margin (120 + 10)
        // The 1 is to allow the max number of buttons
        return (width - 80) / 130 + 1;
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

    public static VTDScreen getInstance() {
        return instance;
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

        this.listWidget = this.addChild(new PackListWidget(category.get("packs").getAsJsonArray(), category.get("category").getAsString()));
        this.selectedPacksListWidget = this.addChild(new SelectedPacksListWidget());

        this.updateTabButtons();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.listWidget.render(matrices, mouseX, mouseY, delta); // Render pack list
        this.selectedPacksListWidget.render(matrices, mouseX, mouseY, delta); // Render selected packs list
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215); // Render title

        // Render tabButtons
        for (ButtonWidget tabButton : this.tabButtons) {
            tabButton.render(matrices, mouseX, mouseY, delta);
        }

        // TODO: Add download progress bar ( see SplashScreen#renderProgressBar )

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
                    this.listWidget = this.addChild(new PackListWidget(category2.get("packs").getAsJsonArray(), category2.get("category").getAsString()));
                    this.selectedPacksListWidget = this.addChild(new SelectedPacksListWidget());
                }
            });

            this.tabButtons.add(i, buttonWidget);
            this.children.add(buttonWidget);
        }
    }

    public void updateDownloadButton() {
        this.downloadButton.active = this.selectedPacks.size() > 0;
    }

    public void savePacks(PackListWidget packListWidget) {
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

    public MinecraftClient getClient() {
        return this.client;
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
}
