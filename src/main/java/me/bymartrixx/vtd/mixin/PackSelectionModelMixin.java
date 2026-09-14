package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.EntryBaseAccess;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PackSelectionModel.class)
public class PackSelectionModelMixin {
    @Mixin(targets = "net/minecraft/client/gui/screens/packs/PackSelectionModel$EntryBase")
    public static class EntryBaseMixin implements EntryBaseAccess {
        @Shadow @Final
        private Pack pack;

        @Override
        public Pack vtdownloader$getPack() {
            return this.pack;
        }
    }
}
