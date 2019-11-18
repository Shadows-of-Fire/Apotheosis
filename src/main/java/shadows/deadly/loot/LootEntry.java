package shadows.deadly.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public class LootEntry extends WeightedRandom.Item {

	protected final ItemStack stack;
	protected final Type type;

	public LootEntry(ItemStack stack, Type type, int weight) {
		super(weight);
		this.stack = stack;
		this.type = type;
	}

	public ItemStack getStack() {
		return stack;
	}

	public Type getType() {
		return type;
	}

	public static enum Type {
		ARMOR,
		WEAPON,
		SHIELD,
		TOOL,
		RANGED;
	}
}
