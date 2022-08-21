package shadows.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.util.IPerDimension;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.SerializerBuilder;
import shadows.placebo.json.WeightedJsonReloadListener;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootManager extends WeightedJsonReloadListener<AffixLootEntry> {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE)
			.registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.create();
	//Formatter::on

	public static final AffixLootManager INSTANCE = new AffixLootManager();

	private AffixLootManager() {
		super(AdventureModule.LOGGER, "affix_loot_entries", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<AffixLootEntry>("Affix Loot Entry").json(obj -> GSON.fromJson(obj, AffixLootEntry.class), e -> GSON.toJsonTree(e).getAsJsonObject()));
	}

	@Override
	protected <T extends AffixLootEntry> void register(ResourceLocation key, T item) {
		Preconditions.checkArgument(!item.stack.isEmpty());
		Preconditions.checkNotNull(item.type);
		super.register(key, item);
	}

	@Override
	@Deprecated
	public AffixLootEntry getRandomItem(Random rand) {
		return super.getRandomItem(rand);
	}

	/**
	 * @see {@link AffixLootManager#getRandomEntry(Random, LootCategory, float, ServerLevelAccessor)}
	 */
	@Nullable
	public static AffixLootEntry getRandomEntry(Random rand, float luck, ServerLevelAccessor level) {
		return getRandomEntry(rand, null, luck, level);
	}

	/**
	 * Selects a random AffixLootEntry from the entire pool, given the conditions.
	 * @param rand The Random
	 * @param type The type of object to select.  If null, it may select any type.
	 * @param luck The player's luck.
	 * @param level The world where the item is being generated.
	 * @return A random AffixLootEntry matching the criteria, or null, if no matches are available.
	 */
	@Nullable
	public static AffixLootEntry getRandomEntry(Random rand, @Nullable LootCategory type, float luck, ServerLevelAccessor level) {
		List<AffixLootEntry> filtered = INSTANCE.entries.stream().filter(IPerDimension.matches(level)).filter(p -> type == null || p.getType() == type).collect(Collectors.toList());
		if (luck == 0) return WeightedRandom.getRandomItem(rand, filtered).orElse(null);

		List<Wrapper<AffixLootEntry>> temp = new ArrayList<>(filtered.size());
		for (AffixLootEntry g : filtered) {
			temp.add(WeightedEntry.wrap(g, getModifiedWeight(g.weight, g.quality, luck)));
		}
		return WeightedRandom.getRandomItem(rand, temp).map(Wrapper::getData).orElse(null);
	}

	public static int getModifiedWeight(int weight, int quality, float luck) {
		return Math.max(0, (int) (quality * luck) + weight);
	}

}