package shadows.apotheosis.deadly.reload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Unique;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.AffixLootEntry;
import shadows.apotheosis.deadly.affix.Affixes;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.LootRarity;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.affix.modifiers.Modifiers;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.util.JsonUtil;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootManager extends SimpleJsonResourceReloadListener {

	public static final Gson GSON = BossArmorManager.GSON;

	public static final AffixLootManager INSTANCE = new AffixLootManager();

	private static final List<AffixLootEntry> ENTRIES = new ArrayList<>();

	private volatile int weight = 0;

	private AffixLootManager() {
		super(GSON, "affix_loot_entries");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
		ENTRIES.clear();
		for (Entry<ResourceLocation, JsonElement> obj : objects.entrySet()) {
			try {
				if (JsonUtil.checkAndLogEmpty(obj.getValue(), obj.getKey(), "Affix Loot Entry", DeadlyModule.LOGGER)) continue;
				AffixLootEntry ent = GSON.fromJson(obj.getValue(), AffixLootEntry.class);
				ENTRIES.add(ent);
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load affix loot entry {}.", obj.getKey());
				e.printStackTrace();
			}
		}
		Collections.shuffle(ENTRIES);
		this.weight = WeightedRandom.getTotalWeight(ENTRIES);
		if (this.weight == 0) throw new RuntimeException("The total affix item weight is zero.  This is not supported.");
		DeadlyModule.LOGGER.info("Loaded {} affix loot entries from resources.", ENTRIES.size());
	}

	public static List<AffixLootEntry> getEntries() {
		return ENTRIES;
	}

	/**
	 * Selects a random loot entry itemstack from the list of entries.
	 * @param rand A random.
	 * @return A loot entry's stack, or a unique, if the rarity selected was ancient.
	 */
	public static AffixLootEntry getRandomEntry(Random rand) {
		return WeightedRandom.getRandomItem(rand, ENTRIES, INSTANCE.weight).get();
	}

	/**
	 * Selects a random loot entry itemstack from the list of entries, filtered by type.
	 * @param rand A random.
	 * @param rarity If this is {@link LootRarity#ANCIENT}, then the item returned will be an {@link Unique}
	 * @return A loot entry's stack, or a unique, if the rarity selected was ancient.
	 */
	public static AffixLootEntry getRandomEntry(Random rand, EquipmentType type) {
		if (type == null) return getRandomEntry(rand);
		return WeightedRandom.getRandomItem(rand, ENTRIES.stream().filter(p -> p.getType() == type).collect(Collectors.toList())).get();
	}

	/**
	 * Applies loot modifiers to the passed in itemstack.
	 * Note that this will be unusual if the passed in itemstack does not meet the qualities of any equipment type.
	 * The default equipment type is {@link EquipmentType#TOOL}, so items that do not match will be treated as tools.
	 */
	public static ItemStack genLootItem(ItemStack stack, Random rand, EquipmentType type, LootRarity rarity) {
		Component name = stack.getHoverName();
		if (type == null) {
			AffixHelper.addLore(stack, new TextComponent("ERROR - ATTEMPTED TO GENERATE LOOT ITEM WITH INVALID EQUIPMENT TYPE."));
			return stack;
		}
		Map<Affix, AffixModifier> affixes = new HashMap<>();
		AffixHelper.setRarity(stack, rarity);

		if (type == EquipmentType.AXE) AffixHelper.applyAffix(stack, Affixes.PIERCING, Affixes.PIERCING.generateLevel(stack, rand, null));

		List<Affix> afxList = AffixHelper.getAffixesFor(type);
		int affixCount = rarity.getAffixes();
		while (affixes.size() < Math.min(affixCount, afxList.size())) {
			affixes.put(WeightedRandom.getRandomItem(rand, afxList).get(), rarity == LootRarity.COMMON ? Modifiers.getBadModifier() : null);
		}

		if (rarity.ordinal() >= LootRarity.EPIC.ordinal()) {
			float modifChance = rarity == LootRarity.EPIC ? 0.3F : 0.65F;
			for (Affix a : affixes.keySet()) {
				if (rand.nextFloat() <= modifChance) affixes.put(a, Modifiers.getRandomModifier(rand));
			}
		}

		for (Affix a : affixes.keySet()) {
			name = a.chainName(name, affixes.get(a));
			AffixHelper.applyAffix(stack, a, a.generateLevel(stack, rand, affixes.get(a)));
		}

		if (rarity.ordinal() >= LootRarity.MYTHIC.ordinal() && DeadlyConfig.mythicUnbreakable) {
			CompoundTag tag = stack.getOrCreateTag();
			tag.putBoolean("Unbreakable", true);
		}

		TextColor color = rarity.getColor();
		stack.setHoverName(new TranslatableComponent("%s", (((MutableComponent) name).withStyle(Style.EMPTY)).withStyle(Style.EMPTY.withColor(color))));
		return stack;
	}

	/**
	 * Creates a unique item. Should only be used during {@link genLootItem} when it rolls {@link LootRarity#UNIQUE}
	 */
	public static ItemStack genUnique(Random rand) {
		return ItemStack.EMPTY;
	}

}