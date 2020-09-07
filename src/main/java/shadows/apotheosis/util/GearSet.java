package shadows.apotheosis.util;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

/**
 * Util class to contain the full equipment for an entity.
 * @author Shadows
 *
 */
public class GearSet extends WeightedRandom.Item {

	protected final List<WeightedItemStack> mainhands;
	protected final List<WeightedItemStack> offhands;
	protected final List<WeightedItemStack> boots;
	protected final List<WeightedItemStack> leggings;
	protected final List<WeightedItemStack> chestplates;
	protected final List<WeightedItemStack> helmets;

	public GearSet(int weight, List<WeightedItemStack> mainhands, List<WeightedItemStack> offhands, List<WeightedItemStack> boots, List<WeightedItemStack> leggings, List<WeightedItemStack> chestplates, List<WeightedItemStack> helmets) {
		super(weight);
		this.mainhands = mainhands;
		this.offhands = offhands;
		this.boots = boots;
		this.leggings = leggings;
		this.chestplates = chestplates;
		this.helmets = helmets;
	}

	/**
	 * Makes the entity wear this armor set.  Returns the entity for convenience.
	 */
	public LivingEntity apply(LivingEntity entity) {
		entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, getRandomStack(mainhands, entity.rand));
		entity.setItemStackToSlot(EquipmentSlotType.OFFHAND, getRandomStack(offhands, entity.rand));
		entity.setItemStackToSlot(EquipmentSlotType.FEET, getRandomStack(boots, entity.rand));
		entity.setItemStackToSlot(EquipmentSlotType.LEGS, getRandomStack(leggings, entity.rand));
		entity.setItemStackToSlot(EquipmentSlotType.CHEST, getRandomStack(chestplates, entity.rand));
		entity.setItemStackToSlot(EquipmentSlotType.HEAD, getRandomStack(helmets, entity.rand));
		return entity;
	}

	/**
	 * Returns a copy of a random itemstack in this list of stacks.
	 */
	public static ItemStack getRandomStack(List<WeightedItemStack> stacks, Random random) {
		if (stacks.isEmpty()) return ItemStack.EMPTY;
		return WeightedRandom.getRandomItem(random, stacks).getStack().copy();
	}

	public static class WeightedItemStack extends WeightedRandom.Item {

		final ItemStack stack;

		public WeightedItemStack(ItemStack stack, int weight) {
			super(weight);
			this.stack = stack;
		}

		public ItemStack getStack() {
			return stack;
		}

		@Override
		public String toString() {
			return "Stack: " + stack.toString() + " @ Weight: " + itemWeight;
		}
	}
}
