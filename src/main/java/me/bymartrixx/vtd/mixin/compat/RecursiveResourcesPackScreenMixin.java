package me.bymartrixx.vtd.mixin.compat;

import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(targets = "nl.enjarai.recursiveresources.gui.FolderedResourcePackScreen")
public abstract class RecursiveResourcesPackScreenMixin extends PackSelectionScreen implements PackScreenAccess {
    @Unique
    private static final Component OPEN_FOLDER_TEXT = Component.translatable("pack.openFolder");
    @Unique
    private static final int LIST_WIDTH = 200;
    @Unique
    private static final int LIST_X_OFFSET = 15;
    @Unique
    private static final int BUTTON_MARGIN = 1;
    @Unique
    private static final Component VTD_TEXT = Component.literal("VTDownloader");
    @Unique
    private static final int BUTTON_Y_OFFSET = 48;

    public RecursiveResourcesPackScreenMixin(PackRepository packManager, Consumer<PackRepository> applier, Path file, Component title) {
        super(packManager, applier, file, title);
    }

    @Shadow(remap = false)
    @Final
    protected Minecraft minecraft;

    @Shadow
    protected abstract Optional<AbstractWidget> findButton(Component text);

    @Inject(at = @At(value = "TAIL"), method = "repositionElements")
    public void vt_downloader$addRecursiveResourcesButton(CallbackInfo ci) {
        this.findButton(OPEN_FOLDER_TEXT).ifPresent(button -> {
            button.setX(this.width / 2 + LIST_X_OFFSET + BUTTON_MARGIN);
            button.setWidth(LIST_WIDTH / 2 - BUTTON_MARGIN * 2);
        });
        this.addRenderableWidget(Button.builder(VTD_TEXT, button -> {
            this.vtdownloader$applyChanges();
            this.minecraft.gui.setScreen(new VTDownloadScreen(this, Constants.RESOURCE_PACK_SCREEN_SUBTITLE));
        })
                .pos((this.width + LIST_WIDTH) / 2 + LIST_X_OFFSET + BUTTON_MARGIN, this.height - BUTTON_Y_OFFSET)
                .size(LIST_WIDTH / 2 - BUTTON_MARGIN * 2, Util.VTD_BUTTON_HEIGHT)
                .build());
    }

    @Override
    public boolean vtdownloader$isResourcePackScreen() {
        return true;
    }
}
