package shadows.apotheosis.deadly.loot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Multimap;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.gen.BossItem.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class LootManager {

	/**
	 * The collection of all prefix affixes, sorted by loot entry type.
	 */
	private static final Map<EquipmentType, List<Affix>> PREFIXES = new EnumMap<>(EquipmentType.class);

	/**
	 * The collection of all suffix affixes, sorted by loot entry type.
	 */
	private static final Map<EquipmentType, List<Affix>> SUFFIXES = new EnumMap<>(EquipmentType.class);

	/**
	 * The collection of all weak affixes.
	 */
	private static final Map<EquipmentType, List<Affix>> WEAK_AFFIXES = new EnumMap<>(EquipmentType.class);

	/**
	 * The collection of all epic affixes.
	 */
	private static final Map<EquipmentType, List<Affix>> EPIC_AFFIXES = new EnumMap<>(EquipmentType.class);

	/**
	 * A list of all affix modifiers.
	 */
	private static final List<AffixModifier> MODIFIERS = new ArrayList<>();

	/**
	 * A list of all loot entries that can be dropped.
	 */
	private static final List<LootEntry> ENTRIES = new ArrayList<>();

	/**
	 * A list of all the possible unique items.
	 */
	private static final List<Unique> UNIQUES = new ArrayList<>();

	/**
	 * Registers an affix.  Throws {@link UnsupportedOperationException} if the affix is found to be present already.
	 * @param type The loot type of the affix.
	 * @param affix The affix to register.
	 */
	public static void registerAffix(EquipmentType type, Affix affix) {
		if (affix == null) throw new NullPointerException("Attempted to register null affix!");
		Map<EquipmentType, List<Affix>> afxMap = affix.isPrefix() ? PREFIXES : SUFFIXES;
		List<Affix> affixes = afxMap.computeIfAbsent(type, t -> new ArrayList<>());
		if (affixes.contains(affix)) throw new UnsupportedOperationException("Attempted to register affix " + affix.getRegistryName() + " for category " + type + " but it is already present!");
		affixes.add(affix);
	}

	/**
	 * Registers an epic affix.  Throws {@link UnsupportedOperationException} if the affix is found to be present already.
	 * An epic affix is a special affix available only to epic rarity items and above.  It's name (and modifier) are applied last.
	 * This means it may override any existing name changes or modifications.
	 * @param type The loot type of the affix.
	 * @param affix The affix to register.
	 */
	public static void registerWeakAffix(EquipmentType type, Affix affix) {
		if (affix == null) throw new NullPointerException("Attempted to register null weak affix!");
		List<Affix> affixes = WEAK_AFFIXES.computeIfAbsent(type, t -> new ArrayList<>());
		if (affixes.contains(affix)) throw new UnsupportedOperationException("Attempted to register weak affix " + affix.getRegistryName() + " for category " + type + " but it is already present!");
		affixes.add(affix);
	}

	/**
	 * Registers an epic affix.  Throws {@link UnsupportedOperationException} if the affix is found to be present already.
	 * An epic affix is a special affix available only to epic rarity items and above.  It's name (and modifier) are applied last.
	 * This means it may override any existing name changes or modifications.
	 * @param type The loot type of the affix.
	 * @param affix The affix to register.
	 */
	public static void registerEpicAffix(EquipmentType type, Affix affix) {
		if (affix == null) throw new NullPointerException("Attempted to register null epic affix!");
		List<Affix> affixes = EPIC_AFFIXES.computeIfAbsent(type, t -> new ArrayList<>());
		if (affixes.contains(affix)) throw new UnsupportedOperationException("Attempted to register epic affix " + affix.getRegistryName() + " for category " + type + " but it is already present!");
		affixes.add(affix);
	}

	/**
	 * Registers an affix modifier.  Null modifiers are not permitted.
	 */
	public static void registerModifier(AffixModifier entry) {
		if (entry == null) throw new NullPointerException("Attempted to register invalid affix modifier!");
		MODIFIERS.add(entry);
	}

	/**
	 * Registers a loot entry.  Null entries, or entries with empty itemstacks, are not permitted.
	 */
	public static void registerEntry(LootEntry entry) {
		if (entry == null || entry.getStack().isEmpty()) throw new NullPointerException("Attempted to register invalid loot entry!");
		ENTRIES.add(entry);
	}

	/**
	 * Registers a unique entry.  Null entries, or entries with empty itemstacks, are not permitted.
	 */
	public static void registerUnique(Unique entry) {
		if (entry == null || entry.getStack().isEmpty()) throw new NullPointerException("Attempted to register invalid loot entry!");
		UNIQUES.add(entry);
	}

	/**
	 * Selects a random loot entry itemstack from the list of entries.
	 * @param rand A random.
	 * @param rarity If this is {@link LootRarity#UNIQUE}, then the item returned will be an {@link Unique}
	 * @return A loot entry's stack, or a unique, if the rarity selected was unique.
	 */
	public static ItemStack getRandomEntry(Random rand, LootRarity rarity) {
		LootEntry entry = WeightedRandom.getRandomItem(rand, ENTRIES);
		ItemStack stack = rarity == LootRarity.UNIQUE ? genUnique(rand) : entry.getStack().copy();
		return stack;
	}

	/**
	 * Applies loot modifiers to the passed in itemstack.
	 * Note that this will be unusual if the passed in itemstack does not meet the qualities of any equipment type.
	 * The default equipment type is {@link EquipmentType#TOOL}, so items that do not match will be treated as tools.
	 */
	public static ItemStack genLootItem(ItemStack stack, Random rand, LootRarity rarity) {
		ITextComponent name = stack.getDisplayName();
		EquipmentType type = EquipmentType.getTypeFor(stack);
		Map<Affix, AffixModifier> affixes = new HashMap<>();
		EquipmentSlotType slot = EquipmentType.getTypeFor(stack).getSlot(stack);
		Multimap<String, AttributeModifier> modifs = stack.getAttributeModifiers(slot);
		AffixHelper.addLore(stack, new TranslationTextComponent("rarity.apoth." + rarity.name().toLowerCase(Locale.ROOT)).setStyle(new Style().setColor(rarity.color).setItalic(true)));

		modifs.forEach((s, a) -> stack.addAttributeModifier(s, a, slot));

		switch (rarity) {
		case COMMON: {
			List<Affix> afxList = WEAK_AFFIXES.get(type);
			if (rand.nextFloat() <= 0.33F && afxList != null) affixes.put(WeightedRandom.getRandomItem(rand, afxList), null);
			break;
		}
		case UNCOMMON: {
			List<Affix> afxList = rand.nextBoolean() ? PREFIXES.get(type) : SUFFIXES.get(type);
			affixes.put(WeightedRandom.getRandomItem(rand, afxList), null);
			break;
		}
		case RARE:
		case EPIC:
		case ANCIENT:
		case UNIQUE: {
			List<Affix> afxList = PREFIXES.get(type);
			affixes.put(WeightedRandom.getRandomItem(rand, afxList), null);
			afxList = SUFFIXES.get(type);
			affixes.put(WeightedRandom.getRandomItem(rand, afxList), null);
			break;
		}
		}

		boolean epicModif = false;

		if (rarity.ordinal() >= LootRarity.EPIC.ordinal()) {
			if (rarity.ordinal() >= LootRarity.ANCIENT.ordinal()) epicModif = rand.nextBoolean();
			int numModifs = rarity == LootRarity.EPIC ? 1 : epicModif ? 1 : 2;
			Affix[] keys = affixes.keySet().toArray(new Affix[2]);
			int modifKey = rand.nextInt(2);
			affixes.put(keys[modifKey], getModifier(rand));
			if (numModifs == 2) {
				modifKey = modifKey == 1 ? 0 : 1;
				affixes.put(keys[modifKey], getModifier(rand));
			}
		}

		for (Affix a : affixes.keySet()) {
			name = a.chainName(name, affixes.get(a));
			AffixHelper.applyAffix(stack, a, a.apply(stack, rand, affixes.get(a)));
		}

		if (rarity.ordinal() >= LootRarity.EPIC.ordinal()) {
			Affix afx = WeightedRandom.getRandomItem(rand, EPIC_AFFIXES.get(type));
			AffixModifier epic = epicModif ? getModifier(rand) : null;
			AffixHelper.applyAffix(stack, afx, afx.apply(stack, rand, epic));
			name = afx.chainName(name, epic);
		}

		if (rarity.ordinal() >= LootRarity.ANCIENT.ordinal()) {
			CompoundNBT tag = stack.getOrCreateTag();
			tag.putBoolean("Unbreakable", true);
		}

		stack.setDisplayName(new StringTextComponent(TextFormatting.RESET + rarity.getColor().toString() + name.getFormattedText().replace(TextFormatting.RESET.toString(), "")));
		return stack;
	}

	/**
	 * Returns a random affix modifier.
	 */
	public static AffixModifier getModifier(Random rand) {
		return WeightedRandom.getRandomItem(rand, MODIFIERS);
	}

	/**
	 * Creates a unique item. Should only be used during {@link genLootItem} when it rolls {@link LootRarity#UNIQUE}
	 */
	public static ItemStack genUnique(Random rand) {
		return ItemStack.EMPTY;//WeightedRandom.getRandomItem(rand, UNIQUES).makeStack();
	}
}
