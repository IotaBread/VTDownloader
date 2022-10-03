package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import me.bymartrixx.vtd.util.Util;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(PackScreen.class)
public class PackScreenMixin extends Screen implements PackScreenAccess {
    @Shadow
    @Final
    private File file;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "HEAD"), method = "init")
    private void addVTDButton(CallbackInfo info) {
        // Checks if it is the resource pack screen and not the data pack screen
        if (this.vtdownloader$isResourcePackScreen()) {
            this.addDrawableChild(new ButtonWidget(this.width / 2 - Util.VTD_BUTTON_CENTER_X,
                    this.height - Util.VTD_BUTTON_BOTTOM_MARGIN, Util.VTD_BUTTON_WIDTH, Util.VTD_BUTTON_HEIGHT,
                    Constants.RESOURCE_PACK_BUTTON_TEXT, button -> {
                // noinspection ConstantConditions
                this.client.setScreen(new VTDownloadScreen(this, Constants.RESOURCE_PACK_SCREEN_SUBTITLE));
            }));
        }
    }

    @Override
    public boolean vtdownloader$isResourcePackScreen() {
        // noinspection ConstantConditions
        return this.file == this.client.getResourcePackDir();
    }
}
