package shadows.apotheosis.adventure.affix;

import java.awt.TextComponent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

public class AffixHelper {

	public static final String DISPLAY = "display";
	public static final String LORE = "Lore";

	public static final String AFFIX_DATA = "affix_data";
	public static final String AFFIXES = "affixes";
	public static final String RARITY = "rarity";
	public static final String NAME = "name";

	/**
	 * Adds this specific affix to the Item's NBT tag.
	 */
	public static void applyAffix(ItemStack stack, AffixInstance affix) {
		var affixes = getAffixes(stack);
		affixes.put(affix.affix(), affix);
		setAffixes(stack, affixes);
	}

	public static void setAffixes(ItemStack stack, Map<Affix, AffixInstance> affixes) {
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		CompoundTag affixesTag = new CompoundTag();
		for (AffixInstance inst : affixes.values()) {
			affixesTag.putFloat(inst.affix().getId().toString(), inst.level());
		}
		afxData.put(AFFIXES, affixesTag);
	}

	public static void setName(ItemStack stack, Component name) {
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		afxData.putString(NAME, Component.Serializer.toJson(name));
	}

	@Nullable
	public static Component getName(ItemStack stack) {
		CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
		if (afxData == null) return null;
		return Component.Serializer.fromJson(afxData.getString(NAME));
	}

	public static Map<Affix, AffixInstance> getAffixes(ItemStack stack) {
		Map<Affix, AffixInstance> map = new HashMap<>();
		CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
		if (afxData != null && afxData.contains(AFFIXES)) {
			CompoundTag affixes = afxData.getCompound(AFFIXES);
			LootRarity rarity = getRarity(stack);
			if (rarity == null) rarity = LootRarity.COMMON;
			for (String key : affixes.getAllKeys()) {
				Affix affix = AffixManager.INSTANCE.getValue(new ResourceLocation(key));
				if (affix == null || !affix.canApplyTo(stack, rarity)) continue;
				float lvl = affixes.getFloat(key);
				map.put(affix, new AffixInstance(affix, stack, rarity, lvl));
			}
		}
		return map;
	}

	public static boolean hasAffixes(ItemStack stack) {
		CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
		return afxData != null && !afxData.getCompound(AFFIXES).isEmpty();
	}

	public static void addLore(ItemStack stack, Component lore) {
		CompoundTag display = stack.getOrCreateTagElement(DISPLAY);
		ListTag tag = display.getList(LORE, 8);
		tag.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
		display.put(LORE, tag);
	}

	public static void setRarity(ItemStack stack, LootRarity rarity) {
		Component comp = Component.translatable("%s", new TextComponent("")).withStyle(Style.EMPTY.withColor(rarity.color()));
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		afxData.putString(NAME, Component.Serializer.toJson(comp));
		//if (!stack.getOrCreateTagElement(DISPLAY).contains(LORE)) AffixHelper.addLore(stack, Component.translatable("info.apotheosis.affix_item").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
		afxData.putString(RARITY, rarity.id());
	}

	@Nullable
	public static LootRarity getRarity(ItemStack stack) {
		CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
		return getRarity(afxData);
	}

	@Nullable
	public static LootRarity getRarity(@Nullable CompoundTag afxData) {
		if (afxData != null) {
			try {
				return LootRarity.byId(afxData.getString(RARITY));
			} catch (IllegalArgumentException e) {
				afxData.remove(RARITY);
				return null;
			}
		}
		return null;
	}

	public static Collection<Affix> byType(AffixType type) {
		return AffixManager.INSTANCE.getTypeMap().get(type);
	}

	public static StepFunction step(float min, int steps, float step) {
		return new StepFunction(min, steps, step);
	}

	public static Map<LootRarity, StepFunction> readValues(JsonObject obj) {
		return Affix.GSON.fromJson(obj, new TypeToken<Map<LootRarity, StepFunction>>() {
		}.getType());
	}

	public static Set<LootCategory> readTypes(JsonArray json) {
		return Affix.GSON.fromJson(json, new TypeToken<Set<LootCategory>>() {
		}.getType());
	}

}