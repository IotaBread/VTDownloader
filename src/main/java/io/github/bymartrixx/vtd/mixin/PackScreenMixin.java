package io.github.bymartrixx.vtd.mixin;

import io.github.bymartrixx.vtd.gui.VTDScreen;
import io.github.bymartrixx.vtd.gui.screen.VTResourcePackScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(PackScreen.class)
public class PackScreenMixin extends Screen {
    private static final Text VTD_RESOURCE_PACK_SUBTITLE = new TranslatableText("vtd.resourcePack.subtitle").formatted(Formatting.GRAY);
    @Shadow
    @Final
    private File file;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "HEAD"), method = "init")
    private void addVTDButton(CallbackInfo info) {
        // Checks if it is the resource pack screen and not the data pack screen
        if (this.file == this.client.getResourcePackDir()) {
            this.addButton(new ButtonWidget(this.width / 2 - 75, this.height - 24, 150, 20, new TranslatableText("vtd.resourcePack.button"), button -> this.client.openScreen(new VTResourcePackScreen(this, VTD_RESOURCE_PACK_SUBTITLE))));
        }
    }
}
