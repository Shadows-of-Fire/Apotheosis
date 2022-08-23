package shadows.apotheosis.adventure.loot;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shadows.placebo.json.DimWeightedJsonReloadListener.IDimWeighted;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public class AffixLootEntry extends TypeKeyedBase<AffixLootEntry> implements IDimWeighted {

	protected final int weight;
	protected final float quality;
	protected final ItemStack stack;
	protected final LootCategory type;
	protected final Set<ResourceLocation> dimensions;

	public AffixLootEntry(int weight, float quality, ItemStack stack, LootCategory type, Set<ResourceLocation> dimensions) {
		this.weight = weight;
		this.quality = quality;
		this.stack = stack;
		this.type = type;
		this.dimensions = dimensions;
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

	public LootCategory getType() {
		return this.type;
	}

	@Override
	public Set<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

}