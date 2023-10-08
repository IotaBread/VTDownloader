package me.bymartrixx.vtd.mixin.compat;

import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.text.Text;
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
public abstract class RecursiveResourcesPackScreenMixin extends PackScreen implements PackScreenAccess {
    @Unique
    private static final Text OPEN_FOLDER_TEXT = Text.translatable("pack.openFolder");
    @Unique
    private static final int LIST_WIDTH = 200;
    @Unique
    private static final int LIST_X_OFFSET = 4;
    @Unique
    private static final Text VTD_TEXT = Text.literal("VTDownloader");
    @Unique
    private static final int BUTTON_Y_OFFSET = 48;

    public RecursiveResourcesPackScreenMixin(PackManager packManager, Consumer<PackManager> applier, Path file, Text title) {
        super(packManager, applier, file, title);
    }

    @Shadow(remap = false)
    @Final
    protected MinecraftClient client;

    @Shadow
    protected abstract Optional<ClickableWidget> findButton(Text text);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setFocusUnlocked(Z)V"), method = "init")
    public void vt_downloader$addRecursiveResourcesButton(CallbackInfo ci) {
        this.findButton(OPEN_FOLDER_TEXT).ifPresent(button -> {
            button.setX(this.width / 2 + LIST_X_OFFSET);
            button.setWidth(LIST_WIDTH / 2);
        });
        this.addDrawableSelectableElement(ButtonWidget.builder(VTD_TEXT, button -> this.client.setScreen(new VTDownloadScreen(this, Constants.RESOURCE_PACK_SCREEN_SUBTITLE)))
                .position((this.width + LIST_WIDTH) / 2 + LIST_X_OFFSET, this.height - BUTTON_Y_OFFSET)
                .size(LIST_WIDTH / 2, Util.VTD_BUTTON_HEIGHT)
                .build());
    }

    @Override
    public boolean vtdownloader$isResourcePackScreen() {
        return true;
    }
}
