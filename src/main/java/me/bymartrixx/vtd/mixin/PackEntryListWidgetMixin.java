package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.PackEntryListWidgetAccess;
import me.bymartrixx.vtd.access.PackScreenAccess;
import me.bymartrixx.vtd.gui.VTDownloadScreen;
import me.bymartrixx.vtd.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
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

@Mixin(PackEntryListWidget.class)
public abstract class PackEntryListWidgetMixin
        implements PackEntryListWidgetAccess {
    @Shadow @Final
    private Text title;

    @Shadow @Final
    PackScreen screen;

    @Override
    public boolean vtdownloader$isAvailablePackList() {
        // Available packs list uses "pack.available.title" as title
        return this.title.asComponent() instanceof TranslatableComponent c && c.getKey().contains("available");
    }

    @Override
    public int vtdownloader$getItemHeight() {
        return 36;
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
    public static abstract class PackEntryMixin {
        @Unique
        private static final int PENCIL_TEXTURE_SIZE = 32;
        @Unique
        private static final int PENCIL_SIZE = 16;
        @Unique
        private static final int PENCIL_RIGHT_MARGIN = 12;
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
        private int vtdownloader$lastRenderX;
        @Unique
        private int vtdownloader$lastRenderY;

        // In 1.21.10+, inner class constructor injection needs the outer class as first parameter
        @Inject(at = @At("TAIL"), method = "<init>")
        private void vtdownloader$init(PackEntryListWidget outer, MinecraftClient client, PackEntryListWidget widget, ResourcePackOrganizer.Pack pack, CallbackInfo ci) {
            if (((PackEntryListWidgetAccess) widget).vtdownloader$isResourcePackList()) {
                this.vtdownloader$vtPack = pack.getDescription().getString().contains(Constants.VT_DESCRIPTION_MARKER);
                this.vtdownloader$editable = this.vtdownloader$vtPack && ((PackEntryListWidgetAccess) this.widget).vtdownloader$isAvailablePackList();
            }
        }

        // In Minecraft 1.21.10+, the render method is method_25343 with different signature
        @Inject(at = @At("TAIL"), method = "method_25343", remap = false)
        private void renderEditButton(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
            if (this.vtdownloader$vtPack) {
                // Calculate entry position from widget
                int entryIndex = this.widget.children().indexOf(this);
                if (entryIndex < 0) return;
                
                int itemHeight = ((PackEntryListWidgetAccess) this.widget).vtdownloader$getItemHeight();
                int entryWidth = this.widget.getRowWidth();
                
                // Get entry position from widget's row calculations
                int x = this.widget.getRowLeft();
                int y = this.widget.getRowTop(entryIndex);
                
                int pencilX = x + entryWidth - PENCIL_SIZE - PENCIL_RIGHT_MARGIN;
                int pencilY = y + itemHeight - PENCIL_SIZE - PENCIL_BOTTOM_MARGIN;
                
                // Check if mouse is directly over the pencil icon (not just the entry)
                boolean mouseOverPencil = mouseX >= pencilX && mouseX < pencilX + PENCIL_SIZE
                        && mouseY >= pencilY && mouseY < pencilY + PENCIL_SIZE;
                
                float u = 0.0F;
                float v = 0.0F;
                if (!this.vtdownloader$editable) {
                    v = PENCIL_SIZE;
                } else if (mouseOverPencil) {
                    // Show white overlay only when cursor is directly over the pencil
                    u = PENCIL_SIZE;
                }

                // Store position for click detection
                this.vtdownloader$lastRenderX = x;
                this.vtdownloader$lastRenderY = y;
                
                graphics.drawTexture(RenderPipelines.GUI_TEXTURED, Constants.PENCIL_TEXTURE, pencilX, pencilY,
                        u, v, PENCIL_SIZE, PENCIL_SIZE, PENCIL_TEXTURE_SIZE, PENCIL_TEXTURE_SIZE);
            }
        }

        // Handle click on pencil icon
        @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
        private void onMouseClicked(MouseButtonEvent event, boolean hovered, CallbackInfoReturnable<Boolean> cir) {
            if (this.vtdownloader$editable && this.vtdownloader$lastRenderX > 0) {
                // Get mouse position from client
                double mouseX = this.client.mouse.getX() * this.client.getWindow().getScaledWidth() / this.client.getWindow().getWidth();
                double mouseY = this.client.mouse.getY() * this.client.getWindow().getScaledHeight() / this.client.getWindow().getHeight();
                
                int itemHeight = ((PackEntryListWidgetAccess) this.widget).vtdownloader$getItemHeight();
                int entryWidth = this.widget.getRowWidth();
                
                int pencilX = this.vtdownloader$lastRenderX + entryWidth - PENCIL_SIZE - PENCIL_RIGHT_MARGIN;
                int pencilY = this.vtdownloader$lastRenderY + itemHeight - PENCIL_SIZE - PENCIL_BOTTOM_MARGIN;
                
                if (mouseX >= pencilX && mouseX < pencilX + PENCIL_SIZE
                        && mouseY >= pencilY && mouseY < pencilY + PENCIL_SIZE) {
                    PackScreen screen = ((PackEntryListWidgetAccess) this.widget).vtdownloader$getScreen();
                    ((PackScreenAccess) screen).vtdownloader$applyChanges();
                    this.client.setScreen(new VTDownloadScreen(screen,
                            Constants.RESOURCE_PACK_SCREEN_SUBTITLE, this.pack));
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
