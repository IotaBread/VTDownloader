package me.bymartrixx.vtd.gui;

import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.access.AbstractPackAccess;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.DownloadPackRequestData;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.data.RpCategories;
import me.bymartrixx.vtd.gui.popup.MessageScreenPopup;
import me.bymartrixx.vtd.gui.popup.ProgressBarScreenPopup;
import me.bymartrixx.vtd.gui.widget.CategorySelectionWidget;
import me.bymartrixx.vtd.gui.widget.ExpandDrawerButtonWidget;
import me.bymartrixx.vtd.gui.widget.MutableMessageButtonWidget;
import me.bymartrixx.vtd.gui.widget.PackNameTextFieldWidget;
import me.bymartrixx.vtd.gui.widget.PackSelectionHelper;
import me.bymartrixx.vtd.gui.widget.PackSelectionListWidget;
import me.bymartrixx.vtd.gui.widget.ReloadButtonWidget;
import me.bymartrixx.vtd.gui.widget.SelectedPacksListWidget;
import me.bymartrixx.vtd.util.Constants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VTDownloadScreen extends Screen {
    // DEBUG
    private static final boolean DOWNLOAD_DISABLED = true;

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
    private static final int PACK_SELECTOR_BOTTOM_HEIGHT = 36;
    private static final int SELECTED_PACKS_WIDTH = 160;
    private static final int SELECTED_PACKS_TOP_HEIGHT = PACK_SELECTOR_TOP_HEIGHT + 6;
    private static final int SELECTED_PACKS_BOTTOM_HEIGHT = PACK_SELECTOR_BOTTOM_HEIGHT + 6;
    private static final int SELECTED_PACKS_BUTTON_Y = 40;
    private static final int PROGRESS_BAR_HEIGHT = 40;
    private static final int PROGRESS_BAR_WIDTH = 200;
    private static final int PROGRESS_BAR_COLOR = 0xE6FFFFFF;
    private static final int PACK_NAME_FIELD_WIDTH = 160;
    private static final int PACK_NAME_FIELD_HEIGHT = 20;
    private static final int PACK_NAME_FIELD_MARGIN = 10;
    private static final float PROGRESS_BAR_MAX_TIME = 20.0F;
    private static final float DOWNLOAD_MESSAGE_MAX_TIME = 120.0F;
    private static final float ERROR_MESSAGE_TIME = 160.0F;

    private final Screen parent;
    private final Text subtitle;
    private final List<Category> categories;

    private Category currentCategory;

    private ProgressBarScreenPopup progressBar;
    private MessageScreenPopup errorPopup;

    private CategorySelectionWidget categorySelector;
    private PackSelectionListWidget packSelector;
    private SelectedPacksListWidget selectedPacksList;
    private PackNameTextFieldWidget packNameField;
    private MutableMessageButtonWidget downloadButton;
    private ButtonWidget doneButton;

    @Nullable
    private String packName;
    @Nullable
    private ResourcePackOrganizer.Pack pack;
    private int leftWidth;
    private boolean changed = false;
    private float downloadProgress = -1.0F;
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
        this.pack = pack;
    }

    private void reloadCategories() {
        VTDMod.loadRpCategories();
        this.updateCategories(VTDMod.rpCategories);
    }

    private void updateCategories(RpCategories data) {
        this.categories.clear();
        this.categories.addAll(data.getCategories());

        this.packSelector.updateCategories(this.categories);
        this.categorySelector.updateCategories(this.categories);

        this.selectionHelper.cleanUpSelection();
        this.selectedPacksList.update();

        this.currentCategory = this.categories.size() > 0 ? this.categories.get(0) : null;
        this.categorySelector.setSelectedCategory(this.currentCategory);
        this.packSelector.setCategory(this.currentCategory);
    }

    @Nullable
    private String getPackName() {
        return this.packNameField != null ? this.packNameField.getText() : this.packName;
    }

    private void download() {
        this.changed = false;
        if (DOWNLOAD_DISABLED) return;

        this.downloadProgress = 0.0F;
        this.progressBar.show(PROGRESS_BAR_MAX_TIME, () -> this.downloadProgress, () -> this.downloadProgress = -1.0F);

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
                this.progressBar.abortWait();
                VTDMod.LOGGER.error("Pack download failed");
                this.downloadButton.setMessage(DOWNLOAD_FAILED_TEXT);
            }
        }).completeOnTimeout(false, Constants.PACK_DOWNLOAD_TIMEOUT, TimeUnit.SECONDS);
    }

    private void readResourcePack() {
        // #AbstractPack and its inheritors are private
        if (this.pack != null && this.pack.getClass().isNestmateOf(ResourcePackOrganizer.Pack.class)) {
            ResourcePackProfile profile = ((AbstractPackAccess) this.pack).vtdownloader$getProfile();

            VTDMod.readResourcePackData(profile).whenCompleteAsync((selection, throwable) -> {
                if (throwable != null) {
                    if (this.errorPopup != null) {
                        this.errorPopup.show(ERROR_MESSAGE_TIME, Text.literal("Failed to read VanillaTweaks pack data:\n")
                                .append(throwable.getLocalizedMessage()));
                    }
                    VTDMod.LOGGER.error("Failed to read VanillaTweaks pack data", throwable);
                } else {
                    this.selectionHelper.setSelection(selection);
                }
            });
        }

        this.pack = null;
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
        ExpandDrawerButtonWidget selectedPacksListButton = this.addSelectableChild(new ExpandDrawerButtonWidget(this.width - 16,
                SELECTED_PACKS_TOP_HEIGHT + SELECTED_PACKS_BUTTON_Y,
                SELECTED_PACKS_WIDTH, e -> this.toggleSelectedPacksListExtended()));
        this.packSelector = this.addDrawableChild(new PackSelectionListWidget(this.client, this, this.width,
                this.height, PACK_SELECTOR_TOP_HEIGHT, this.height - PACK_SELECTOR_BOTTOM_HEIGHT,
                this.currentCategory, this.selectionHelper));
        this.packSelector.updateCategories(this.categories);

        this.selectedPacksList = this.addDrawableChild(new SelectedPacksListWidget(this, this.client,
                SELECTED_PACKS_WIDTH, SELECTED_PACKS_TOP_HEIGHT,
                this.height - SELECTED_PACKS_BOTTOM_HEIGHT,
                this.width - SELECTED_PACKS_WIDTH, this.selectionHelper));

        this.addDrawable(selectedPacksListButton);

        // noinspection ConstantConditions
        this.packNameField = this.addDrawableChild(new PackNameTextFieldWidget(this.textRenderer,
                this.width - DONE_BUTTON_WIDTH - BUTTON_MARGIN * 2 - DOWNLOAD_BUTTON_WIDTH - PACK_NAME_FIELD_MARGIN - PACK_NAME_FIELD_WIDTH,
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

        this.addDrawableChild(new ReloadButtonWidget(BUTTON_MARGIN, BUTTON_MARGIN,
                Constants.RESOURCE_PACK_RELOAD_TEXT, button -> this.reloadCategories()));

        this.doneButton = this.addDrawableChild(new ButtonWidget(
                this.width - DONE_BUTTON_WIDTH - BUTTON_MARGIN, this.height - BUTTON_HEIGHT - BUTTON_MARGIN,
                DONE_BUTTON_WIDTH, BUTTON_HEIGHT,
                ScreenTexts.DONE, button -> this.closeScreen()
        ));

        this.categorySelector = this.addDrawableChild(new CategorySelectionWidget(this, 32));
        this.categorySelector.setCategories(this.categories);
        this.categorySelector.initCategoryButtons();
        this.categorySelector.setSelectedCategory(this.currentCategory);

        this.progressBar = this.addDrawable(new ProgressBarScreenPopup(this.client, this.width / 2, this.height / 2,
                PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT, PROGRESS_BAR_COLOR));
        this.errorPopup = this.addDrawable(new MessageScreenPopup(this.client, this.width / 2, this.height / 2,
                this.width / 2, (int) (this.height / 1.5), Constants.ERROR_TEXT));

        this.readResourcePack();
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

    public boolean isCoveredByPopup(int mouseX, int mouseY) {
        return this.progressBar.isMouseOver(mouseX, mouseY)
                || this.errorPopup.isMouseOver(mouseX, mouseY);
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

        this.renderDebugInfo(matrices, mouseX, mouseY);
        this.packSelector.renderTooltips(matrices, mouseX, mouseY);

        this.updateTime(delta);
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
