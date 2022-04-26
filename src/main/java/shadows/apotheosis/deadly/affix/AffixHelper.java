package shadows.apotheosis.deadly.affix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

public class AffixHelper {

	public static final String AFFIX_DATA = "AffixData";
	public static final String AFFIXES = "Affixes";

	/**
	 * Adds this specific affix to the Item's NBT tag.
	 * Disallows illegal affixes.
	 */
	public static void applyAffix(ItemStack stack, Affix affix, float level) {
		LootCategory cat = LootCategory.forItem(stack);
		if (!(stack.getItem() instanceof IAffixSensitiveItem) && (cat == null || !affix.canApply(cat))) return;
		var afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		if (!afxData.contains(AFFIXES)) afxData.put(AFFIXES, new CompoundTag());
		var affixes = afxData.getCompound(AFFIXES);
		affixes.putFloat(affix.getRegistryName().toString(), level);
	}

	public static void setAffixes(ItemStack stack, Map<Affix, Float> affixes) {
		var afxData = stack.getTagElement(AFFIX_DATA);
		if (afxData != null) afxData.remove(AFFIXES);
		affixes.forEach((a, l) -> applyAffix(stack, a, l));
	}

	public static Map<Affix, Float> getAffixes(ItemStack stack) {
		Map<Affix, Float> map = new HashMap<>();
		CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
		if (afxData != null && afxData.contains(AFFIXES)) {
			CompoundTag affixes = afxData.getCompound(AFFIXES);
			for (String key : affixes.getAllKeys()) {
				Affix affix = Affix.REGISTRY.getValue(new ResourceLocation(key));
				if (affix == null) continue;
				float lvl = affixes.getFloat(key);
				map.put(affix, lvl);
			}
		}
		return map;
	}

	public static boolean hasAffixes(ItemStack stack) {
		var afxData = stack.getTagElement(AFFIX_DATA);
		return afxData != null && !afxData.getCompound(AFFIXES).isEmpty();
	}

	public static void addLore(ItemStack stack, Component lore) {
		CompoundTag display = stack.getOrCreateTagElement("display");
		ListTag tag = display.getList("Lore", 8);
		tag.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
		display.put("Lore", tag);
	}

	public static List<Affix> getAffixesFor(LootCategory type, LootRarity maxAffixRarity) {
		List<Affix> affixes = new ArrayList<>();
		Affix.REGISTRY.getValues().stream().filter(t -> t.canApply(type) && t.getRarity().ordinal() <= maxAffixRarity.ordinal()).forEach(affixes::add);
		return affixes;
	}

	public static void setRarity(ItemStack stack, LootRarity rarity) {
		Component comp = new TranslatableComponent("%s", new TextComponent("")).withStyle(Style.EMPTY.withColor(rarity.getColor()));
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		afxData.putString("Name", Component.Serializer.toJson(comp));
		if (!stack.getOrCreateTagElement("display").contains("Lore")) AffixHelper.addLore(stack, new TranslatableComponent("info.apotheosis.affix_item").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
		afxData.putString("Rarity", rarity.name());
		stack.hideTooltipPart(TooltipPart.MODIFIERS);
	}

	@Nullable
	public static LootRarity getRarity(ItemStack stack) {
		var afxData = stack.getTagElement(AFFIX_DATA);
		if (afxData != null) {
			try {
				return LootRarity.valueOf(afxData.getString("Rarity"));
			} catch (IllegalArgumentException e) {
				afxData.remove("Rarity");
				return null;
			}
		}
		return null;
	}

}