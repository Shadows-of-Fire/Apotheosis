package shadows.apotheosis.deadly.reload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.util.GearSet;
import shadows.placebo.util.json.ItemAdapter;
import shadows.placebo.util.json.NBTAdapter;

public class BossArmorManager extends JsonReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE).registerTypeAdapter(CompoundNBT.class, NBTAdapter.INSTANCE).setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName()).create();

	public static final BossArmorManager INSTANCE = new BossArmorManager();

	protected final Map<ResourceLocation, GearSet> registry = new HashMap<>();
	protected final List<GearSet> sets = new ArrayList<>();
	private int weight = 0;

	public BossArmorManager() {
		super(GSON, "boss_gear");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager mgr, IProfiler profiler) {
		this.sets.clear();
		this.registry.clear();
		objects.forEach((id, obj) -> {
			try {
				this.register(id, GSON.fromJson(obj, GearSet.class));
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load boss armor set {}.", id.toString());
				e.printStackTrace();
			}
		});
		if (this.registry.isEmpty()) throw new RuntimeException("No Apotheosis Boss armor sets were registered.  At least one is required.");
		DeadlyModule.LOGGER.info("Registered {} boss gear sets.", this.sets.size());
		this.weight = WeightedRandom.getTotalWeight(this.sets);
	}

	protected void register(ResourceLocation id, GearSet set) {
		if (!this.registry.containsKey(id)) {
			set.setId(id);
			this.registry.put(id, set);
			this.sets.add(set);
		} else DeadlyModule.LOGGER.error("Attempted to register a boss gear set with name {}, but it already exists!", id);
	}

	/**
	 * Returns a random weighted armor set based on the given random (and predicate, if applicable).
	 */
	public <T extends Predicate<GearSet>> GearSet getRandomSet(Random rand, @Nullable List<T> filter) {
		if (filter == null || filter.isEmpty()) return WeightedRandom.getRandomItem(rand, this.sets, this.weight);
		List<GearSet> valid = this.sets.stream().filter(e -> {
			for (Predicate<GearSet> f : filter)
				if (f.test(e)) return true;
			return false;
		}).collect(Collectors.toList());
		if (valid.isEmpty()) return WeightedRandom.getRandomItem(rand, this.sets, this.weight);
		return WeightedRandom.getRandomItem(rand, valid);
	}

}
