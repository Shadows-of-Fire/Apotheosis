package shadows.apotheosis.deadly.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.util.GearSet;
import shadows.apotheosis.util.json.ItemAdapter;
import shadows.apotheosis.util.json.NBTAdapter;

public class BossArmorManager extends JsonReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE).registerTypeAdapter(CompoundNBT.class, NBTAdapter.INSTANCE).setFieldNamingStrategy(f -> f.getName().equals("itemWeight") ? "weight" : f.getName()).create();

	public static final BossArmorManager INSTANCE = new BossArmorManager();

	protected final Map<ResourceLocation, GearSet> registry = new HashMap<>();
	protected final List<GearSet> sets = new ArrayList<>();

	public BossArmorManager() {
		super(GSON, "boss_gear");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> objects, IResourceManager mgr, IProfiler profiler) {
		sets.clear();
		registry.clear();
		objects.forEach((id, obj) -> {
			try {
				register(id, GSON.fromJson(obj, GearSet.class));
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load boss armor set {}.", id.toString());
				e.printStackTrace();
			}
		});
		if (registry.isEmpty()) throw new RuntimeException("No Apotheosis Boss armor sets were registered.  At least one is required.");
		else DeadlyModule.LOGGER.info("Registered {} boss gear sets.", sets.size());
	}

	public void register(ResourceLocation id, GearSet set) {
		if (!registry.containsKey(id)) {
			registry.put(id, set);
			sets.add(set);
		} else DeadlyModule.LOGGER.error("Attempted to register an ArmorSet with name {}, but it already exists!", id);
	}

	public GearSet getRandomSet(Random random) {
		return WeightedRandom.getRandomItem(random, sets);
	}

}
