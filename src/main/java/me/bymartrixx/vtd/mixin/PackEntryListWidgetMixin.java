package me.bymartrixx.vtd.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.bymartrixx.vtd.access.PackEntryListWidgetAccess;
import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TransferableSelectionList.class)
public abstract class PackEntryListWidgetMixin extends ObjectSelectionList<TransferableSelectionList.Entry>
        implements PackEntryListWidgetAccess {
    @Shadow @Final
    private Component title;

    @Shadow @Final
    PackSelectionScreen screen;

    public PackEntryListWidgetMixin(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    @Override
    public boolean vtdownloader$isAvailablePackList() {
        // Available packs list uses "pack.available.title" as title
        return this.title.getContents() instanceof TranslatableContents c && c.getKey().contains("available");
    }

    @Override
    public int vtdownloader$getItemHeight() {
        return this.defaultEntryHeight;
    }

    @Override
    public boolean vtdownloader$isResourcePackList() {
        return ((PackScreenAccess) this.screen).vtdownloader$isResourcePackScreen();
    }

    @Override
    public PackSelectionScreen vtdownloader$getScreen() {
        return this.screen;
    }

    @Mixin(TransferableSelectionList.PackEntry.class)
    public static abstract class PackEntryMixin extends AbstractSelectionList.Entry<TransferableSelectionList.Entry> {
        @Unique
        private static final int PENCIL_TEXTURE_SIZE = 32;
        @Unique
        private static final int PENCIL_SIZE = 16;
        @Unique
        private static final int PENCIL_RIGHT_MARGIN = 4;
        @Unique
        private static final int PENCIL_BOTTOM_MARGIN = 0;

        @Shadow @Final
        private TransferableSelectionList parent;
        @Shadow @Final
        protected Minecraft minecraft;
        @Shadow @Final
        private PackSelectionModel.Entry pack;

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
        private void vtdownloader$init(TransferableSelectionList outer, Minecraft client, TransferableSelectionList widget, PackSelectionModel.Entry pack, CallbackInfo ci) {
            if (((PackEntryListWidgetAccess) widget).vtdownloader$isResourcePackList()) {
                this.vtdownloader$vtPack = pack.getDescription().getString().contains(Constants.VT_DESCRIPTION_MARKER);
                this.vtdownloader$editable = this.vtdownloader$vtPack && ((PackEntryListWidgetAccess) this.parent).vtdownloader$isAvailablePackList();
            }
        }

        @Inject(at = @At("TAIL"), method = "extractContent")
        private void renderEditButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
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

                // drawTexture
                graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.PENCIL_TEXTURE, pencilX, pencilY,
                        u, v, PENCIL_SIZE, PENCIL_SIZE, PENCIL_TEXTURE_SIZE, PENCIL_TEXTURE_SIZE);
            }
        }

        // @version 26.1
        @Inject(at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/screens/packs/PackSelectionModel$Entry;canSelect()Z"
        ), method = "mouseClicked")
        private void onMouseClicked(MouseButtonEvent event, boolean bl, CallbackInfoReturnable<Boolean> cir,
                                    @Local(ordinal = 0) int clickedX, @Local(ordinal = 1) int clickedY) {
            if (this.vtdownloader$editable) {
                int pencilX = this.vtdownloader$getPencilXOffset();
                int pencilY = this.vtdownloader$getPencilYOffset();

                if (clickedX >= pencilX && clickedX < pencilX + PENCIL_SIZE
                        && clickedY >= pencilY && clickedY < pencilY + PENCIL_SIZE) {
                    PackSelectionScreen screen = ((PackEntryListWidgetAccess) this.parent).vtdownloader$getScreen();
                    ((PackScreenAccess) screen).vtdownloader$applyChanges();
                    this.minecraft.gui.setScreen(new VTDownloadScreen(screen,
                            Constants.RESOURCE_PACK_SCREEN_SUBTITLE, this.pack));
                }
            }
        }
    }
}
