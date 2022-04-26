package shadows.apotheosis.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.inventory.AnvilMenu;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

	@ModifyConstant(method = "createResult()V", constant = @Constant(intValue = 40))
	public int apoth_removeLevelCap(int old) {
		return Integer.MAX_VALUE;
	}

	@Redirect(method = "createResult" , at =  @At(value = "INVOKE",target = "Lnet/minecraft/world/item/ItemStack;isDamageableItem()Z"))
	public boolean apoth_isTrulyDamageable(ItemStack stack)
	{
		return stack.getItem().isDamageable(stack);
	}

}
