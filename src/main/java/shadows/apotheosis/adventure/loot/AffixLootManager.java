package shadows.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.compat.GameStagesCompat;
import shadows.placebo.json.DimWeightedJsonReloadListener;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.SerializerBuilder;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootManager extends DimWeightedJsonReloadListener<AffixLootEntry> {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE)
			.registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(LootRarity.class, new LootRarity.Serializer())
			.setPrettyPrinting().create();
	//Formatter::on

	public static final AffixLootManager INSTANCE = new AffixLootManager();

	private static List<Runnable> loadCallbacks = new ArrayList<>(); // TODO: Replace with more complete solution in PlaceboJsonReloadListener.

	private AffixLootManager() {
		super(AdventureModule.LOGGER, "affix_loot_entries", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<AffixLootEntry>("Affix Loot Entry").json(obj -> GSON.fromJson(obj, AffixLootEntry.class), e -> GSON.toJsonTree(e).getAsJsonObject()));
	}

	@Override
	protected void validateItem(AffixLootEntry item) {
		super.validateItem(item);
		Preconditions.checkArgument(!item.stack.isEmpty());
		Preconditions.checkArgument(item.type != null);
		Preconditions.checkArgument(item.type != LootCategory.NONE);
	}

	@Override
	protected void onReload() {
		super.onReload();
		loadCallbacks.forEach(Runnable::run);
	}

	public static void registerCallback(Runnable r) {
		loadCallbacks.add(r);
	}

	public AffixLootEntry getRandomItem(Random rand, Player player, ServerLevelAccessor level) {
		List<Wrapper<AffixLootEntry>> list = new ArrayList<>(zeroLuckList.size());
		this.registry.values().stream().filter(IDimWeighted.matches(level.getLevel().dimension().location())).filter(i -> GameStagesCompat.hasStage(player, i.stages)).map(l -> l.<AffixLootEntry>wrap(player.getLuck())).forEach(list::add);
		return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).orElse(null);
	}

}