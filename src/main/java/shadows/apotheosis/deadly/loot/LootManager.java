package shadows.apotheosis.deadly.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.affix.Affixes;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.modifiers.Modifiers;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class LootManager extends JsonReloadListener {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(LootEntry.class, (JsonDeserializer<LootEntry>) (json, type, ctx) -> {
		return LootEntry.deserialize(json.getAsJsonObject());
	}).setPrettyPrinting().create();

	public static final LootManager INSTANCE = new LootManager();

	private static final List<LootEntry> ENTRIES = new ArrayList<>();

	private LootManager() {
		super(GSON, "affix_loot_entries");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> objects, IResourceManager mgr, IProfiler profiler) {
		ENTRIES.clear();
		for (Entry<ResourceLocation, JsonObject> obj : objects.entrySet()) {
			try {
				LootEntry ent = GSON.fromJson(obj.getValue(), LootEntry.class);
				ENTRIES.add(ent);
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load affix loot entry {}.", obj.getKey());
				e.printStackTrace();
			}
		}
		DeadlyModule.LOGGER.info("Loaded {} affix loot entries from resources.", ENTRIES.size());
	}

	public static List<LootEntry> getEntries() {
		return ENTRIES;
	}

	/**
	 * Selects a random loot entry itemstack from the list of entries.
	 * @param rand A random.
	 * @param rarity If this is {@link LootRarity#ANCIENT}, then the item returned will be an {@link Unique}
	 * @return A loot entry's stack, or a unique, if the rarity selected was ancient.
	 */
	public static ItemStack getRandomEntry(Random rand, LootRarity rarity) {
		LootEntry entry = WeightedRandom.getRandomItem(rand, ENTRIES);
		ItemStack stack = rarity == LootRarity.ANCIENT ? genUnique(rand) : entry.getStack().copy();
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
		AffixHelper.setRarity(stack, rarity);

		modifs.forEach((s, a) -> stack.addAttributeModifier(s, a, slot));

		if (type == EquipmentType.AXE) AffixHelper.applyAffix(stack, Affixes.PIERCING, Affixes.PIERCING.apply(stack, rand, null));

		List<Affix> afxList = AffixHelper.getAffixesFor(type);
		int affixCount = rarity.getAffixes();
		while (affixes.size() < Math.min(affixCount, afxList.size())) {
			affixes.put(WeightedRandom.getRandomItem(rand, afxList), rarity == LootRarity.COMMON ? rand.nextBoolean() ? Modifiers.MIN : Modifiers.HALF : null);
		}

		if (rarity.ordinal() >= LootRarity.EPIC.ordinal()) {
			float modifChance = rarity == LootRarity.EPIC ? 0.3F : 0.65F;
			for (Affix a : affixes.keySet()) {
				if (rand.nextFloat() <= modifChance) affixes.put(a, Modifiers.getRandomModifier(rand));
			}
		}

		for (Affix a : affixes.keySet()) {
			name = a.chainName(name, affixes.get(a));
			AffixHelper.applyAffix(stack, a, a.apply(stack, rand, affixes.get(a)));
		}

		if (rarity.ordinal() >= LootRarity.MYTHIC.ordinal()) {
			CompoundNBT tag = stack.getOrCreateTag();
			tag.putBoolean("Unbreakable", true);
		}

		stack.setDisplayName(new StringTextComponent(TextFormatting.RESET + rarity.getColor().toString() + name.getFormattedText().replace(TextFormatting.RESET.toString(), "")));
		return stack;
	}

	/**
	 * Creates a unique item. Should only be used during {@link genLootItem} when it rolls {@link LootRarity#UNIQUE}
	 */
	public static ItemStack genUnique(Random rand) {
		return ItemStack.EMPTY;
	}

}
