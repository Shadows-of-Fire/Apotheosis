package shadows.deadly.loot.affixes;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class AffixHelper {

	public static final String AFFIXES = "Affixes";

	public static void applyAffix(ItemStack stack, Affix affix, float level) {
		NBTTagCompound tag = stack.getOrCreateSubCompound(AFFIXES);
		if (!tag.hasKey(affix.getRegistryName().toString())) {
			NBTTagCompound afx = new NBTTagCompound();
			afx.setFloat("lvl", level);
			tag.setTag(affix.getRegistryName().toString(), afx);
		} else {
			NBTTagCompound afx = tag.getCompoundTag(affix.getRegistryName().toString());
			afx.setFloat("lvl", level);
		}
	}

	public static Map<Affix, Float> getAffixes(ItemStack stack) {
		Map<Affix, Float> map = new HashMap<>();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(AFFIXES)) {
			NBTTagCompound tag = stack.getTagCompound().getCompoundTag(AFFIXES);
			for (String key : tag.getKeySet()) {
				Affix affix = Affix.REGISTRY.getValue(new ResourceLocation(key));
				float lvl = tag.getFloat("lvl");
				map.put(affix, lvl);
			}
		}
		return map;
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#getEnchantmentModifierDamage}
	 */
	public static int getProtectionLevel(Iterable<ItemStack> stacks, DamageSource source) {
		int prot = 0;
		for (ItemStack s : stacks) {
			Map<Affix, Float> affixes = getAffixes(s);
			for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
				prot += e.getKey().getProtectionLevel(e.getValue(), source);
			}
		}
		return prot;
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#getModifierForCreature}
	 */
	public static float getExtraDamageFor(Iterable<ItemStack> stacks, EnumCreatureAttribute type) {
		float dmg = 0;
		for (ItemStack s : stacks) {
			Map<Affix, Float> affixes = getAffixes(s);
			for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
				dmg += e.getKey().getExtraDamageFor(e.getValue(), type);
			}
		}
		return dmg;
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#applyArthropodEnchantments}
	 */
	public static void onEntityDamaged(EntityLivingBase user, Entity target) {
		if (user != null) {
			for (ItemStack s : user.getEquipmentAndArmor()) {
				Map<Affix, Float> affixes = getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					e.getKey().onEntityDamaged(user, target, e.getValue());
				}
			}
		}
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#applyThornEnchantments}
	 */
	public static void onUserHurt(EntityLivingBase user, Entity target) {
		if (user != null) {
			for (ItemStack s : user.getEquipmentAndArmor()) {
				Map<Affix, Float> affixes = getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					e.getKey().onUserHurt(user, target, e.getValue());
				}
			}
		}
	}

}
