package shadows.apotheosis.deadly.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import shadows.deadly.gen.BossItem.EquipmentType;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public class LootEntry extends WeightedRandom.Item {

	protected final ItemStack stack;
	protected final EquipmentType type;

	public LootEntry(ItemStack stack, EquipmentType type, int weight) {
		super(weight);
		this.stack = stack;
		this.type = type;
	}

	public ItemStack getStack() {
		return stack;
	}

	public EquipmentType getType() {
		return type;
	}

}
