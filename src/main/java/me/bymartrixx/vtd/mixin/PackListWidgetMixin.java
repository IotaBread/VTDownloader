package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.PackListWidgetAccess;
import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.text.component.TranslatableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PackListWidget.class)
public abstract class PackListWidgetMixin extends AlwaysSelectedEntryListWidget<PackListWidget.ResourcePackEntry>
        implements PackListWidgetAccess {
    @Shadow @Final
    private Text title;

    @Shadow @Final
    PackScreen screen;

    private PackListWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    public boolean vtdownloader$isAvailablePackList() {
        // Available packs list uses "pack.available.title" as title
        return this.title.asComponent() instanceof TranslatableComponent c && c.getKey().contains("available");
    }

    @Override
    public int vtdownloader$getItemHeight() {
        return this.itemHeight;
    }

    @Override
    public boolean vtdownloader$isResourcePackList() {
        return ((PackScreenAccess) this.screen).vtdownloader$isResourcePackScreen();
    }

    @Override
    public PackScreen vtdownloader$getScreen() {
        return this.screen;
    }

    @Mixin(PackListWidget.ResourcePackEntry.class)
    public static abstract class ResourcePackEntryMixin {
        private static final int PENCIL_TEXTURE_SIZE = 32;
        private static final int PENCIL_SIZE = 16;
        private static final int PENCIL_RIGHT_MARGIN = 9;
        private static final int PENCIL_BOTTOM_MARGIN = 1;

        @Shadow @Final
        private PackListWidget widget;
        @Shadow @Final
        protected MinecraftClient client;
        @Shadow @Final
        private ResourcePackOrganizer.Pack pack;

        @Unique
        private boolean vtdownloader$vtPack;
        @Unique
        private boolean vtdownloader$editable;

        @Inject(at = @At("TAIL"), method = "<init>")
        private void vtdownloader$init(MinecraftClient client, PackListWidget widget, ResourcePackOrganizer.Pack pack, CallbackInfo ci) {
            if (((PackListWidgetAccess) widget).vtdownloader$isResourcePackList()) {
                this.vtdownloader$vtPack = pack.getDescription().getString().contains(Constants.VT_DESCRIPTION_MARKER);
                this.vtdownloader$editable = this.vtdownloader$vtPack && ((PackListWidgetAccess) this.widget).vtdownloader$isAvailablePackList();
            }
        }

        @Inject(at = @At("TAIL"), method = "render")
        private void renderEditButton(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                                      int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
            if (this.vtdownloader$vtPack) {
                graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                int pencilX = x + entryWidth - PENCIL_SIZE - PENCIL_RIGHT_MARGIN;
                int pencilY = y + entryHeight - PENCIL_SIZE - PENCIL_BOTTOM_MARGIN;
                float u = 0.0F;
                float v = 0.0F;
                if (!this.vtdownloader$editable) {
                    v = PENCIL_SIZE;
                } else if (mouseX >= pencilX && mouseX < pencilX + PENCIL_SIZE
                        && mouseY >= pencilY && mouseY < pencilY + PENCIL_SIZE) {
                    u = PENCIL_SIZE;
                }

                graphics.drawTexture(Constants.PENCIL_TEXTURE, pencilX, pencilY,
                        u, v, PENCIL_SIZE, PENCIL_SIZE, PENCIL_TEXTURE_SIZE, PENCIL_TEXTURE_SIZE);
            }
        }

        // @version 1.19.2
        @SuppressWarnings("InvalidInjectorMethodSignature") // Plugin gives invalid params
        @Inject(at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/screen/pack/PackListWidget$ResourcePackEntry;isSelectable()Z"
        ), method = "mouseClicked", locals = LocalCapture.CAPTURE_FAILHARD)
        private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir,
                                    double clickedX, double clickedY) {
            if (this.vtdownloader$editable) {
                double pencilX = this.widget.getRowWidth() - PENCIL_SIZE - PENCIL_RIGHT_MARGIN;
                double pencilY = ((PackListWidgetAccess) this.widget).vtdownloader$getItemHeight() - 4 - PENCIL_SIZE - PENCIL_BOTTOM_MARGIN;

                if (clickedX >= pencilX && clickedX < pencilX + PENCIL_SIZE
                        && clickedY >= pencilY && clickedY < pencilY + PENCIL_SIZE) {
                    this.client.setScreen(new VTDownloadScreen(((PackListWidgetAccess) this.widget).vtdownloader$getScreen(),
                            Constants.RESOURCE_PACK_SCREEN_SUBTITLE, this.pack));
                }
            }
        }
    }
}
