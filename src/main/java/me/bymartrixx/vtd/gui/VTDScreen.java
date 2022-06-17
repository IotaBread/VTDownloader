package me.bymartrixx.vtd.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.gui.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.ColorUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class VTDScreen extends Screen {
    private static VTDScreen instance;
    public final Map<String, List<String>> selectedPacks; // {"$category":["$pack","$pack"],"$category":["$pack"]}
    protected final Text subtitle;
    private final PackNameTextFieldWidget.TooltipSupplier TOOLTIP_SUPPLIER = (textField, nameValidity, matrices, mouseX, mouseY) -> {
        Text text = null;
        switch (nameValidity) {
            case RESERVED_WINDOWS -> text = Text.translatable("vtd.fileNameValidity.reservedWindows");
            case INVALID_WINDOWS -> text = Text.translatable("vtd.fileNameValidity.invalidWindows");
            case REGEX_DOESNT_MATCH -> text = Text.translatable("vtd.fileNameValidity.regexDoesntMatch", PackNameTextFieldWidget.fileNameRegex);
            case FILE_EXISTS -> text = Text.translatable("vtd.fileNameValidity.fileExists");
        }

        if (text != null) {
            this.renderTooltip(matrices, text, textField.x, textField.y - (textField.getHeight() / 2));
        }
    };
    private final Screen previousScreen;
    private final ArrayList<ButtonWidget> tabButtons = new ArrayList<>();
    private final List<TooltipData> tooltips = new ArrayList<>();
    private ButtonWidget tabLeftButton;
    private ButtonWidget tabRightButton;
    private DownloadButtonWidget downloadButton;
    private PackListWidget listWidget;
    private SelectedPacksListWidget selectedPacksListWidget;
    private PackNameTextFieldWidget packNameField;
    private int tabIndex = 0;
    private int selectedTabIndex = 0;
    /**
     * The download progress. A percentage represented as a float.
     * The value {@code 1.0F} means that the download progress bar should not be rendered.
     * Use {@link #resetDownloadProgress()} to set the progress to {@code 1.0F}.
     */
    private float downloadProgress = -1.0F;

    public VTDScreen(Screen previousScreen, Text subtitle) {
        super(Text.translatable("vtd.title"));
        this.previousScreen = previousScreen;
        this.selectedPacks = new LinkedHashMap<>();
        this.subtitle = subtitle;

        VTDScreen.instance = this;
    }

    public VTDScreen(Screen previousScreen, Text subtitle, Map<String, List<String>> selectedPacks) {
        super(Text.translatable("vtd.title"));
        this.previousScreen = previousScreen;
        this.selectedPacks = selectedPacks;
        this.subtitle = subtitle;

        VTDScreen.instance = this;
    }

    /**
     * Get the number of "tabs" that should be generated/rendered.
     *
     * @param width the width of the screen.
     * @return The max number of "tabs"
     */
    private static int getTabNum(int width) {
        // 110 is the 3 buttons (20 * 3) and the margin between (10 between the buttons, 10 to the right of the right button and 10 * 2 for both sides of the screen)
        // The 130 is the width of the "tab" buttons and the margin (120 + 10)
        // The 1 is to make an "overflow" effect
        return (width - 110) / 130 + 1;
    }

    public static VTDScreen getInstance() {
        return instance;
    }

    private void download(DownloadButtonWidget button) {
        Thread downloadThread = new Thread(() -> {
            try {
                // Reverse this.selectedPacks order
                Map<String, List<String>> selectedPacks2 = new LinkedHashMap<>();
                ArrayList<String> keys = new ArrayList<>(this.selectedPacks.keySet());

                for (int i = keys.size() - 1; i >= 0; --i) {
                    String key = keys.get(i);
                    List<String> values = this.selectedPacks.get(key);

                    List<String> valuesReverse = new ArrayList<>();
                    for (int j = values.size() - 1; j >= 0; --j) {
                        valuesReverse.add(values.get(j));
                    }

                    selectedPacks2.put(key, valuesReverse);
                }

                JsonObject selectedPacks = VTDMod.GSON.toJsonTree(selectedPacks2).getAsJsonObject();
                this.downloadProgress = 0.0F;

                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    // Get the download link
                    HttpPost httpPost = new HttpPost(VTDMod.BASE_URL + "/assets/server/zipresourcepacks.php");

                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("version", VTDMod.MINECRAFT_VERSION));
                    params.add(new BasicNameValuePair("packs", VTDMod.GSON.toJson(selectedPacks)));
                    httpPost.setEntity(new UrlEncodedFormEntity(params));

                    HttpResponse response = client.execute(httpPost);
                    this.downloadProgress = 0.1F;

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
                    this.downloadProgress = 0.35F;

                    String downloadLink = VTDMod.GSON.fromJson(responseBody.toString(), JsonObject.class).get("link").getAsString();
                    String defaultFileName = downloadLink.split("/")[downloadLink.split("/").length - 1];
                    String fileName = !this.packNameField.getText().equals("") ? this.packNameField.getText() + ".zip" : defaultFileName;

                    // Download the resource pack
                    URL url = new URL(VTDMod.BASE_URL + downloadLink);
                    URLConnection connection = url.openConnection();
                    connection.addRequestProperty("User-Agent", "VTDownloader v" + VTDMod.VERSION);
                    connection.setConnectTimeout(500);
                    connection.setConnectTimeout(4000);

                    InputStream in = connection.getInputStream();
                    Files.copy(in, new File(this.client.getResourcePackDir(), fileName.trim()).toPath(), StandardCopyOption.REPLACE_EXISTING);

                    this.downloadProgress = 1.0F;
                }

                button.setSuccess(true);
            } catch (IOException e) {
                VTDMod.logError("Encountered an exception while trying to download the resource pack.", e);
                button.setSuccess(false);
            }
        });

        downloadThread.setName("VT Download");
        downloadThread.start();
    }

    protected void init() {
        this.tabLeftButton = this.addDrawableChild(new ArrowButtonWidget(40, 30, 20, 20, ArrowButtonWidget.ArrowType.LEFT, button -> {
            --this.tabIndex;
            this.updateTabButtons();
        }));
        this.tabRightButton = this.addDrawableChild(new ArrowButtonWidget(70, 30, 20, 20, ArrowButtonWidget.ArrowType.RIGHT, button -> {
            ++this.tabIndex;
            this.updateTabButtons();
        }));

        // Reload button
        this.addDrawableChild(new ArrowButtonWidget(10, 30, 20, 20, ArrowButtonWidget.ArrowType.CLOCKWISE, button -> {
            VTDMod.reloadRPCategories();
            this.client.setScreen(new VTDScreen(this.previousScreen, this.subtitle, this.selectedPacks));
        }));
        // Done button
        this.addDrawableChild(new ButtonWidget(this.width - 90, this.height - 30, 80, 20, Text.translatable("vtd.done"), button -> this.onClose()));

        this.downloadButton = this.addDrawableChild(new DownloadButtonWidget(this.width - 200, this.height - 30, 100, 20, Text.translatable("vtd.download"), Text.translatable("vtd.download.success"), Text.translatable("vtd.download.failure"), button -> this.download((DownloadButtonWidget) button)));

        if (this.packNameField != null) {
            this.packNameField = new PackNameTextFieldWidget(this.textRenderer, 10, this.height - 30, 160, 20, Text.translatable("vtd.resourcePack.nameField"), this.client.getResourcePackDir(), this::updateDownloadButton, TOOLTIP_SUPPLIER, this.packNameField.getText());
        } else {
            this.packNameField = new PackNameTextFieldWidget(this.textRenderer, 10, this.height - 30, 160, 20, Text.translatable("vtd.resourcePack.nameField"), this.client.getResourcePackDir(), this::updateDownloadButton, TOOLTIP_SUPPLIER);
        }
        this.packNameField.setMaxLength(64);
        this.addSelectableChild(this.packNameField);

        boolean exceptionFound = VTDMod.rpCategories.size() == 0;

        if (!exceptionFound) {
            JsonObject category = VTDMod.rpCategories.get(selectedTabIndex).getAsJsonObject();

            this.listWidget = this.addSelectableChild(new PackListWidget(category.get("packs").getAsJsonArray(), category.get("category").getAsString()));
        } else {
            this.listWidget = this.addSelectableChild(new PackListWidget());
        }

        this.selectedPacksListWidget = new SelectedPacksListWidget();
        this.selectedPacksListWidget.setLeftPos(this.width - 170);
        this.addSelectableChild(this.selectedPacksListWidget);

        this.resetDownloadProgress();
        if (!exceptionFound) {
            this.updateTabButtons();
            this.tabButtons.get(0).active = false;
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
        this.packNameField.render(matrices, mouseX, mouseY, delta); // Render pack name text field
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215); // Render title
        drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2, 20, 16777215); // Render subtitle

        // Render tabButtons
        for (ButtonWidget tabButton : this.tabButtons) {
            tabButton.render(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);

        if (this.downloadProgress != -1.0F)
            this.renderDownloadProgressBar(matrices, this.width - 170, this.height - 50, this.width - 10, this.height - 40, 0.9F);

        // Render tooltips; make sure tooltips are always rendered on top of everything else
        for (TooltipData tooltip: this.tooltips) {
            this.renderTooltip(matrices, tooltip.text, tooltip.x, tooltip.y);
        }

        this.tooltips.clear();
    }

    /**
     * A cursed fix to #2. Bedrockify mixins into {@link Screen#renderBackgroundTexture(int)}
     * and "disables" it. In order to "enable" it again, I have copied the method and, because
     * it's a different method on a different class Bedrockify won't affect it with a mixin.
     */
    @Deprecated
    @Override
    public void renderBackgroundTexture(int vOffset) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(0.0, this.height, 0.0).uv(0.0F, (float) this.height / 32.0F + (float) vOffset).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.width, this.height, 0.0).uv((float) this.width / 32.0F, (float) this.height / 32.0F + (float) vOffset).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(this.width, 0.0, 0.0).uv((float) this.width / 32.0F, (float) vOffset).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0F, (float) vOffset).color(64, 64, 64, 255).next();
        tessellator.draw();
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
    @SuppressWarnings("SameParameterValue")
    private void renderDownloadProgressBar(MatrixStack matrices, int x1, int y1, int x2, int y2, float opacity) {
        int progressWidth = MathHelper.ceil((float) (x2 - x1 - 4) * this.downloadProgress);
        int alpha = Math.round(opacity * 255.0F);
        int color = ColorUtil.ARGB32.getArgb(alpha, 255, 255, 255);

        // Draw progress bar outline
        fill(matrices, x1 + 1, y1, x2 - 1, y1 + 1, color); // Top line
        fill(matrices, x1 + 1, y2, x2 - 1, y2 - 1, color); // Bottom line
        fill(matrices, x1, y1, x1 + 1, y2, color); // Left line
        fill(matrices, x2, y1, x2 - 1, y2, color); // Right line

        // Draw progress bar "progress"
        fill(matrices, x1 + 2, y1 + 2, x1 + progressWidth + 2, y2 - 2, color);
    }

    public void tick() {
        this.downloadButton.tick();
    }

    public void onClose() {
        this.client.setScreen(this.previousScreen);
    }

    private void updateTabButtons() {
        this.tabLeftButton.active = this.tabIndex > 0;
        this.tabRightButton.active = this.tabIndex < VTDMod.rpCategories.size() - 1;

        this.updateDownloadButton();

        this.tabButtons.clear();

        // Remove old buttons from this.children
        for (JsonElement category : VTDMod.rpCategories) {
            String categoryName = category.getAsJsonObject().get("category").getAsString();
            this.children().removeIf(element -> element instanceof ButtonWidget button && button.getMessage().getString().equals(categoryName));
        }

        for (int i = 0; i < getTabNum(this.width); ++i) {
            int index = i + this.tabIndex;
            if (index >= VTDMod.rpCategories.size()) break;

            JsonObject category = VTDMod.rpCategories.get(index).getAsJsonObject();
            String categoryName = category.get("category").getAsString();
            ButtonWidget buttonWidget = new ButtonWidget(i * 130 + 100, 30, 120, 20, Text.literal(categoryName), button -> {
                if (this.selectedTabIndex != index) {
                    for (ButtonWidget widget: this.tabButtons) {
                        widget.active = true;
                    }
                    button.active = false;
                    this.selectedTabIndex = index;

                    this.remove(this.listWidget);
                    // Doesn't work as expected :/
//                    this.listWidget.replaceEntries(VTDMod.rpCategories.get(selectedTabIndex).getAsJsonObject().get("packs").getAsJsonArray());

                    JsonObject category2 = VTDMod.rpCategories.get(selectedTabIndex).getAsJsonObject();
                    this.listWidget = this.addSelectableChild(new PackListWidget(category2.get("packs").getAsJsonArray(), category2.get("category").getAsString()));
                }
            });

            this.tabButtons.add(i, buttonWidget);
            this.addSelectableChild(buttonWidget);
        }
    }

