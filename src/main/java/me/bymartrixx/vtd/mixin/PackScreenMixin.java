package me.bymartrixx.vtd.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;

@Mixin(PackSelectionScreen.class)
public class PackScreenMixin extends Screen implements PackScreenAccess {
    @Shadow
    @Final
    private Path packDir;

    @Shadow
    @Final
    private PackSelectionModel model;

    protected PackScreenMixin(Component title) {
        super(title);
    }

    /**
     * Add the VT button between the "Open pack folder" and "Done" buttons
     */
    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
            ordinal = 4)) // last invoke
    private void addVTDButton(CallbackInfo ci, @Local(ordinal = 1) LinearLayout footerLayout) {
        //noinspection ConstantValue
        if (!this.vtdownloader$isResourcePackScreen() || (Class<?>) this.getClass() != PackSelectionScreen.class) {
            return;
        }

        footerLayout.addChild(Button.builder(Constants.RESOURCE_PACK_BUTTON_TEXT, btn -> {
            this.vtdownloader$applyChanges();
            // noinspection ConstantConditions
            this.minecraft.setScreen(new VTDownloadScreen(this, Constants.RESOURCE_PACK_SCREEN_SUBTITLE));
        }).size(Util.VTD_BUTTON_WIDTH, Util.VTD_BUTTON_HEIGHT).build());
    }

    /**
     * Reduce the size of the vanilla buttons to make room for the vtd button
     */
    /* At a bytecode level, this would be enough
     *  ...
     * +LDC [width]
     * +INVOKEVIRTUAL L../ButtonWidget$Builder;width(I)L../ButtonWidget$Builder;
     * INVOKEVIRTUAL L../ButtonWidget$Builder;build()L../ButtonWidget;
     */
    @Redirect(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToFooter(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;")))
    private Button shrinkVanillaButton(Button.Builder builder) {
        //noinspection ConstantValue
        if (!this.vtdownloader$isResourcePackScreen() || (Class<?>) this.getClass() != PackSelectionScreen.class) {
            return builder.build();
        }

        return builder.width(Button.SMALL_WIDTH).build();
    }

    @Override
    public boolean vtdownloader$isResourcePackScreen() {
        // noinspection ConstantConditions
        return this.packDir == this.minecraft.getResourcePackDirectory();
    }

    @Override
    public void vtdownloader$applyChanges() {
        this.model.commit();
    }
}
