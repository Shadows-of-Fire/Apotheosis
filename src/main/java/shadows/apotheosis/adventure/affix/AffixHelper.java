package shadows.apotheosis.adventure.affix;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.floats.Float2IntFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import shadows.apotheosis.adventure.loot.LootRarity;

public class AffixHelper {

	public static final String DISPLAY = "display";
	public static final String LORE = "Lore";

	public static final String AFFIX_DATA = "affix_data";
	public static final String AFFIXES = "affixes";
	public static final String RARITY = "rarity";
	public static final String NAME = "name";

	private static final Multimap<AffixType, Affix> BY_TYPE = HashMultimap.create();

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
			affixesTag.putFloat(inst.affix().getRegistryName().toString(), inst.level());
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
				Affix affix = Affix.REGISTRY.getValue(new ResourceLocation(key));
				if (affix == null) continue;
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
		Component comp = new TranslatableComponent("%s", new TextComponent("")).withStyle(Style.EMPTY.withColor(rarity.color()));
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		afxData.putString(NAME, Component.Serializer.toJson(comp));
		//if (!stack.getOrCreateTagElement(DISPLAY).contains(LORE)) AffixHelper.addLore(stack, new TranslatableComponent("info.apotheosis.affix_item").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
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
		return BY_TYPE.get(type);
	}

	public static void recomputeMaps(IForgeRegistry<Affix> reg, RegistryManager stage) {
		BY_TYPE.clear();
		reg.forEach(a -> BY_TYPE.put(a.getType(), a));
	}

	/**
	 * Level Function that allows for only returning "nice" stepped numbers.
	 * @param min The min value
	 * @param steps The max number of steps
	 * @param step The value per step
	 * @return A level function according to these rules
	 */
	public static Float2FloatFunction step(float min, int steps, float step) {
		return level -> min + (int) (steps * (level + 0.5F / steps)) * step;
	}

	public static Float2IntFunction step(int min, int steps, int step) {
		return level -> min + (int) (steps * (level + 0.5F / steps)) * step;
	}

}