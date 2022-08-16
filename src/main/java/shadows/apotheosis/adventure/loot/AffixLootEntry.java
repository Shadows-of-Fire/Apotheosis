package shadows.apotheosis.adventure.loot;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public class AffixLootEntry extends TypeKeyedBase<AffixLootEntry> implements WeightedEntry {

	protected final int weight;
	protected final int quality;
	protected final ItemStack stack;
	protected final LootCategory type;

	public AffixLootEntry(int weight, int quality, ItemStack stack, LootCategory type) {
		this.weight = weight;
		this.quality = quality;
		this.stack = stack;
		this.type = type;
	}

	@Override
	public Weight getWeight() {
		return Weight.of(weight);
	}

	public int getIntWeight() {
		return weight;
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

}