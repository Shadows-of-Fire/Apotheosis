package shadows.apotheosis.deadly.asm;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.util.DamageSource;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.Affixes;

/**
 * ASM methods for the deadly module.
 * @author Shadows
 *
 */
public class DeadlyHooks {

	/**
	 * ASM Hook: Called from {@link AttributeModifier#fromTag}
	 */
	public static UUID getRealUUID(UUID uuid) {
		if (!Apotheosis.enableDeadly) return uuid;
		if (Access.getADM().equals(uuid)) return Access.getADM();
		if (Access.getASM().equals(uuid)) return Access.getASM();
		return uuid;
	}

	public static class Access extends Item {
		public Access(Properties properties) {
			super(properties);
		}

		public static UUID getADM() {
			return Item.ATTACK_DAMAGE_MODIFIER;
		}

		public static UUID getASM() {
			return Item.ATTACK_SPEED_MODIFIER;
		}
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#getEnchantmentModifierDamage}
	 */
	public static int getProtectionLevel(Iterable<ItemStack> stacks, DamageSource source) {
		int prot = 0;
		for (ItemStack s : stacks) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
			for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
				prot += e.getKey().getProtectionLevel(e.getValue(), source);
			}
		}
		return prot;
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#getModifierForCreature}
	 */
	public static float getExtraDamageFor(ItemStack stack, CreatureAttribute type) {
		float dmg = 0;
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
			dmg += e.getKey().getExtraDamageFor(e.getValue(), type);
		}
		return dmg;
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#applyArthropodEnchantments}
	 */
	public static void onEntityDamaged(LivingEntity user, Entity target) {
		if (user != null) {
			for (ItemStack s : user.getEquipmentAndArmor()) {
				Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					int old = target.hurtResistantTime;
					target.hurtResistantTime = 0;
					e.getKey().onEntityDamaged(user, target, e.getValue());
					target.hurtResistantTime = old;
				}
			}
		}
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#applyThornEnchantments}
	 */
	public static void onUserHurt(LivingEntity user, Entity attacker) {
		if (user != null) {
			for (ItemStack s : user.getEquipmentAndArmor()) {
				Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					e.getKey().onUserHurt(user, attacker, e.getValue());
				}
			}
		}
	}

	/**
	 * Allows for the enchantability affix to work properly.
	 */
	public static int getEnchantability(ItemStack stack) {
		int ench = stack.getItemEnchantability();
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		if (!affixes.isEmpty()) {
			ench += affixes.getOrDefault(Affixes.ENCHANTABILITY, 0F).intValue();
		}
		return ench;
	}

	/**
	 * ASM Hook: Called from {@link CampfireTileEntity#findMatchingRecipe}<br>
	 * Replaces the standard {@link Inventory} with a context-aware {@link CampfireInventory}.
	 */
	public static IInventory getCampfireInv(IInventory src, CampfireTileEntity tile) {
		return new CampfireInventory(tile, src.getStackInSlot(0));
	}

	public static class CampfireInventory extends Inventory {

		private final WeakReference<CampfireTileEntity> tile;

		public CampfireInventory(CampfireTileEntity tile, ItemStack stack) {
			super(stack);
			this.tile = new WeakReference<>(tile);
		}

		public CampfireTileEntity getTile() {
			return this.tile.get();
		}

	}

	/**
	 * ASM Hook: Replaces all calls to {@link ItemStack#isDamageable()} in {@link RepairContainer}<br>
	 * This allows for items with the "Unbreakable" tag to be used in an anvil.<br>
	 * Applied by mythics_anvil.js
	 */
	public static boolean isTrulyDamageable(ItemStack stack) {
		return stack.getItem().isDamageable(stack);
	}

}