//    private static boolean isFileNameValid(String fileName) {
//        if (fileName.equals("")) {
//            return true;
//        }
//
//        // On Windows the max path length is of 260 characters. The path to
//        // the resource packs folder is normally about 50 characters long,
//        // and the path to a data packs folder is about 60 characters long.
//        // That means there are 200 characters left to use for a data/resource
//        // pack file name, but that number may vary greatly.
//        if (fileName.length() > 64) {
//            return false;
//        }
//
//        return true;
//    }

    public void updateDownloadButton() {
        this.downloadButton.active = this.selectedPacks.size() > 0 && this.packNameField.isNameValid();
    }

    public void addSelectedPack(String categoryName, String packName) {
        this.addSelectedPack(categoryName, packName, false);
    }

    public void addSelectedPack(String categoryName, String packName, boolean oneEntry) {
        if (!this.selectedPacks.containsKey(categoryName)) {
            this.selectedPacks.put(categoryName, new ArrayList<>(Collections.singleton(packName)));

            this.updateDownloadButton();
            this.selectedPacksListWidget.updateEntries();
        } else if (!this.isPackSelected(categoryName, packName)) {
            List<String> packs = oneEntry ? new ArrayList<>() : this.selectedPacks.get(categoryName);
            packs.add(packName);
            this.selectedPacks.replace(categoryName, packs);

            this.updateDownloadButton();
            this.selectedPacksListWidget.updateEntries();
        }
    }

    public boolean isPackSelected(String categoryName, String packName) {
        if (!this.selectedPacks.containsKey(categoryName)) return false;

        return this.selectedPacks.get(categoryName).contains(packName);
    }

    public void removeSelectedPack(String categoryName, String packName) {
        if (!isPackSelected(categoryName, packName)) return;

        this.selectedPacks.get(categoryName).remove(packName);

        if (this.selectedPacks.get(categoryName).size() == 0) {
            this.selectedPacks.remove(categoryName);
        }

        this.updateDownloadButton();
        this.selectedPacksListWidget.updateEntries();
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

    public void addTooltip(TooltipData tooltip) {
        this.tooltips.add(tooltip);
    }

    public record TooltipData(List<Text> text, int x, int y) {}
}
