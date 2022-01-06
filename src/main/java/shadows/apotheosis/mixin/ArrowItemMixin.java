package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import shadows.apotheosis.Apoth;

@Mixin(ArrowItem.class)
public class ArrowItemMixin {

	@Inject(method = "isInfinite", at = @At(value = "RETURN"), remap = false, cancellable = true)
	public void isInfinite(ItemStack stack, ItemStack bow, Player player, CallbackInfoReturnable<Boolean> ci) {
		if (!ci.getReturnValueZ()) {
			ci.setReturnValue(EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.TRUE_INFINITY, bow) > 0 && stack.getItem() instanceof ArrowItem);
		}
	}

}
