package shadows.apotheosis.deadly.loot;

import net.minecraft.item.ItemStack;
import shadows.apotheosis.deadly.gen.BossItem.EquipmentType;

/**
 * A Unique is a loot drop with rarity equal to {@link LootRarity#UNIQUE}
 * It has special definitions that make it truly unique, and is less based on the underlying item.
 */
public abstract class Unique extends LootEntry {

	public Unique(ItemStack stack, EquipmentType type, int weight) {
		super(stack, type, weight);
	}

	/**
	 * Creates an itemstack of the unique represented by this entry.
	 */
	public abstract ItemStack makeStack();
}
