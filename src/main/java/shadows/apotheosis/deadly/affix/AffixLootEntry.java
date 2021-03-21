package shadows.apotheosis.deadly.affix;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public class AffixLootEntry extends WeightedRandom.Item {

	protected final ItemStack stack;
	protected final EquipmentType type;

	public AffixLootEntry(ItemStack stack, EquipmentType type, int weight) {
		super(weight);
		this.stack = stack;
		this.type = type;
	}

	public ItemStack getStack() {
		return this.stack;
	}

	public EquipmentType getType() {
		return this.type;
	}

}