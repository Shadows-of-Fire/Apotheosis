package shadows.apotheosis.adventure.affix.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.loot.AffixLootManager;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.RandomAttributeModifier;
import shadows.placebo.json.SerializerBuilder;

public class GemManager extends PlaceboJsonReloadListener<Gem> {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(RandomAttributeModifier.class, new RandomAttributeModifier.Deserializer()).create();
	public static final GemManager INSTANCE = new GemManager();

	protected List<Gem> gemList = new ArrayList<>();
	protected int totalWeight = 0;

	public GemManager() {
		super(AdventureModule.LOGGER, "gems", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<Gem>("Gem").json(Gem::fromJson, Gem::toJson));
	}

	@Override
	protected void beginReload() {
		super.beginReload();
		this.gemList.clear();
	}

	@Override
	protected void onReload() {
		super.onReload();
		this.gemList.addAll(this.getValues());
		totalWeight = WeightedRandom.getTotalWeight(this.gemList);
	}

	public static Gem getRandomGem(Random rand, float luck) {
		if (luck == 0) return WeightedRandom.getRandomItem(rand, INSTANCE.gemList, INSTANCE.totalWeight).get();
		else {
			List<Wrapper<Gem>> temp = new ArrayList<>(INSTANCE.gemList.size());
			for (Gem g : INSTANCE.gemList) {
				temp.add(WeightedEntry.wrap(g, AffixLootManager.getModifiedWeight(g.weight, g.quality, luck)));
			}
			return WeightedRandom.getRandomItem(rand, temp).get().getData();
		}
	}

	public static ItemStack getRandomGemStack(Random rand, float luck) {
		return GemItem.fromGem(getRandomGem(rand, luck), rand);
	}

}
