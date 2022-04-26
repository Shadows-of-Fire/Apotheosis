package shadows.apotheosis.mixin;

import java.util.List;
import java.util.Random;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import shadows.apotheosis.deadly.asm.DeadlyHooks;
import shadows.apotheosis.ench.table.RealEnchantmentHelper;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

	/**
	 * @author Shadows
	 * @reason Enables apotheosis special handling of enchanting rules.  More lenient injection is not possible.
	 * @param power The current enchanting power.
	 * @param stack The ItemStack being enchanted.
	 * @param allowTreasure If treasure enchantments are allowed.
	 * @return All possible enchantments that are eligible to be placed on this item at a specific power level.
	 */
	@Overwrite
	public static List<EnchantmentInstance> getAvailableEnchantmentResults(int power, ItemStack stack, boolean allowTreasure) {
		return RealEnchantmentHelper.getAvailableEnchantmentResults(power, stack, allowTreasure);
	}

	/**
	 * @author Shadows
	 * @reason Enables global consistency with the apotheosis enchanting system, even outside the table.
	 * @param pRandom The random
	 * @param pItemStack The stack being enchanted
	 * @param pLevel The enchanting level
	 * @param pAllowTreasure If treasure enchantments are allowed.
	 * @return A list of enchantments to apply to this item.
	 */
	@Overwrite
	public static List<EnchantmentInstance> selectEnchantment(Random pRandom, ItemStack pItemStack, int pLevel, boolean pAllowTreasure) {
		return RealEnchantmentHelper.selectEnchantment(pRandom, pItemStack, pLevel, 0, 0, 0, pAllowTreasure);
	}

	@Inject(method = "getDamageBonus", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private static void apoth_getBonusDamage(ItemStack pStack, MobType pCreatureAttribute, CallbackInfoReturnable<Float> cir, MutableFloat mutablefloat){
		float dmgFromAffixes = DeadlyHooks.getExtraDamageFor(pStack, pCreatureAttribute);
		cir.setReturnValue(cir.getReturnValue() + dmgFromAffixes);
	}

	@Inject(method = "getDamageProtection", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private static void apoth_getDamageProtection(Iterable<ItemStack> pStacks, DamageSource pSource, CallbackInfoReturnable<Integer> cir, MutableInt mutableint) {
		int protFromAffixes = DeadlyHooks.getProtectionLevel(pStacks, pSource);
		cir.setReturnValue(cir.getReturnValue() + protFromAffixes);
	}

	@Inject(method = "doPostDamageEffects", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private static void apoth_doPostDamageEffects(LivingEntity pUser, Entity pTarget, CallbackInfo ci){
		DeadlyHooks.onEntityDamaged(pUser, pTarget);
	}

	@Inject(method = "doPostHurtEffects", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private static void apoth_doPostHurtEffects(LivingEntity pUser, Entity pTarget, CallbackInfo ci){
		DeadlyHooks.onUserHurt(pUser, pTarget);
	}
}
