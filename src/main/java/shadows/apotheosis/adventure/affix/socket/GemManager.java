package shadows.apotheosis.adventure.affix.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.compat.GameStagesCompat;
import shadows.placebo.json.DimWeightedJsonReloadListener;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.json.SerializerBuilder;

public class GemManager extends DimWeightedJsonReloadListener<Gem> {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).registerTypeAdapter(Attribute.class, JsonUtil.makeSerializer(ForgeRegistries.ATTRIBUTES)).create();
	public static final GemManager INSTANCE = new GemManager();

	private static List<Runnable> loadCallbacks = new ArrayList<>(); // TODO: Replace with more complete solution in PlaceboJsonReloadListener.

	public GemManager() {
		super(AdventureModule.LOGGER, "gems", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<Gem>("Gem").withJsonDeserializer(obj -> GSON.fromJson(obj, Gem.class)));
	}

	public static ItemStack getRandomGemStack(Random rand, Player player, ServerLevelAccessor level) {
		return GemItem.fromGem(INSTANCE.getRandomItem(rand, player, level), rand);
	}

	@Override
	protected void onReload() {
		super.onReload();
		loadCallbacks.forEach(Runnable::run);
	}

	public static void registerCallback(Runnable r) {
		loadCallbacks.add(r);
	}

	public Gem getRandomItem(Random rand, Player player, ServerLevelAccessor level) {
		List<Wrapper<Gem>> list = new ArrayList<>(zeroLuckList.size());
		this.registry.values().stream().filter(IDimWeighted.matches(level.getLevel().dimension().location())).filter(i -> GameStagesCompat.hasStage(player, i.stages)).map(l -> l.<Gem>wrap(player.getLuck())).forEach(list::add);
		return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).orElse(null);
	}

}
