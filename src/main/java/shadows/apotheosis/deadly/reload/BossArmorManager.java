package shadows.apotheosis.deadly.reload;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.coremod.api.ASMAPI;
import org.jetbrains.annotations.NotNull;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.util.GearSet;
import shadows.apotheosis.util.JsonUtil;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

public class BossArmorManager extends PlaceboJsonReloadListener<GearSet> {

	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE)
			.registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
			.create();

	public static final BossArmorManager INSTANCE = new BossArmorManager();

	protected final List<GearSet> sets = new ArrayList<>();
	private volatile int weight = 0;

	public BossArmorManager() {
		super(DeadlyModule.LOGGER, "boss_gear", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(
				DEFAULT,
				new SerializerBuilder<GearSet>("GearSet")
						.withJsonDeserializer(json -> GSON.fromJson(json, GearSet.class)));
	}

	@Override
	protected <T extends GearSet> void register(ResourceLocation id, T set) {
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, set);
			this.sets.add(set);
		} else DeadlyModule.LOGGER.error("Attempted to register a boss gear set with name {}, but it already exists!", id);
	}

	@Override
	protected void onReload()
	{
		super.onReload();
		if (this.sets.size() == 0) this.logger.error("No boss GearSet(s)  were registered.  This is not supported.");
	}


	/**
	 * Returns a random weighted armor set based on the given random (and predicate, if applicable).
	 * @return
	 */
	public <T extends Predicate<GearSet>> Optional<GearSet> getRandomSet(Random rand, @Nullable List<T> filter) {
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
