package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.Gems;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.AttributeBonus;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.DamageReductionBonus;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.DurabilityBonus;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.EnchantmentBonus;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.MultiAttrBonus;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.codec.PlaceboCodecs;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.WeightedJsonReloadListener;

@SuppressWarnings("removal")
public class GemManager extends WeightedJsonReloadListener<Gem> {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).registerTypeAdapter(Attribute.class, JsonUtil.makeSerializer(ForgeRegistries.ATTRIBUTES)).registerTypeAdapter(LootRarity.class, new LootRarity.Serializer()).create();
	public static final GemManager INSTANCE = new GemManager();

	protected final Map<ResourceLocation, Codec<? extends GemBonus>> gemBonusCodecs = new HashMap<>();
	protected final BiMap<ResourceLocation, PSerializer<? extends GemBonus>> gemBonusSerializers = HashBiMap.create();

	public GemManager() {
		super(AdventureModule.LOGGER, "gems", true, true);
		registerBonusCodecs();
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, PSerializer.fromCodec("Gem", Gem.CODEC));
		this.registerSerializer(Apotheosis.loc("legacy"), PSerializer.builtin("Legacy Gem", () -> LegacyGem.INSTANCE));
	}

	protected void registerBonusCodecs() {
		this.registerBonusCodec(Apotheosis.loc("attribute"), AttributeBonus.CODEC);
		this.registerBonusCodec(Apotheosis.loc("multi_attribute"), MultiAttrBonus.CODEC);
		this.registerBonusCodec(Apotheosis.loc("durability"), DurabilityBonus.CODEC);
		this.registerBonusCodec(Apotheosis.loc("damage_reduction"), DamageReductionBonus.CODEC);
		this.registerBonusCodec(Apotheosis.loc("enchantment"), EnchantmentBonus.CODEC);
	}

	@Override
	protected void onReload() {
		super.onReload();
		Preconditions.checkArgument(Gems.LEGACY.get() instanceof LegacyGem, "Legacy Gem not registered!");
	}

	public final void registerBonusCodec(ResourceLocation id, Codec<? extends GemBonus> codec) {
		if (this.gemBonusSerializers.containsKey(id)) throw new RuntimeException("Attempted to register a gem bonus serializer with id " + id + " but one already exists!");
		this.gemBonusCodecs.put(id, codec);
		this.gemBonusSerializers.put(id, PSerializer.fromCodec(id.toString(), codec).build(this.synced));
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
		if (rarity == null) rarity = LootRarity.random(rand, luck, gem);
		GemItem.setLootRarity(stack, rarity);
		int facets = rand.nextInt(gem.getMaxFacets(rarity) + 1);
		GemItem.setFacets(stack, facets);
		return stack;
	}

	private final Codec<GemBonus> gemBonusCodec = PlaceboCodecs.mapBacked("Gem Bonus", this.gemBonusCodecs, GemBonus::getId);

	public static Codec<GemBonus> gemBonusCodec() {
		return ExtraCodecs.lazyInitializedCodec(() -> INSTANCE.gemBonusCodec);
	}

}
