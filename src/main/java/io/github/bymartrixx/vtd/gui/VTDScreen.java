package io.github.bymartrixx.vtd.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bymartrixx.vtd.VTDMod;
import io.github.bymartrixx.vtd.gui.widget.DownloadButtonWidget;
import io.github.bymartrixx.vtd.gui.widget.PackListWidget;
import io.github.bymartrixx.vtd.gui.widget.SelectedPacksListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
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
import java.util.*;

public class VTDScreen extends Screen {
    private static VTDScreen instance;
    public final Map<String, List<String>> selectedPacks; // {"$category":["$pack","$pack"],"$category":["$pack"]}
    private final Screen previousScreen;
    private final ArrayList<ButtonWidget> tabButtons = Lists.newArrayList();
    private ButtonWidget tabLeftButton;
    private ButtonWidget tabRightButton;
    private DownloadButtonWidget downloadButton;
    private PackListWidget listWidget;
    private SelectedPacksListWidget selectedPacksListWidget;
    private int tabIndex = 0;
    private int selectedTabIndex = 0;
    /**
     * The download progress. A percentage represented as a float.
     * The value {@code 1.0F} means that the download progress bar should not be rendered.
     * Use {@link #resetDownloadProgress()} to set the progress to {@code 1.0F}.
     */
    private float downloadProgress = -1.0F;

    public VTDScreen(Screen previousScreen) {
        super(new LiteralText("VTDownloader"));
        this.previousScreen = previousScreen;
        this.selectedPacks = new LinkedHashMap<>();

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

    private static void download(VTDScreen screen) throws IOException {
        JsonObject selectedPacks = VTDMod.GSON.toJsonTree(screen.selectedPacks).getAsJsonObject();
        MinecraftClient minecraftClient = screen.client;
        screen.downloadProgress = 0.0F;

        // Get the download link
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://vanillatweaks.net/assets/server/zipresourcepacks.php");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("version", "1.16"));
            params.add(new BasicNameValuePair("packs", VTDMod.GSON.toJson(selectedPacks)));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = client.execute(httpPost);
            screen.downloadProgress = 0.1F;

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
            screen.downloadProgress = 0.35F;

            String downloadLink = VTDMod.GSON.fromJson(responseBody.toString(), JsonObject.class).get("link").getAsString();
            String fileName = downloadLink.split("/")[downloadLink.split("/").length - 1];

            // Download the resource pack
            FileUtils.copyURLToFile(new URL("https://vanillatweaks.net" + downloadLink), new File(minecraftClient.getResourcePackDir(), fileName), 500, 4000);
            screen.downloadProgress = 1.0F;
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
            try {
                download(this);
                this.downloadButton.setSuccess(true);
            } catch (IOException e) {
                VTDMod.logError("Encountered an exception while trying to download the resource pack.", e);
                this.downloadButton.setSuccess(false);
            }
        }));

        boolean exceptionFound = VTDMod.categories == null || VTDMod.categories.size() == 0;

        if (!exceptionFound) {
            JsonObject category = VTDMod.categories.get(selectedTabIndex).getAsJsonObject();

            this.listWidget = this.addChild(new PackListWidget(category.get("packs").getAsJsonArray(), category.get("category").getAsString()));
        } else {
            this.listWidget = this.addChild(new PackListWidget());
        }

        this.selectedPacksListWidget = new SelectedPacksListWidget();
        this.selectedPacksListWidget.setLeftPos(this.width - 170);
        this.addChild(this.selectedPacksListWidget);

        this.resetDownloadProgress();
        if (!exceptionFound) {
            this.updateTabButtons();
        } else {
            this.tabLeftButton.active = false;
            this.tabRightButton.active = false;

            this.downloadButton.active = false;
        }
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

        super.render(matrices, mouseX, mouseY, delta);

        if (this.downloadProgress != -1.0F)
        this.renderDownloadProgressBar(matrices, 10, this.height - 25, 110, this.height - 15, 0.9F);
    }

    /**
     * Renders the download progress bar.
     * The width of it will be {@code x2 - x1}, and the height will be {@code y2 - y1}.
     * The progress of the bar will be the value of {@link #downloadProgress}
     *
     * @param matrices I don't actually know what this is for lol.
     * @param x1       the bar top left corner/start x position.
     * @param y1       the bar top left corner/start y position.
     * @param x2       the bar bottom right corner/end x position.
     * @param y2       the bar bottom right corner/end y position.
     * @param opacity  the opacity of the progress bar
     */
    private void renderDownloadProgressBar(MatrixStack matrices, int x1, int y1, int x2, int y2, float opacity) {
        int progressWidth = MathHelper.ceil((float) (x2 - x1 - 2) * this.downloadProgress);
        int alpha = Math.round(opacity * 255.0F);
        int color = BackgroundHelper.ColorMixer.getArgb(alpha, 255, 255, 255);

        // Draw progress bar outline
        fill(matrices, x1 + 1, y1, x2 - 1, y1 + 1, color); // Top line
        fill(matrices, x1 + 1, y2, x2 - 1, y2 - 1, color); // Bottom line
        fill(matrices, x1, y1, x1 + 1, y2, color); // Left line
        fill(matrices, x2, y1, x2 - 1, y2, color); // Right line

        // Draw progress bar "progress"
        fill(matrices, x1 + 2, y1 + 2, x1 + progressWidth, y2 - 2, color);
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

                    JsonObject category2 = VTDMod.categories.get(selectedTabIndex).getAsJsonObject();
                    this.listWidget = this.addChild(new PackListWidget(category2.get("packs").getAsJsonArray(), category2.get("category").getAsString()));
                }
            });

            this.tabButtons.add(i, buttonWidget);
            this.children.add(buttonWidget);
        }
    }

    public void updateDownloadButton() {
        this.downloadButton.active = this.selectedPacks.size() > 0;
    }

    public void addSelectedPack(String categoryName, String packName) {
        this.addSelectedPack(categoryName, packName, false);
    }

    public void addSelectedPack(String categoryName, String packName, boolean oneEntry) {
        if (!this.selectedPacks.containsKey(categoryName)) {
            this.selectedPacks.put(categoryName, new ArrayList<>(Collections.singleton(packName)));
        } else if (!this.isPackSelected(categoryName, packName)) {
            List<String> packs = oneEntry ? new ArrayList<>() : this.selectedPacks.get(categoryName);
            packs.add(packName);
            this.selectedPacks.replace(categoryName, packs);
        }
    }

    public boolean isPackSelected(String categoryName, String packName) {
        if (!this.selectedPacks.containsKey(categoryName)) return false;

        return this.selectedPacks.get(categoryName).contains(packName);
    }

    public void removeSelectedPack(String categoryName, String packName) {
        if (!isPackSelected(categoryName, packName)) return;

        this.selectedPacks.get(categoryName).remove(packName);
    }

    public MinecraftClient getClient() {
        return this.client;
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    /**
     * Sets {@link #downloadProgress} back to the default.
     */
    public void resetDownloadProgress() {
        this.downloadProgress = -1.0F;
    }

    public String getSelectedCategory() {
        return this.listWidget.categoryName;
    }

    public void updateListWidgetSelectedEntries() {
        this.listWidget.updateSelectedEntries();
    }
}
