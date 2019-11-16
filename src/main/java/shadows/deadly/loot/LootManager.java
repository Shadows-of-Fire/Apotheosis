package shadows.deadly.loot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Multimap;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import shadows.deadly.gen.BossItem.EquipmentType;
import shadows.deadly.loot.affixes.Affix;
import shadows.deadly.loot.affixes.AffixHelper;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class LootManager {

	/**
	 * The collection of all prefix affixes, sorted by loot entry type.
	 */
	private static final Map<LootEntry.Type, List<Affix>> PREFIXES = new EnumMap<>(LootEntry.Type.class);

	/**
	 * The collection of all suffix affixes, sorted by loot entry type.
	 */
	private static final Map<LootEntry.Type, List<Affix>> SUFFIXES = new EnumMap<>(LootEntry.Type.class);

	/**
	 * A collection of special affixes, based on rarity.
	 * In the event that a rarity has an affix from this set available, it will be used first, before searching the other maps.
	 */
	private static final Map<LootRarity, Map<LootEntry.Type, List<Affix>>> RARE_AFFIXES = new EnumMap<>(LootRarity.class);

	/**
	 * A list of all loot entries that can be dropped.
	 */
	private static final List<LootEntry> ENTRIES = new ArrayList<>();

	/**
	 * A list of all the possible unique items.
	 */
	private static final List<Unique> UNIQUES = new ArrayList<>();

	/**
	 * The collection of all transcended enchantment affixes.
	 */
	private static final Map<LootEntry.Type, List<Affix>> TRANSCENDED_ENCH = new EnumMap<>(LootEntry.Type.class);

	/**
	 * Registers an affix.  Throws {@link UnsupportedOperationException} if the affix is found to be present already.
	 * @param type The loot type of the affix.
	 * @param affix The affix to register.
	 */
	public static void registerAffix(LootEntry.Type type, Affix affix, boolean prefix) {
		if (affix == null) throw new NullPointerException("Attempted to register null affix!");
		Map<LootEntry.Type, List<Affix>> afxMap = prefix ? PREFIXES : SUFFIXES;
		List<Affix> affixes = afxMap.computeIfAbsent(type, t -> new ArrayList<>());
		if (affixes.contains(affix)) throw new UnsupportedOperationException("Attempted to register affix " + affix.getRegistryName() + " for category " + type + " but it is already present!");
		affixes.add(affix);
	}

	/**
	 * Registers a rare affix.  Throws {@link UnsupportedOperationException} if the affix is found to be present already.
	 * @param rarity The loot rarity to add the affix for.
	 * @param type The loot type of the affix.
	 * @param affix The affix to register.
	 */
	public static void registerRareAffix(LootRarity rarity, LootEntry.Type type, Affix affix) {
		if (affix == null) throw new NullPointerException("Attempted to register null rare affix!");
		Map<LootEntry.Type, List<Affix>> afxMap = RARE_AFFIXES.computeIfAbsent(rarity, r -> new EnumMap<>(LootEntry.Type.class));
		List<Affix> affixes = afxMap.computeIfAbsent(type, t -> new ArrayList<>());
		if (affixes.contains(affix)) throw new UnsupportedOperationException("Attempted to register rare affix " + affix.getRegistryName() + " for category " + type + " and rarity " + rarity + " but it is already present!");
		affixes.add(affix);
	}

	/**
	 * Registers a transcended enchantment.  Throws {@link UnsupportedOperationException} if the affix is found to be present already.
	 * @param type The loot type of the affix.
	 * @param affix The affix to register.
	 */
	public static void registerTransEnch(LootEntry.Type type, Affix affix) {
		if (affix == null) throw new NullPointerException("Attempted to register null transcended enchantment!");
		List<Affix> affixes = TRANSCENDED_ENCH.computeIfAbsent(type, t -> new ArrayList<>());
		if (affixes.contains(affix)) throw new UnsupportedOperationException("Attempted to register affix " + affix.getRegistryName() + " for category " + type + " but it is already present!");
		affixes.add(affix);
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
	 * Generates a random loot item.
	 * @param rand A random.
	 * @return A loot item, based on the random and all possible rarities, entries, and affixes.
	 */
	public static ItemStack genLootItem(Random rand) { //TODO Limit to one suffix due to name length, introduce modifiers.
		LootRarity rarity = LootRarity.random(rand);
		LootEntry entry = WeightedRandom.getRandomItem(rand, ENTRIES);
		ItemStack stack = rarity == LootRarity.UNIQUE ? genUnique(rand) : entry.getStack().copy();
		ITextComponent name = new TextComponentString(stack.getDisplayName());
		Set<Affix> affixes = new HashSet<>();
		EntityEquipmentSlot slot = EquipmentType.getTypeFor(stack).getSlot(stack);
		Multimap<String, AttributeModifier> modifs = stack.getAttributeModifiers(slot);
		AffixHelper.addLore(stack, new TextComponentTranslation("rarity.apoth." + rarity.name().toLowerCase(Locale.ROOT)).setStyle(new Style().setColor(rarity.color).setItalic(true)).getFormattedText());

		modifs.forEach((s, a) -> stack.addAttributeModifier(s, a, slot));

		if (RARE_AFFIXES.containsKey(rarity) && RARE_AFFIXES.get(rarity).containsKey(entry.type)) {
			affixes.add(WeightedRandom.getRandomItem(rand, RARE_AFFIXES.get(rarity).get(entry.type)));
		}

		if (rarity.ordinal() >= LootRarity.TRANSCENDED.ordinal()) {
			Affix afx = WeightedRandom.getRandomItem(rand, TRANSCENDED_ENCH.get(entry.type));
			AffixHelper.applyAffix(stack, afx, afx.apply(stack, rand, null));
			name = afx.chainName(name, null);
		}

		if (rarity.ordinal() >= LootRarity.ANCIENT.ordinal()) {
			NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
			tag.setBoolean("Unbreakable", true);
			stack.setTagCompound(tag);
		}

		while (affixes.size() != rarity.affixes) {
			affixes.add(selectAffix(entry.type, rand));
		}

		for (Affix a : affixes) {
			name = a.chainName(name, null);
			AffixHelper.applyAffix(stack, a, a.apply(stack, rand, null));
		}

		stack.setStackDisplayName(TextFormatting.RESET + rarity.getColor().toString() + name.getFormattedText().replace(TextFormatting.RESET.toString(), ""));
		return stack;
	}

	/**
	 * Selects an affix for the given type.  50% chance to select a prefix or suffix.
	 */
	private static Affix selectAffix(LootEntry.Type type, Random rand) {
		if (rand.nextBoolean()) {
			return WeightedRandom.getRandomItem(rand, PREFIXES.get(type));
		} else {
			return WeightedRandom.getRandomItem(rand, SUFFIXES.get(type));
		}
	}

	/**
	 * Creates a unique item. Should only be used during {@link genLootItem} when it rolls {@link LootRarity#UNIQUE}
	 */
	public static ItemStack genUnique(Random rand) {
		return ItemStack.EMPTY;//WeightedRandom.getRandomItem(rand, UNIQUES).makeStack();
	}
}
