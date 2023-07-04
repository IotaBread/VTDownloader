package me.bymartrixx.vtd.gui;

import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.access.AbstractPackAccess;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.DownloadPackRequestData;
import me.bymartrixx.vtd.data.Pack;
import me.bymartrixx.vtd.data.RpCategories;
import me.bymartrixx.vtd.data.SharePackRequestData;
import me.bymartrixx.vtd.gui.popup.MessageScreenPopup;
import me.bymartrixx.vtd.gui.popup.ProgressBarScreenPopup;
import me.bymartrixx.vtd.gui.widget.CategorySelectionWidget;
import me.bymartrixx.vtd.gui.widget.DebugButtonWidget;
import me.bymartrixx.vtd.gui.widget.ExpandDrawerButtonWidget;
import me.bymartrixx.vtd.gui.widget.MutableMessageButtonWidget;
import me.bymartrixx.vtd.gui.widget.PackNameTextFieldWidget;
import me.bymartrixx.vtd.gui.widget.PackSelectionHelper;
import me.bymartrixx.vtd.gui.widget.PackSelectionListWidget;
import me.bymartrixx.vtd.gui.widget.ReloadButtonWidget;
import me.bymartrixx.vtd.gui.widget.SelectedPacksListWidget;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class VTDownloadScreen extends Screen {
    // DEBUG
    private static final boolean DOWNLOAD_DISABLED = false;
    private static final boolean DEBUG_BUTTON = false;

    private static final Text TITLE = Text.literal("VTDownloader");
    private static final Text DOWNLOAD_TEXT = Text.translatable("vtd.download");
    private static final Text DOWNLOAD_FAILED_TEXT = Text.translatable("vtd.download.failed");
    private static final Text DOWNLOAD_SUCCESS_TEXT = Text.translatable("vtd.download.success");
    private static final Text SHARE_TEXT = Text.translatable("vtd.share");
    private static final Text SHARE_FAILED_TEXT = Text.translatable("vtd.share.failed");
    private static final Function<Text, Text> SHARE_CODE_TEXT = code -> Text.translatable("vtd.share.code", code);
    private static final Text READ_PACK_DATA_FAILED_TEXT = Text.translatable("vtd.readPackDataFailed");
    private static final Text PACK_NAME_FIELD_TEXT = Text.translatable("vtd.resourcePack.nameField");
    private static final Text PLACEHOLDER_TEXT = Text.literal("Lorem ipsum dolor sit amet");

    private static final int WIDGET_HEIGHT = 20;
    private static final int WIDGET_MARGIN = 10;

    private static final int TITLE_Y = 8;
    private static final int SUBTITLE_Y = 20;
    private static final int CATEGORY_SELECTOR_Y = 32;

    private static final int PACK_SELECTOR_TOP_HEIGHT = 66;
    private static final int PACK_SELECTOR_BOTTOM_HEIGHT = 36;
    private static final int SELECTED_PACKS_WIDTH = 160;
    private static final int SELECTED_PACKS_CENTER_X = SELECTED_PACKS_WIDTH / 2;
    private static final int SELECTED_PACKS_TOP_HEIGHT = PACK_SELECTOR_TOP_HEIGHT + 6;
    private static final int SELECTED_PACKS_BOTTOM_HEIGHT = PACK_SELECTOR_BOTTOM_HEIGHT + 36;
    private static final int SELECTED_PACKS_BUTTON_Y = 40;
    private static final int SHARE_BUTTON_WIDTH = 120;
    private static final int SHARE_BUTTON_CENTER_X = SHARE_BUTTON_WIDTH / 2;
    private static final int DONE_BUTTON_WIDTH = 80;
    private static final int DOWNLOAD_BUTTON_WIDTH = 100;
    private static final int PROGRESS_BAR_HEIGHT = 40;
    private static final int PROGRESS_BAR_WIDTH = 200;
    private static final int PROGRESS_BAR_COLOR = 0xFFFFFF;
    private static final int PACK_NAME_FIELD_WIDTH = 160;

    private static final float PROGRESS_BAR_MAX_TIME = 20.0F;
    private static final float DOWNLOAD_MESSAGE_MAX_TIME = 120.0F;
    private static final float SHARE_MESSAGE_TIME = 200.0F;
    private static final float ERROR_MESSAGE_TIME = 160.0F;
    private static final float DEBUG_MESSAGE_TIME = 200.0F;

    private final Screen parent;
    private final Text subtitle;
    private final List<Category> categories;

    private Category currentCategory;

    private ProgressBarScreenPopup progressBar;
    private MessageScreenPopup sharePopup;
    private MessageScreenPopup errorPopup;
    // DEBUG
    @Nullable
    private MessageScreenPopup debugPopup;

    private CategorySelectionWidget categorySelector;
    private PackSelectionListWidget packSelector;
    private SelectedPacksListWidget selectedPacksList;
    private PackNameTextFieldWidget packNameField;
    private ButtonWidget shareButton;
    private MutableMessageButtonWidget downloadButton;
    private ButtonWidget doneButton;

    @Nullable
    private String packName;
    @Nullable
    private ResourcePackOrganizer.Pack pack;
    @Nullable
    private String defaultPackName;
    private int leftWidth;
    private boolean changed = false;
    private float downloadProgress = -1.0F;
    private float downloadMessageTime;
    @Nullable
    private SharePackRequestData lastShareData;
    @Nullable
    private String lastShareCode;

    private final PackSelectionHelper selectionHelper = new PackSelectionHelper();

    public VTDownloadScreen(Screen parent, Text subtitle) {
        super(TITLE);
        this.parent = parent;
        this.subtitle = subtitle;

        this.categories = VTDMod.rpCategories.getCategories();
        this.currentCategory = this.categories.size() > 0 ? this.categories.get(0) : null;

        this.selectionHelper.addCallback((pack, category, selected) -> {
            this.changed = true;
            this.updateButtons();
        });
    }

    public VTDownloadScreen(Screen parent, Text subtitle, ResourcePackOrganizer.Pack pack) {
        this(parent, subtitle);

        this.packName = pack.getDisplayName().getString().replaceAll("\\.zip$", "");
        this.pack = pack;
        this.defaultPackName = this.packName;
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
                this.client.getResourcePackDir(),
                this.packNameField.isBlank() ? null : this.packNameField.getText());

        download.whenCompleteAsync((success, throwable) -> {
            this.updateButtons();
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

    private void share() {
        SharePackRequestData data = new SharePackRequestData("resourcepacks", VTDMod.VT_VERSION,
                this.selectionHelper.getSelectedPacksPrimitive());
        if (data.equals(this.lastShareData)) {
            this.showSharePopup(this.lastShareCode);
            return;
        }

        VTDMod.executeShare(data).whenCompleteAsync((code, throwable) -> {
            if (throwable != null) {
                VTDMod.LOGGER.error("Failed to get resource pack share code", throwable);
                this.errorPopup.show(ERROR_MESSAGE_TIME, SHARE_FAILED_TEXT.copy()
                        .append("\n").append(throwable.getLocalizedMessage()));
                return;
            }

            this.lastShareData = data;
            this.lastShareCode = code;

            this.showSharePopup(code);
        });
    }

    private void showSharePopup(String code) {
        if (code != null && this.sharePopup != null) {
            String url = VTDMod.BASE_URL + "/share#" + code;
            this.sharePopup.show(SHARE_MESSAGE_TIME, SHARE_CODE_TEXT.apply(Util.urlText(url)));
        }
    }

    private void readResourcePack() {
        // #AbstractPack and its inheritors are private
        if (this.pack != null && this.pack.getClass().isNestmateOf(ResourcePackOrganizer.Pack.class)) {
            ResourcePackProfile profile = ((AbstractPackAccess) this.pack).vtdownloader$getProfile();

            VTDMod.readResourcePackData(profile).whenCompleteAsync((selection, throwable) -> {
                if (throwable != null) {
                    if (this.errorPopup != null) {
                        this.errorPopup.show(ERROR_MESSAGE_TIME, READ_PACK_DATA_FAILED_TEXT.copy()
                                .append("\n").append(throwable.getLocalizedMessage()));
                    }
                    VTDMod.LOGGER.error("Failed to read VanillaTweaks pack data", throwable);
                } else {
                    this.selectionHelper.setSelection(selection);
                    this.selectedPacksList.update();
                    this.packSelector.updateSelection();
                    this.updateButtons();
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
        // Reset left width
        this.leftWidth = this.width;

        // Draw before everything else
        this.packSelector = this.addDrawable(new PackSelectionListWidget(this.client, this, this.width,
                this.height, PACK_SELECTOR_TOP_HEIGHT, this.height - PACK_SELECTOR_BOTTOM_HEIGHT,
                this.currentCategory, this.selectionHelper));
        this.packSelector.updateCategories(this.categories);

        this.selectedPacksList = this.addDrawable(new SelectedPacksListWidget(this, this.client,
                SELECTED_PACKS_WIDTH, SELECTED_PACKS_TOP_HEIGHT,
                this.height - SELECTED_PACKS_BOTTOM_HEIGHT,
                this.width - SELECTED_PACKS_WIDTH, this.selectionHelper));

        // Reload button
        this.addDrawableChild(new ReloadButtonWidget(WIDGET_MARGIN, WIDGET_MARGIN,
                Constants.RESOURCE_PACK_RELOAD_TEXT, button -> this.reloadCategories()));

        if (DEBUG_BUTTON) this.addDrawableChild(new DebugButtonWidget(WIDGET_MARGIN * 2 + ReloadButtonWidget.BUTTON_SIZE,
                WIDGET_MARGIN, PLACEHOLDER_TEXT, button -> {
            if (this.debugPopup != null) this.debugPopup.show(DEBUG_MESSAGE_TIME, PLACEHOLDER_TEXT);
        }));

        this.categorySelector = this.addDrawableChild(new CategorySelectionWidget(this, CATEGORY_SELECTOR_Y));
        this.categorySelector.init(this.categories, this.currentCategory);

        ExpandDrawerButtonWidget expandButton = this.addDrawable(new ExpandDrawerButtonWidget(this.width - ExpandDrawerButtonWidget.TAB_WIDTH,
                SELECTED_PACKS_TOP_HEIGHT + SELECTED_PACKS_BUTTON_Y,
                SELECTED_PACKS_WIDTH, e -> this.toggleSelectedPacksListExtended()));

        // Handle clicks before the pack selector
        this.sharePopup = this.addSelectableChild(new MessageScreenPopup(this.client, this,
                this.width / 2, this.height / 2,
                this.width / 2, (int) (this.height / 1.5), SHARE_TEXT));
        this.errorPopup = this.addSelectableChild(new MessageScreenPopup(this.client, this,
                this.width / 2, this.height / 2,
                this.width / 2, (int) (this.height / 1.5), Constants.ERROR_TEXT));
        if (DEBUG_BUTTON) this.debugPopup = this.addSelectableChild(new MessageScreenPopup(this.client, this,
                this.width / 2, this.height / 2,
                this.width / 2, (int) (this.height / 1.5), PLACEHOLDER_TEXT));

        this.addSelectableChild(expandButton);
        this.addSelectableChild(this.packSelector);
        this.addSelectableChild(this.selectedPacksList);

        this.shareButton = this.addDrawableChild(ButtonWidget.builder(SHARE_TEXT, button -> this.share())
                .position(this.leftWidth + SELECTED_PACKS_CENTER_X - SHARE_BUTTON_CENTER_X,
                        this.height - SELECTED_PACKS_BOTTOM_HEIGHT + WIDGET_MARGIN)
                .size(SHARE_BUTTON_WIDTH, WIDGET_HEIGHT)
                .build());

        // noinspection ConstantConditions
        this.packNameField = this.addDrawableChild(new PackNameTextFieldWidget(this.textRenderer,
                this.width - DONE_BUTTON_WIDTH - WIDGET_MARGIN * 2 - DOWNLOAD_BUTTON_WIDTH - WIDGET_MARGIN - PACK_NAME_FIELD_WIDTH,
                this.height - WIDGET_HEIGHT - WIDGET_MARGIN, PACK_NAME_FIELD_WIDTH,
                WIDGET_HEIGHT, this.getPackName(), PACK_NAME_FIELD_TEXT,
                this.client.getResourcePackDir(), this.defaultPackName));
        this.packNameField.setChangedListener(s -> this.updateButtons());
        this.packName = null; // Pack name should only be used once

        this.downloadButton = this.addDrawableChild(new MutableMessageButtonWidget(
                this.width - DONE_BUTTON_WIDTH - WIDGET_MARGIN * 2 - DOWNLOAD_BUTTON_WIDTH,
                this.height - WIDGET_HEIGHT - WIDGET_MARGIN, DOWNLOAD_BUTTON_WIDTH, WIDGET_HEIGHT, DOWNLOAD_TEXT,
                button -> this.download()));

        this.doneButton = this.addDrawableChild(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen())
                .position(this.width - DONE_BUTTON_WIDTH - WIDGET_MARGIN, this.height - WIDGET_HEIGHT - WIDGET_MARGIN)
                .size(DONE_BUTTON_WIDTH, WIDGET_HEIGHT)
                .build());

        // Render over everything else
        this.progressBar = this.addDrawable(new ProgressBarScreenPopup(this.client, this.width / 2, this.height / 2,
                PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT, PROGRESS_BAR_COLOR));
        this.addDrawable(this.sharePopup);
        this.addDrawable(this.errorPopup);
        if (this.debugPopup != null) this.addDrawable(this.debugPopup);

        this.updateButtons();
        this.readResourcePack();
    }

    private void updateButtons() {
        if (this.shareButton != null) {
            this.shareButton.active = this.selectionHelper.hasSelection();
        }
        if (this.downloadButton != null) {
            this.downloadButton.active = this.selectionHelper.hasSelection() && this.packNameField.canUseName();
        }
    }

    private void toggleSelectedPacksListExtended() {
        boolean extended = this.selectedPacksList.toggleExtended();
        this.leftWidth = extended ? this.width - SELECTED_PACKS_WIDTH : this.width;

        this.categorySelector.updateScreenWidth();
        this.packSelector.updateScreenWidth();

        this.shareButton.visible = extended;
        this.shareButton.setX(this.leftWidth + SELECTED_PACKS_CENTER_X - SHARE_BUTTON_CENTER_X);
    }

    public boolean isCoveredByPopup(int mouseX, int mouseY) {
        return this.progressBar.isMouseOver(mouseX, mouseY)
                || this.sharePopup.isMouseOver(mouseX, mouseY)
                || this.errorPopup.isMouseOver(mouseX, mouseY)
                || (this.debugPopup != null && this.debugPopup.isMouseOver(mouseX, mouseY));
    }

    public int getLeftWidth() {
        return this.leftWidth;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, TITLE_Y, 0xFFFFFF);
        graphics.drawCenteredShadowedText(this.textRenderer, this.subtitle, this.width / 2, SUBTITLE_Y, 0xFFFFFF);

        this.renderDebugInfo(graphics, mouseX, mouseY);
        this.packSelector.renderTooltips(graphics, mouseX, mouseY);
        this.renderPackNameFieldTooltip(graphics, mouseX, mouseY);

        this.updateTime(delta);
    }

    private void renderDebugInfo(GuiGraphics graphics, int mouseX, int mouseY) {
        this.packSelector.renderDebugInfo(graphics, mouseX, mouseY);
        this.categorySelector.renderDebugInfo(graphics);
    }

    private void renderPackNameFieldTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.packNameField.isMouseOver(mouseX, mouseY)) {
            Text text = this.packNameField.getTooltipText();
            if (text != null) {
                graphics.drawTooltip(this.textRenderer, text, mouseX, mouseY);
            }
        }
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
