package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.text.Text;
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

@Mixin(PackScreen.class)
public class PackScreenMixin extends Screen implements PackScreenAccess {
    @Shadow
    @Final
    private Path file;

    @Shadow
    @Final
    private ResourcePackOrganizer organizer;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    /**
     * Add the VT button between the "Open pack folder" and "Done" buttons
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/layout/LinearLayoutWidget;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;",
            ordinal = 3), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addVTDButton(CallbackInfo ci, LinearLayoutWidget headerLayout, LinearLayoutWidget footerLayout) {
        footerLayout.add(ButtonWidget.builder(Constants.RESOURCE_PACK_BUTTON_TEXT, btn -> {
            this.vtdownloader$applyChanges();
            // noinspection ConstantConditions
            this.client.setScreen(new VTDownloadScreen(this, Constants.RESOURCE_PACK_SCREEN_SUBTITLE));
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
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/button/ButtonWidget$Builder;build()Lnet/minecraft/client/gui/widget/button/ButtonWidget;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/layout/HeaderFooterLayoutWidget;addToFooter(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;")))
    private ButtonWidget shrinkVanillaButton(ButtonWidget.Builder builder) {
        return builder.width(ButtonWidget.SMALL_WIDTH).build();
    }

    @Override
    public boolean vtdownloader$isResourcePackScreen() {
        // noinspection ConstantConditions
        return this.file == this.client.getResourcePackDir();
    }

    @Override
    public void vtdownloader$applyChanges() {
        this.organizer.apply();
    }
}
