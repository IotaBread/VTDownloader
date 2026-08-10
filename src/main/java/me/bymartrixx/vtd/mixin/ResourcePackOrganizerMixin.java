package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.AbstractPackAccess;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PackSelectionModel.class)
public class ResourcePackOrganizerMixin {
    @Mixin(targets = "net.minecraft.client.gui.screens.packs.PackSelectionModel$EntryBase")
    public static class AbstractPackMixin implements AbstractPackAccess {
        @Shadow @Final
        private Pack pack;

        @Override
        public Pack vtdownloader$getProfile() {
            return this.pack;
        }
    }
}
