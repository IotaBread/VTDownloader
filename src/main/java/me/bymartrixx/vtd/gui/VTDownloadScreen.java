package me.bymartrixx.vtd.gui;

import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.DownloadPackRequestData;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.gui.widget.CategorySelectionWidget;
import me.bymartrixx.vtd.gui.widget.PackSelectionHelper;
import me.bymartrixx.vtd.gui.widget.PackSelectionListWidget;
import me.bymartrixx.vtd.gui.widget.SelectedPacksListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VTDownloadScreen extends Screen {
    private static final Text TITLE = Text.literal("VTDownloader");
    private static final Text DOWNLOAD_TEXT = Text.translatable("vtd.download");

    private static final boolean DOWNLOAD_DISABLED = false;
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
    private static final float PROGRESS_BAR_MAX_TIME = 40.0F;

    private final Screen parent;
    private final Text subtitle;
    private final List<Category> categories;

    private Category currentCategory;

    private CategorySelectionWidget categorySelector;
    private PackSelectionListWidget packSelector;
    private SelectedPacksListWidget selectedPacksList;
    private ButtonWidget downloadButton;
    private ButtonWidget doneButton;

    private int leftWidth;
    private boolean changed = false;
    private float downloadProgress = -1.0F;
    private float progressBarTime = 0.0F;

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

    private void download() {
        this.changed = false;
        if (DOWNLOAD_DISABLED) return;

        this.downloadProgress = 0.0F;

        // Disable download and done button to keep the download running within the screen
        this.downloadButton.active = false;
        this.doneButton.active = false;

        DownloadPackRequestData data = DownloadPackRequestData.create(this.selectionHelper.getSelectedPacks());

        // noinspection ConstantConditions
        CompletableFuture<Boolean> download = VTDMod.executePackDownload(data, f -> this.downloadProgress = f,
                this.client.getResourcePackDir().toPath(), null /* TODO userFileName */);

        download.whenCompleteAsync((success, throwable) -> {
            this.updateDownloadButtonActive();
            this.doneButton.active = true;

            if (throwable != null) {
                VTDMod.LOGGER.error("Pack download failed", throwable);
                return;
            }

            if (success) {
                this.downloadProgress = 1.0F;
                VTDMod.LOGGER.info("Pack downloaded successfully");
            } else {
                VTDMod.LOGGER.error("Pack download failed");
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

        this.downloadButton = this.addDrawableChild(new ButtonWidget(
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
            this.downloadButton.active = this.selectionHelper.hasSelection();
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

        this.packSelector.renderTooltips(matrices, mouseX, mouseY);
    }

    private void renderDownloadProgressBar(MatrixStack matrices, float delta) {
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
}
