package shadows.apotheosis.deadly.affix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class AffixHelper {

	public static final String AFFIXES = "Affixes";

	/**
	 * Adds this specific affix to the Item's NBT tag.
	 * Does not edit the item name or perform other changes.
	 */
	public static void applyAffix(ItemStack stack, Affix affix, float level) {
		CompoundNBT tag = stack.getOrCreateChildTag(AFFIXES);
		tag.putFloat(affix.getRegistryName().toString(), level);
	}

	public static void setAffixes(ItemStack stack, Map<Affix, Float> affixes) {
		stack.removeChildTag(AFFIXES);
		affixes.forEach((a, l) -> applyAffix(stack, a, l));
	}

	public static Map<Affix, Float> getAffixes(ItemStack stack) {
		Map<Affix, Float> map = new HashMap<>();
		if (stack.hasTag() && stack.getTag().contains(AFFIXES)) {
			CompoundNBT tag = stack.getTag().getCompound(AFFIXES);
			for (String key : tag.keySet()) {
				Affix affix = Affix.REGISTRY.getValue(new ResourceLocation(key));
				if (affix == null) continue;
				float lvl = tag.getFloat(key);
				map.put(affix, lvl);
			}
		}
		return map;
	}

	public static boolean hasAffixes(ItemStack stack) {
		return stack.hasTag() && !stack.getTag().getCompound(AffixHelper.AFFIXES).isEmpty();
	}

	public static float getAffixLevel(ItemStack stack, Affix afx) {
		if (stack.hasTag() && stack.getTag().contains(AFFIXES)) {
			CompoundNBT tag = stack.getTag().getCompound(AFFIXES);
			return tag.getFloat(afx.getRegistryName().toString());
		}
		return 0;
	}

	public static void addLore(ItemStack stack, ITextComponent lore) {
		CompoundNBT display = stack.getOrCreateChildTag("display");
		ListNBT tag = display.getList("Lore", 8);
		tag.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(lore)));
		display.put("Lore", tag);
	}

	public static List<Affix> getAffixesFor(EquipmentType type) {
		List<Affix> affixes = new ArrayList<>();
		Affix.REGISTRY.getValues().stream().filter(t -> t.canApply(type)).forEach(affixes::add);
		return affixes;
	}

	public static void setRarity(ItemStack stack, LootRarity rarity) {
		AffixHelper.addLore(stack, new TranslationTextComponent("rarity.apoth." + rarity.name().toLowerCase(Locale.ROOT)).mergeStyle(rarity.getColor(), TextFormatting.ITALIC));
		stack.getOrCreateTag().putString("apoth.rarity", rarity.name());
	}

	@Nullable
	public static LootRarity getRarity(ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains("apoth.rarity")) {
			try {
				return LootRarity.valueOf(stack.getTag().getString("apoth.rarity"));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		return null;
	}

}