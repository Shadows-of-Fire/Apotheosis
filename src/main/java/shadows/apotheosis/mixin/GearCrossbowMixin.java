package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import shadows.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;

@Pseudo
@Mixin(targets = "net.silentchaos512.gear.item.gear.GearCrossbowItem")
public class GearCrossbowMixin extends CrossbowItem {

	public GearCrossbowMixin(Properties pProperties) {
		super(pProperties);
	}

	@Inject(method = "fireProjectiles", at = @At(value = "HEAD", remap = false))
	private static void apoth_preFired(Level pLevel, LivingEntity pPlayer, InteractionHand pHand, ItemStack crossbow, float velocity, float inaccuracy, CallbackInfo ci) {
		CrescendoEnchant.preArrowFired(pPlayer.getItemInHand(pHand));
	}

	@Inject(method = "m_7203_", at = @At(value = "RETURN", ordinal = 1))
	public void apoth_addCharges(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci) {
		CrescendoEnchant.onArrowFired(pPlayer.getItemInHand(pHand));
	}

	@Inject(method = "getArrow", at = @At(value = "RETURN"), remap = false)
	private static void apoth_markArrows(Level pLevel, LivingEntity pLivingEntity, ItemStack pCrossbowStack, ItemStack pAmmoStack, CallbackInfoReturnable<AbstractArrow> ci) {
		CrescendoEnchant.markGeneratedArrows(ci.getReturnValue(), pCrossbowStack);
	}
}
