package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.VTDMod;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Util.class)
public class UtilMixin {
	@Inject(method = "shutdownExecutors", at = @At("TAIL"))
	private static void vtdownloader$shutdownExecutors(CallbackInfo ci) {
		VTDMod.shutdownExecutor();
	}
}
