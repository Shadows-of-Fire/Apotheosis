package shadows.apotheosis.adventure.loot;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public final class AffixLootEntry extends TypeKeyedBase<AffixLootEntry> implements ILuckyWeighted, IDimensional, LootRarity.Clamped, IStaged {

	protected int weight;
	protected float quality;
	protected ItemStack stack;
	protected Set<ResourceLocation> dimensions;
	@SerializedName("min_rarity")
	protected LootRarity minRarity;
	@SerializedName("max_rarity")
	protected LootRarity maxRarity;
	protected @Nullable Set<String> stages;

	public AffixLootEntry(int weight, float quality, ItemStack stack, Set<ResourceLocation> dimensions, LootRarity min, LootRarity max) {
		this.weight = weight;
		this.quality = quality;
		this.stack = stack;
		this.dimensions = dimensions;
		this.minRarity = min;
		this.maxRarity = max;
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

}