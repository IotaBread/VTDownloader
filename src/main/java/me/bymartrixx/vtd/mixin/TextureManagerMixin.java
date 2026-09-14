package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.TextureManagerAccess;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(TextureManager.class)
public class TextureManagerMixin implements TextureManagerAccess {
	@Shadow @Final
	private Map<Identifier, AbstractTexture> byPath;

	@Override
	public boolean vtdownloader$hasTexture(Identifier id) {
		return this.byPath.containsKey(id);
	}
}
