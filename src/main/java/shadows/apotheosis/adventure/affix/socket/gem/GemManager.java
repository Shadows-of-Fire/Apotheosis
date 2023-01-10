package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.Gems;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.WeightedJsonReloadListener;

public class GemManager extends WeightedJsonReloadListener<Gem> {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).registerTypeAdapter(Attribute.class, JsonUtil.makeSerializer(ForgeRegistries.ATTRIBUTES)).registerTypeAdapter(LootRarity.class, new LootRarity.Serializer()).create();
	public static final GemManager INSTANCE = new GemManager();

	public GemManager() {
		super(AdventureModule.LOGGER, "gems", true, true);
	}

	@Override
	@SuppressWarnings("removal")
	protected void registerBuiltinSerializers() {
		this.registerSerializer(Apotheosis.loc("stat"), PSerializer.autoRegister("Stat Gem", StatGem.class));
		this.registerSerializer(Apotheosis.loc("legacy"), PSerializer.builtin("Legacy Gem", () -> LegacyGem.INSTANCE));
	}

	@Override
	@SuppressWarnings("removal")
	protected void onReload() {
		super.onReload();
		Preconditions.checkArgument(Gems.LEGACY.get() instanceof LegacyGem, "Legacy Gem not registered!");
	}

	/**
	 * Pulls a random LootRarity and Gem, and generates an Gem Item
	 * @param rand Random
	 * @param rarity The rarity, or null if it should be randomly selected.
	 * @param luck The player's luck level
	 * @param filter The filter
	 * @return A gem item, or an empty ItemStack if no entries were available for the dimension.
	 */
	public static ItemStack createRandomGemStack(RandomSource rand, @Nullable LootRarity rarity, float luck, Predicate<Gem> filter) {
		Gem gem = GemManager.INSTANCE.getRandomItem(rand, luck, filter);
		if (gem == null) return ItemStack.EMPTY;
		return createGemStack(gem, rand, rarity, luck);
	}

	public static ItemStack createGemStack(Gem gem, RandomSource rand, @Nullable LootRarity rarity, float luck) {
		ItemStack stack = new ItemStack(Apoth.Items.GEM.get());
		GemItem.setGem(stack, gem);
		GemItem.setVariant(stack, gem.getVariant());
		if (rarity == null) rarity = LootRarity.random(rand, luck, gem);
		GemItem.setLootRarity(stack, rarity);
		int facets = rand.nextInt(gem.getMaxFacets(stack, rarity) + 1);
		GemItem.setFacets(stack, facets);
		return stack;
	}

}
