package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import shadows.apotheosis.deadly.asm.DeadlyHooks;

@Mixin(IForgeItemStack.class)
public interface MixinIForgeItemStack {

	@Overwrite(remap = false)
	public default int getItemEnchantability() {
		return DeadlyHooks.getEnchantability((ItemStack) (Object) this);
	}
}
