package shadows.apotheosis.deadly.asm;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixEvents;
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
					int old = target.hurtResistantTime = 0;
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
	 * ASM Hook: Called from {@link ItemStack#onItemUse(ItemUseContext)}
	 */
	public static ActionResultType onItemUse(ItemStack stack, ItemUseContext ctx) {
		ActionResultType type = AffixEvents.onItemUse(ctx);
		if (type != null) return type;
		if (!ctx.getWorld().isRemote) return net.minecraftforge.common.ForgeHooks.onPlaceItemIntoWorld(ctx);
		return onItemUse(stack, ctx, c -> stack.getItem().onItemUse(ctx));
	}

	/**
	 * Vanilla (Patch) Copy :: {@link ItemStack#onItemUse(ItemUseContext, Function)}
	 */
	public static ActionResultType onItemUse(ItemStack stack, ItemUseContext ctx, Function<ItemUseContext, ActionResultType> callback) {
		PlayerEntity playerentity = ctx.getPlayer();
		BlockPos blockpos = ctx.getPos();
		CachedBlockInfo cachedblockinfo = new CachedBlockInfo(ctx.getWorld(), blockpos, false);
		if (playerentity != null && !playerentity.abilities.allowEdit && !stack.canPlaceOn(ctx.getWorld().getTags(), cachedblockinfo)) {
			return ActionResultType.PASS;
		} else {
			Item item = stack.getItem();
			ActionResultType actionresulttype = callback.apply(ctx);
			if (playerentity != null && actionresulttype == ActionResultType.SUCCESS) {
				playerentity.addStat(Stats.ITEM_USED.get(item));
			}

			return actionresulttype;
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

}