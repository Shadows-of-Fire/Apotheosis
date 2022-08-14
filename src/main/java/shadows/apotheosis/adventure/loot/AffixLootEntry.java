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
	protected final ItemStack stack;
	protected final LootCategory type;

	public AffixLootEntry(int weight, ItemStack stack, LootCategory type) {
		this.weight = weight;
		this.stack = stack;
		this.type = type;
	}

	@Override
	public Weight getWeight() {
		return Weight.of(weight);
	}

	public ItemStack getStack() {
		return this.stack;
	}

	public LootCategory getType() {
		return this.type;
	}

}