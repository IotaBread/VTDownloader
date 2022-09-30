package me.bymartrixx.vtd.gui;

import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.access.AbstractPackAccess;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.DownloadPackRequestData;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.gui.widget.CategorySelectionWidget;
import me.bymartrixx.vtd.gui.widget.MutableMessageButtonWidget;
import me.bymartrixx.vtd.gui.widget.PackNameTextFieldWidget;
import me.bymartrixx.vtd.gui.widget.PackSelectionHelper;
import me.bymartrixx.vtd.gui.widget.PackSelectionListWidget;
import me.bymartrixx.vtd.gui.widget.SelectedPacksListWidget;
import me.bymartrixx.vtd.util.Constants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VTDownloadScreen extends Screen {
    // DEBUG
    private static final boolean DOWNLOAD_DISABLED = false;

    private static final Text TITLE = Text.literal("VTDownloader");
    private static final Text DOWNLOAD_TEXT = Text.translatable("vtd.download");
    private static final Text DOWNLOAD_FAILED_TEXT = Text.translatable("vtd.download.failed");
    private static final Text DOWNLOAD_SUCCESS_TEXT = Text.translatable("vtd.download.success");
    private static final Text PACK_NAME_FIELD_TEXT = Text.translatable("vtd.resourcePack.nameField");

    private static final int MAX_NAME_LENGTH = 64;

    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_WIDTH = 80;
    private static final int DOWNLOAD_BUTTON_WIDTH = 100;
    private static final int BUTTON_MARGIN = 10;

    private static final int PACK_SELECTOR_TOP_HEIGHT = 66;
    private static final int PACK_SELECTOR_BOTTOM_HEIGHT = 32;
    private static final int SELECTED_PACKS_WIDTH = 160;
    private static final int SELECTED_PACKS_TOP_HEIGHT = PACK_SELECTOR_TOP_HEIGHT + 20;
    private static final int SELECTED_PACKS_BOTTOM_HEIGHT = PACK_SELECTOR_BOTTOM_HEIGHT + 20;
    private static final int PROGRESS_BAR_HEIGHT = 10;
    private static final int PROGRESS_BAR_WIDTH = 160;
    private static final int PROGRESS_BAR_OUTLINE_SIZE = 1;
    private static final int PROGRESS_BAR_COLOR = 0xE6FFFFFF;
    private static final int PROGRESS_BAR_MARGIN = 10;
    private static final int PACK_NAME_FIELD_WIDTH = 160;
    private static final int PACK_NAME_FIELD_HEIGHT = 20;
    private static final int PACK_NAME_FIELD_MARGIN = 10;
    private static final float PROGRESS_BAR_MAX_TIME = 40.0F;
    private static final float DOWNLOAD_MESSAGE_MAX_TIME = 120.0F;

    private final Screen parent;
    private final Text subtitle;
    private final List<Category> categories;

    private Category currentCategory;

    private CategorySelectionWidget categorySelector;
    private PackSelectionListWidget packSelector;
    private SelectedPacksListWidget selectedPacksList;
    private PackNameTextFieldWidget packNameField;
    private MutableMessageButtonWidget downloadButton;
    private ButtonWidget doneButton;

    @Nullable
    private String packName;
    private int leftWidth;
    private boolean changed = false;
    private float downloadProgress = -1.0F;
    private float progressBarTime = 0.0F;
    private float downloadMessageTime;

    private final PackSelectionHelper selectionHelper = new PackSelectionHelper();

    public VTDownloadScreen(Screen parent, Text subtitle) {
        super(TITLE);
        this.parent = parent;
        this.subtitle = subtitle;

        this.categories = VTDMod.rpCategories.getCategories();
        this.currentCategory = this.categories.size() > 0 ? this.categories.get(0) : null;

        this.selectionHelper.addCallback((pack, category, selected) -> {
            this.changed = true;
            this.updateDownloadButtonActive();
        });
    }

    public VTDownloadScreen(Screen parent, Text subtitle, ResourcePackOrganizer.Pack pack) {
        this(parent, subtitle);

        this.packName = pack.getDisplayName().getString().replaceAll("\\.zip$", "");
        if (pack.getClass().isNestmateOf(ResourcePackOrganizer.Pack.class)) { // #AbstractPack and its inheritors are private
            ResourcePackProfile profile = ((AbstractPackAccess) pack).vtdownloader$getProfile();
            List<String> selection;
            try (ResourcePack resourcePack = profile.createResourcePack();
                 InputStream stream = resourcePack.openRoot(Constants.SELECTED_PACKS_FILE)) {
                if (stream != null) {
                    selection = VTDMod.readSelectedPacks(new BufferedReader(new InputStreamReader(stream)));
                } else {
                    selection = Collections.emptyList();
                }
            } catch (Exception e) {
                // TODO: Show error message
                VTDMod.LOGGER.error("Failed to read VanillaTweaks pack data", e);
                return;
            }

            this.selectionHelper.setSelection(selection);
        }
    }

    @Nullable
    private String getPackName() {
        return this.packNameField != null ? this.packNameField.getText() : this.packName;
    }

    private void download() {
        this.changed = false;
        if (DOWNLOAD_DISABLED) return;

        this.downloadProgress = 0.0F;

        // Disable download and done button to keep the download running within the screen
        this.downloadButton.active = false;
        this.doneButton.active = false;
        this.packNameField.setEditable(false);
        this.packSelector.setEditable(false);

        DownloadPackRequestData data = DownloadPackRequestData.create(this.selectionHelper.getSelectedPacks());

        // noinspection ConstantConditions
        CompletableFuture<Boolean> download = VTDMod.executePackDownload(data, f -> this.downloadProgress = f,
                this.client.getResourcePackDir().toPath(),
                this.packNameField.isBlank() ? null : this.packNameField.getText());

        download.whenCompleteAsync((success, throwable) -> {
            this.updateDownloadButtonActive();
            this.doneButton.active = true;
            this.packNameField.setEditable(true);
            this.packSelector.setEditable(true);

            if (throwable != null) {
                VTDMod.LOGGER.error("Pack download failed", throwable);
                return;
            }

            if (success) {
                this.downloadProgress = 1.0F;
                VTDMod.LOGGER.info("Pack downloaded successfully");
                this.downloadButton.setMessage(DOWNLOAD_SUCCESS_TEXT);
            } else {
                VTDMod.LOGGER.error("Pack download failed");
                this.downloadButton.setMessage(DOWNLOAD_FAILED_TEXT);
            }
        });
    }

    public boolean selectCategory(Category category) {
        if (this.currentCategory != category) {
            this.currentCategory = category;
            this.categorySelector.setSelectedCategory(category);
            this.packSelector.setCategory(category);
            return true;
        }

        return false;
    }

    public void goToPack(Pack pack, Category category) {
        this.selectCategory(category);

        this.packSelector.focusPack(pack);
    }

    @SuppressWarnings("ConstantConditions") // client is marked as nullable
    @Override
    public void closeScreen() {
        if (this.changed && this.selectionHelper.hasSelection()) {
            this.client.setScreen(new UnsavedPackWarningScreen(this, this.parent));
        } else {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    protected void init() {
        this.leftWidth = this.width;
        this.packSelector = this.addDrawableChild(new PackSelectionListWidget(this.client, this, this.width,
                this.height, PACK_SELECTOR_TOP_HEIGHT, this.height - PACK_SELECTOR_BOTTOM_HEIGHT,
                this.currentCategory, this.selectionHelper));
        this.packSelector.updateCategories(this.categories);

        this.selectedPacksList = this.addDrawableChild(new SelectedPacksListWidget(this, this.client,
                SELECTED_PACKS_WIDTH, SELECTED_PACKS_TOP_HEIGHT,
                this.height - SELECTED_PACKS_BOTTOM_HEIGHT,
                this.width - SELECTED_PACKS_WIDTH, this.selectionHelper));

        // TODO: Implement better extend button
        this.addDrawableChild(new ButtonWidget(
                this.width - DONE_BUTTON_WIDTH - BUTTON_MARGIN * 3 - DOWNLOAD_BUTTON_WIDTH - 40,
                this.height - BUTTON_HEIGHT - BUTTON_MARGIN, 40, 20, Text.literal("Ext"),
                button -> this.toggleSelectedPacksListExtended()
        ));

        // noinspection ConstantConditions
        this.packNameField = this.addDrawableChild(new PackNameTextFieldWidget(this.textRenderer,
                this.width - DONE_BUTTON_WIDTH - BUTTON_MARGIN * 3 - DOWNLOAD_BUTTON_WIDTH - 40 - PACK_NAME_FIELD_MARGIN - PACK_NAME_FIELD_WIDTH,
                this.height - PACK_NAME_FIELD_HEIGHT - PACK_NAME_FIELD_MARGIN, PACK_NAME_FIELD_WIDTH,
                PACK_NAME_FIELD_HEIGHT, this.getPackName(), PACK_NAME_FIELD_TEXT,
                this.client.getResourcePackDir().toPath()));
        this.packNameField.setMaxLength(MAX_NAME_LENGTH);
        this.packNameField.setChangedListener(s -> this.updateDownloadButtonActive());
        this.packName = null; // Pack name should only be used once

        this.downloadButton = this.addDrawableChild(new MutableMessageButtonWidget(
                this.width - DONE_BUTTON_WIDTH - BUTTON_MARGIN * 2 - DOWNLOAD_BUTTON_WIDTH,
                this.height - BUTTON_HEIGHT - BUTTON_MARGIN, DOWNLOAD_BUTTON_WIDTH, BUTTON_HEIGHT, DOWNLOAD_TEXT,
                button -> this.download()));
        this.updateDownloadButtonActive();

        this.doneButton = this.addDrawableChild(new ButtonWidget(
                this.width - DONE_BUTTON_WIDTH - BUTTON_MARGIN, this.height - BUTTON_HEIGHT - BUTTON_MARGIN,
                DONE_BUTTON_WIDTH, BUTTON_HEIGHT,
                ScreenTexts.DONE, button -> this.closeScreen()
        ));

        this.categorySelector = this.addDrawableChild(new CategorySelectionWidget(this, 32));
        this.categorySelector.setCategories(this.categories);
        this.categorySelector.initCategoryButtons();
        this.categorySelector.setSelectedCategory(this.currentCategory);
    }

    private void updateDownloadButtonActive() {
        if (this.downloadButton != null) {
            this.downloadButton.active = this.selectionHelper.hasSelection() && this.packNameField.canUseName();
        }
    }

    private void toggleSelectedPacksListExtended() {
        boolean extended = this.selectedPacksList.toggleExtended();
        this.leftWidth = extended ? this.width - SELECTED_PACKS_WIDTH : this.width;

        this.categorySelector.updateScreenWidth();
        this.packSelector.updateScreenWidth();
    }

    public int getLeftWidth() {
        return this.leftWidth;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2, 20, 0xFFFFFF);
        this.renderDownloadProgressBar(matrices, delta);

        this.renderDebugInfo(matrices, mouseX, mouseY);
        this.packSelector.renderTooltips(matrices, mouseX, mouseY);

        this.updateTime(delta);
    }

    private void renderDownloadProgressBar(MatrixStack matrices, float delta) {
        // TODO: Fix collision with pack name field
        if (this.downloadProgress == -1.0F) {
            return;
        } else if (this.downloadProgress >= 1.0F) {
            if (this.progressBarTime >= PROGRESS_BAR_MAX_TIME) {
                this.progressBarTime = 0.0F;
                this.downloadProgress = -1.0F;
                return;
            } else {
                this.progressBarTime += delta;
            }
        }

        int outline = PROGRESS_BAR_OUTLINE_SIZE;
        int progressWidth = Math.round((PROGRESS_BAR_WIDTH - outline * 4) * this.downloadProgress);
        int x1 = PROGRESS_BAR_MARGIN;
        int y1 = this.height - PROGRESS_BAR_HEIGHT - PROGRESS_BAR_MARGIN;
        int x2 = x1 + PROGRESS_BAR_WIDTH;
        int y2 = y1 + PROGRESS_BAR_HEIGHT;

        // Outline
        fill(matrices, x1 + outline, y1, x2 - outline, y1 + outline, PROGRESS_BAR_COLOR); // Top line
        fill(matrices, x1 + outline, y2 - outline, x2 - outline, y2, PROGRESS_BAR_COLOR); // Bottom line
        fill(matrices, x1, y1, x1 + outline, y2, PROGRESS_BAR_COLOR); // Left line
        fill(matrices, x2 - outline, y1, x2, y2, PROGRESS_BAR_COLOR); // Right line

        // Progress line
        fill(matrices, x1 + outline * 2, y1 + outline * 2,
                x1 + outline * 2 + progressWidth, y2 - outline * 2, PROGRESS_BAR_COLOR);
    }

    private void renderDebugInfo(MatrixStack matrices, int mouseX, int mouseY) {
        this.packSelector.renderDebugInfo(matrices, mouseX, mouseY);
        this.categorySelector.renderDebugInfo(matrices);
    }

    private void updateTime(float delta) {
        if (this.downloadMessageTime >= DOWNLOAD_MESSAGE_MAX_TIME) {
            this.downloadMessageTime = 0.0F;
            this.downloadButton.resetMessage();
        } else {
            this.downloadMessageTime += delta;
        }
    }
}
