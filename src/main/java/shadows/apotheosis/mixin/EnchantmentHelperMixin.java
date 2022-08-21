package shadows.apotheosis.mixin;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
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

	/**
	 * Injection to {@link EnchantmentHelper#getDamageProtection(Iterable, DamageSource)}
	 */
	@Inject(at = @At("RETURN"), method = "getDamageProtection(Ljava/lang/Iterable;Lnet/minecraft/world/damagesource/DamageSource;)I", cancellable = true)
	private static void apoth_getDamageProtection(Iterable<ItemStack> stacks, DamageSource source, CallbackInfoReturnable<Integer> cir) {
		int prot = cir.getReturnValueI();
		for (ItemStack s : stacks) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(s);
			for (AffixInstance inst : affixes.values()) {
				prot += inst.getDamageProtection(source);
			}
		}
		cir.setReturnValue(prot);
	}

	/**
	 * Injection to {@link EnchantmentHelper#getDamageBonus(ItemStack, MobType)
	 */
	@Inject(at = @At("RETURN"), method = "getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F", cancellable = true)
	private static void apoth_getDamageBonus(ItemStack stack, MobType type, CallbackInfoReturnable<Float> cir) {
		float dmg = cir.getReturnValueF();
		Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
		for (AffixInstance inst : affixes.values()) {
			dmg += inst.getDamageBonus(type);
		}
		cir.setReturnValue(dmg);
	}

	/**
	 * Injection to {@link EnchantmentHelper#doPostDamageEffects(LivingEntity, Entity)}
	 */
	@Inject(at = @At("TAIL"), method = "doPostDamageEffects(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;)V")
	private static void apoth_doPostDamageEffects(LivingEntity user, Entity target, CallbackInfo ci) {
		if (user == null) return;
		for (ItemStack s : user.getAllSlots()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(s);
			for (AffixInstance inst : affixes.values()) {
				int old = target.invulnerableTime;
				target.invulnerableTime = 0;
				inst.doPostAttack(user, target);
				target.invulnerableTime = old;
			}
		}
	}

	/**
	 * Injection to {@link EnchantmentHelper#doPostHurtEffects(LivingEntity, Entity)}
	 */
	@Inject(at = @At("TAIL"), method = "doPostHurtEffects(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;)V")
	private static void apoth_doPostHurtEffects(LivingEntity user, Entity attacker, CallbackInfo ci) {
		if (user == null) return;
		for (ItemStack s : user.getAllSlots()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(s);
			for (AffixInstance inst : affixes.values()) {
				inst.doPostHurt(user, attacker);
			}
		}
	}

}
