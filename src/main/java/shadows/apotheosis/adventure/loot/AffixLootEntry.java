package shadows.apotheosis.adventure.loot;

import java.util.Set;

import com.google.gson.annotations.Expose;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.util.IPerDimension;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public class AffixLootEntry extends TypeKeyedBase<AffixLootEntry> implements WeightedEntry, IPerDimension {

	@Expose(deserialize = false)
	private Weight _weight;
	protected final int weight;
	protected final int quality;
	protected final ItemStack stack;
	protected final LootCategory type;
	protected final Set<ResourceLocation> dimensions;

	public AffixLootEntry(int weight, int quality, ItemStack stack, LootCategory type, Set<ResourceLocation> dimensions) {
		this.weight = weight;
		this.quality = quality;
		this.stack = stack;
		this.type = type;
		this.dimensions = dimensions;
	}

	@Override
	public Weight getWeight() {
		if (this._weight == null) this._weight = Weight.of(this.weight);
		return this._weight;
	}

	public int getIntWeight() {
		return this.weight;
	}

	public int getQuality() {
		return this.quality;
	}

	public ItemStack getStack() {
		return this.stack.copy();
	}

	public LootCategory getType() {
		return this.type;
	}

	public Set<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

}