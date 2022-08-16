package shadows.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootManager extends PlaceboJsonReloadListener<AffixLootEntry> {

	public static final AffixLootManager INSTANCE = new AffixLootManager();

	protected List<AffixLootEntry> list = new ArrayList<>();
	protected int totalWeight = 0;

	private AffixLootManager() {
		super(AdventureModule.LOGGER, "affix_loot_entries", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<AffixLootEntry>("Affix Loot Entry").json(obj -> ItemAdapter.ITEM_READER.fromJson(obj, AffixLootEntry.class), e -> ItemAdapter.ITEM_READER.toJsonTree(e).getAsJsonObject()));
	}

	@Override
	protected void beginReload() {
		super.beginReload();
		this.list.clear();
	}

	@Override
	protected <T extends AffixLootEntry> void register(ResourceLocation key, T item) {
		Preconditions.checkArgument(!item.stack.isEmpty());
		Preconditions.checkNotNull(item.type);
		super.register(key, item);
	}

	@Override
	protected void onReload() {
		super.onReload();
		this.list.addAll(this.getValues());
		totalWeight = WeightedRandom.getTotalWeight(this.list);
	}

	/**
	 * Selects a random loot entry itemstack from the list of entries.
	 * @param rand A random.
	 * @return A loot entry's stack, or a unique, if the rarity selected was ancient.
	 */
	public static AffixLootEntry getRandomEntry(Random rand, float luck) {
		if (luck == 0) return WeightedRandom.getRandomItem(rand, INSTANCE.list, INSTANCE.totalWeight).get();
		List<Wrapper<AffixLootEntry>> temp = new ArrayList<>(INSTANCE.list.size());
		for (AffixLootEntry g : INSTANCE.list) {
			temp.add(WeightedEntry.wrap(g, getModifiedWeight(g.weight, g.quality, luck)));
		}
		return WeightedRandom.getRandomItem(rand, temp).get().getData();
	}

	/**
	 * Selects a random loot entry itemstack from the list of entries, filtered by type.
	 * @param rand A random.
	 * @param rarity If this is {@link LootRarity#ANCIENT}, then the item returned will be an {@link Unique}
	 * @return A loot entry's stack, or a unique, if the rarity selected was ancient.
	 */
	public static AffixLootEntry getRandomEntry(Random rand, LootCategory type, float luck) {
		if (type == null) return getRandomEntry(rand, luck);
		List<AffixLootEntry> filtered = INSTANCE.list.stream().filter(p -> p.getType() == type).collect(Collectors.toList());
		if (luck == 0) return WeightedRandom.getRandomItem(rand, filtered).get();

		List<Wrapper<AffixLootEntry>> temp = new ArrayList<>(filtered.size());
		for (AffixLootEntry g : filtered) {
			temp.add(WeightedEntry.wrap(g, getModifiedWeight(g.weight, g.quality, luck)));
		}
		return WeightedRandom.getRandomItem(rand, temp).get().getData();
	}

	public static int getModifiedWeight(int weight, int quality, float luck) {
		return Math.max(0, (int) (quality * luck) + weight);
	}

}