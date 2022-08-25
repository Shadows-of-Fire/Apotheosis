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
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.util.GearSet;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.SerializerBuilder;
import shadows.placebo.json.WeightedJsonReloadListener;

public class BossArmorManager extends WeightedJsonReloadListener<GearSet> {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE).registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE).setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName()).create();

	public static final BossArmorManager INSTANCE = new BossArmorManager();

	public BossArmorManager() {
		super(AdventureModule.LOGGER, "boss_gear", false, false);
	}

	/**
	 * Returns a random weighted armor set based on the given random (and predicate, if applicable).
	 */
	public <T extends Predicate<GearSet>> GearSet getRandomSet(Random rand, float luck, @Nullable List<T> filter) {
		if (filter == null || filter.isEmpty()) return this.getRandomItem(rand, luck);
		List<GearSet> valid = this.registry.values().stream().filter(e -> {
			for (Predicate<GearSet> f : filter)
				if (f.test(e)) return true;
			return false;
		}).collect(Collectors.toList());
		if (valid.isEmpty()) return this.getRandomItem(rand, luck);

		List<Wrapper<GearSet>> list = new ArrayList<>(valid.size());
		valid.stream().map(l -> l.<GearSet>wrap(luck)).forEach(list::add);
		return WeightedRandom.getRandomItem(rand, list).get().getData();
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<GearSet>("Boss Gear Set").withJsonDeserializer(obj -> GSON.fromJson(obj, GearSet.class)));
	}

}