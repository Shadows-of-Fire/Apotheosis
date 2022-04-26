package shadows.apotheosis.deadly.asm;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import shadows.apotheosis.Apoth;
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
	 * Mixin: Called from {@link AttributeModifier#load(CompoundTag)}
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
			return Item.BASE_ATTACK_DAMAGE_UUID;
		}

		public static UUID getASM() {
			return Item.BASE_ATTACK_SPEED_UUID;
		}
	}

	/**
	 * Mixin: Called from {@link EnchantmentHelper#getDamageProtection}
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
	 * Mixin: Called from {@link EnchantmentHelper#getDamageBonus}
	 */
	public static float getExtraDamageFor(ItemStack stack, MobType type) {
		float dmg = 0;
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
			dmg += e.getKey().getExtraDamageFor(e.getValue(), type);
		}
		return dmg;
	}

	/**
	 * Mixin: Called from {@link EnchantmentHelper#doPostDamageEffects}
	 */
	public static void onEntityDamaged(LivingEntity user, Entity target) {
		if (user != null) {
			for (ItemStack s : user.getAllSlots()) {
				Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					int old = target.invulnerableTime;
					target.invulnerableTime = 0;
					e.getKey().onEntityDamaged(user, target, e.getValue());
					target.invulnerableTime = old;
				}
			}
		}
	}

	/**
	 * Mixin: Called from {@link EnchantmentHelper#doPostHurtEffects}
	 */
	public static void onUserHurt(LivingEntity user, Entity attacker) {
		if (user != null) {
			for (ItemStack s : user.getAllSlots()) {
				Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					e.getKey().onUserHurt(user, attacker, e.getValue());
				}
			}
		}
	}

	/**
	 * Mixin: Allows for the enchantability affix to work properly.
	 */
	public static int getEnchantability(ItemStack stack) {
		int ench = stack.getItem().getItemEnchantability(stack);
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		if (!affixes.isEmpty()) {
			ench += affixes.getOrDefault(Apoth.Affixes.ENCHANTABILITY, 0F).intValue();
		}
		return ench;
	}

	/**
	 * ASM Hook: Called from {@link CampfireBlockEntity#getCookableRecipe(ItemStack)}<br>
	 * Replaces the standard {@link Inventory} with a context-aware {@link CampfireInventory}.
	 */
	public static Container getCampfireInv(Container src, CampfireBlockEntity block) {
		return new CampfireInventory(block, src.getItem(0));
	}

	public static class CampfireInventory extends SimpleContainer {

		private final WeakReference<CampfireBlockEntity> block;

		public CampfireInventory(CampfireBlockEntity block, ItemStack stack) {
			super(stack);
			this.block = new WeakReference<>(block);
		}

		public CampfireBlockEntity getTile() {
			return this.block.get();
		}

	}

}