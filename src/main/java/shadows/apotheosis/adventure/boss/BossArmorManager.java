package shadows.apotheosis.adventure.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.util.GearSet;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

public class BossArmorManager extends PlaceboJsonReloadListener<GearSet> {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE).registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE).setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName()).create();

	public static final BossArmorManager INSTANCE = new BossArmorManager();

	protected final List<GearSet> sets = new ArrayList<>();
	private volatile int weight = 0;

	public BossArmorManager() {
		super(AdventureModule.LOGGER, "boss_gear", false, false);
	}

	@Override
	protected void beginReload() {
		super.beginReload();
		this.sets.clear();
	}

	@Override
	protected void register(ResourceLocation id, GearSet set) {
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, set);
			this.sets.add(set);
		} else AdventureModule.LOGGER.error("Attempted to register a boss gear set with name {}, but it already exists!", id);
	}

	@Override
	protected void onReload() {
		super.onReload();
		this.weight = WeightedRandom.getTotalWeight(this.sets);
		if (this.weight == 0) throw new RuntimeException("The total boss armor weight is zero.  This is not supported.");
	}

	/**
	 * Returns a random weighted armor set based on the given random (and predicate, if applicable).
	 */
	public <T extends Predicate<GearSet>> GearSet getRandomSet(Random rand, @Nullable List<T> filter) {
		if (filter == null || filter.isEmpty()) return WeightedRandom.getRandomItem(rand, this.sets, this.weight).get();
		List<GearSet> valid = this.sets.stream().filter(e -> {
			for (Predicate<GearSet> f : filter)
				if (f.test(e)) return true;
			return false;
		}).collect(Collectors.toList());
		if (valid.isEmpty()) return WeightedRandom.getRandomItem(rand, this.sets, this.weight).get();
		return WeightedRandom.getRandomItem(rand, valid).get();
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<GearSet>("Boss Gear Set").withJsonDeserializer(obj -> GSON.fromJson(obj, GearSet.class)));
	}

}