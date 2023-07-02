package shadows.apotheosis.adventure.loot;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.placebo.codec.PlaceboCodecs;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.TypeKeyed.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public final class AffixLootEntry extends TypeKeyedBase<AffixLootEntry> implements ILuckyWeighted, IDimensional, LootRarity.Clamped, IStaged {

	//Formatter::off
	public static final Codec<AffixLootEntry> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("quality", 0F).forGetter(ILuckyWeighted::getQuality),
			ItemAdapter.CODEC.fieldOf("stack").forGetter(a -> a.stack),
			PlaceboCodecs.setOf(ResourceLocation.CODEC).fieldOf("dimensions").forGetter(a -> a.dimensions),
			LootRarity.CODEC.optionalFieldOf("min_rarity", LootRarity.COMMON).forGetter(a -> a.minRarity),
			LootRarity.CODEC.optionalFieldOf("max_rarity", LootRarity.MYTHIC).forGetter(a -> a.maxRarity),
			PlaceboCodecs.setOf(Codec.STRING).optionalFieldOf("stages").forGetter(a -> Optional.ofNullable(a.stages)))
		.apply(inst, AffixLootEntry::new)
	);
	//Formatter::on
	public static final PSerializer<AffixLootEntry> SERIALIZER = PSerializer.fromCodec("Affix Loot Entry", CODEC);

	protected final int weight;
	protected final float quality;
	protected final ItemStack stack;
	protected final Set<ResourceLocation> dimensions;
	protected final LootRarity minRarity;
	protected final LootRarity maxRarity;
	protected final @Nullable Set<String> stages;

	public AffixLootEntry(int weight, float quality, ItemStack stack, Set<ResourceLocation> dimensions, LootRarity min, LootRarity max, Optional<Set<String>> stages) {
		this.weight = weight;
		this.quality = quality;
		this.stack = stack;
		this.dimensions = dimensions;
		this.minRarity = min;
		this.maxRarity = max;
		this.stages = stages.orElse(null);
	}

	public AffixLootEntry(int weight, float quality, ItemStack stack, Set<ResourceLocation> dimensions, LootRarity min, LootRarity max) {
		this(weight, quality, stack, dimensions, min, max, Optional.empty());
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	public ItemStack getStack() {
		return this.stack.copy();
	}

	@Override
	public Set<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

	@Override
	public LootRarity getMinRarity() {
		return this.minRarity;
	}

	@Override
	public LootRarity getMaxRarity() {
		return this.maxRarity;
	}

	public LootCategory getType() {
		return LootCategory.forItem(this.stack);
	}

	@Override
	public Set<String> getStages() {
		return this.stages;
	}

	@Override
	public PSerializer<? extends AffixLootEntry> getSerializer() {
		return SERIALIZER;
	}

}