package me.bymartrixx.vtd.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.bymartrixx.vtd.access.PackEntryListWidgetAccess;
import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.list.EntryListWidget;
import net.minecraft.client.gui.widget.list.pack.PackEntryListWidget;
import net.minecraft.client.render.RenderPipelines;
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
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PackEntryListWidget.class)
public abstract class PackEntryListWidgetMixin extends AlwaysSelectedEntryListWidget<PackEntryListWidget.C_rndhezet>
        implements PackEntryListWidgetAccess {
    @Shadow @Final
    private Text title;

    @Shadow @Final
    PackScreen screen;

    public PackEntryListWidgetMixin(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    @Override
    public boolean vtdownloader$isAvailablePackList() {
        // Available packs list uses "pack.available.title" as title
        return this.title.asComponent() instanceof TranslatableComponent c && c.getKey().contains("available");
    }

    @Override
    public int vtdownloader$getItemHeight() {
        return this.field_62109;
    }

    @Override
    public boolean vtdownloader$isResourcePackList() {
        return ((PackScreenAccess) this.screen).vtdownloader$isResourcePackScreen();
    }

    @Override
    public PackScreen vtdownloader$getScreen() {
        return this.screen;
    }

    @Mixin(PackEntryListWidget.PackEntry.class)
    public static abstract class PackEntryMixin extends EntryListWidget.Entry<PackEntryListWidget.C_rndhezet> {
        @Unique
        private static final int PENCIL_TEXTURE_SIZE = 32;
        @Unique
        private static final int PENCIL_SIZE = 16;
        @Unique
        private static final int PENCIL_RIGHT_MARGIN = 4;
        @Unique
        private static final int PENCIL_BOTTOM_MARGIN = 0;

        @Shadow @Final
        private PackEntryListWidget widget;
        @Shadow @Final
        protected MinecraftClient client;
        @Shadow @Final
        private ResourcePackOrganizer.Pack pack;

        @Unique
        private boolean vtdownloader$vtPack;
        @Unique
        private boolean vtdownloader$editable;

        @Unique
        private int vtdownloader$getPencilXOffset() {
            return this.getWidth() - PENCIL_SIZE - PENCIL_RIGHT_MARGIN;
        }

        @Unique
        private int vtdownloader$getPencilYOffset() {
            return this.getHeight() - PENCIL_SIZE - PENCIL_BOTTOM_MARGIN;
        }

        @Inject(at = @At("TAIL"), method = "<init>")
        private void vtdownloader$init(PackEntryListWidget outer, MinecraftClient client, PackEntryListWidget widget, ResourcePackOrganizer.Pack pack, CallbackInfo ci) {
            if (((PackEntryListWidgetAccess) widget).vtdownloader$isResourcePackList()) {
                this.vtdownloader$vtPack = pack.getDescription().getString().contains(Constants.VT_DESCRIPTION_MARKER);
                this.vtdownloader$editable = this.vtdownloader$vtPack && ((PackEntryListWidgetAccess) this.widget).vtdownloader$isAvailablePackList();
            }
        }

        @Inject(at = @At("TAIL"), method = "method_25343")
        private void renderEditButton(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
            if (this.vtdownloader$vtPack) {
                int pencilX = this.getX() + this.vtdownloader$getPencilXOffset();
                int pencilY = this.getY() + this.vtdownloader$getPencilYOffset();

                // Check if mouse is directly over the pencil icon (not just the entry)
                boolean mouseOverPencil = mouseX >= pencilX && mouseX < pencilX + PENCIL_SIZE
                        && mouseY >= pencilY && mouseY < pencilY + PENCIL_SIZE;

                // textures (two columns, two rows) are arranged, left to right, top to bottom, as:
                // regular, grayed out, highlighted
                float u = 0.0F; // regular pencil
                float v = 0.0F;
                if (!this.vtdownloader$editable) {
                    // grayed out pencil
                    v = PENCIL_SIZE;
                } else if (mouseOverPencil) {
                    // highlighted pencil
                    u = PENCIL_SIZE;
                }

                graphics.drawTexture(RenderPipelines.GUI_TEXTURED, Constants.PENCIL_TEXTURE, pencilX, pencilY,
                        u, v, PENCIL_SIZE, PENCIL_SIZE, PENCIL_TEXTURE_SIZE, PENCIL_TEXTURE_SIZE);
            }
        }

        // @version 1.21.10
        @Inject(at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/widget/list/pack/PackEntryListWidget$PackEntry;isSelectable()Z"
        ), method = "mouseClicked")
        private void onMouseClicked(MouseButtonEvent event, boolean bl, CallbackInfoReturnable<Boolean> cir,
                                    @Local(ordinal = 0) double clickedX, @Local(ordinal = 1) double clickedY) {
            if (this.vtdownloader$editable) {
                int pencilX = this.vtdownloader$getPencilXOffset();
                int pencilY = this.vtdownloader$getPencilYOffset();

                if (clickedX >= pencilX && clickedX < pencilX + PENCIL_SIZE
                        && clickedY >= pencilY && clickedY < pencilY + PENCIL_SIZE) {
                    PackScreen screen = ((PackEntryListWidgetAccess) this.widget).vtdownloader$getScreen();
                    ((PackScreenAccess) screen).vtdownloader$applyChanges();
                    this.client.setScreen(new VTDownloadScreen(screen,
                            Constants.RESOURCE_PACK_SCREEN_SUBTITLE, this.pack));
                }
            }
        }
    }
}
