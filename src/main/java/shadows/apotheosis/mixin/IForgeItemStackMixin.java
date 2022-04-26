package shadows.apotheosis.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Overwrite;
import shadows.apotheosis.deadly.asm.DeadlyHooks;

@Mixin(IForgeItemStack.class)
public interface IForgeItemStackMixin {

	@Overwrite(remap = false)
	public default int getItemEnchantability() {
		return DeadlyHooks.getEnchantability((ItemStack) (Object) this);
	}
}
