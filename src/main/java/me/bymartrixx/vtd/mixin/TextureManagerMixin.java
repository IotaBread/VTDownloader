package me.bymartrixx.vtd.mixin;

import me.bymartrixx.vtd.access.TextureManagerAccess;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(TextureManager.class)
public class TextureManagerMixin implements TextureManagerAccess {
	@Shadow @Final
	private Map<Identifier, AbstractTexture> textures;

	@Override
	public boolean vtdownloader$hasTexture(Identifier id) {
		return this.textures.containsKey(id);
	}
}
