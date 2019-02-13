package shadows.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.WeightedRandom;
import shadows.placebo.util.PlaceboUtil;

/**
 * Util class to contain the full equipment for an entity.
 * @author Shadows
 *
 */
public class ArmorSet {

	public static final Map<Integer, ArmorSet> LEVEL_TO_SETS = new HashMap<>();
	public static final List<ArmorSet> SORTED_SETS = new ArrayList<>();

	final int level;

	ItemStack mainhand;
	ItemStack offhand;
	ItemStack feet;
	ItemStack legs;
	ItemStack chest;
	ItemStack head;

	EnumMap<EntityEquipmentSlot, ItemStack> slotMap = new EnumMap<>(EntityEquipmentSlot.class);
	List<WeightedRandomStack> possibleMainhands = new ArrayList<>();

	/**
	 * Construcs an armor set, using the order of EntityEquipmentSlot.
	 * @param set A 6-length ItemStack array, ordered to match EntityEquipmentSlot.  Stacks may be empty.
	 */
	public ArmorSet(int level, ItemStack... set) {
		this.level = level;
		mainhand = set[0];
		offhand = set[1];
		feet = set[2];
		legs = set[3];
		chest = set[4];
		head = set[5];
		for (int i = 0; i < 6; i++)
			slotMap.put(EntityEquipmentSlot.values()[i], set[i]);
	}

	/**
	 * A helper constructor that will take either ItemStack, Block, or Item.
	 * @param set A 6-length ItemStack/Item/Block array, ordered to match EntityEquipmentSlot.  Stacks may be empty, may not pass null.
	 */
	public ArmorSet(int level, Object... set) {
		this(level, PlaceboUtil.toStackArray(set));
	}

	/**
	 * Makes the entity wear this armor set.  Returns the entity for convenience.
	 */
	public EntityLivingBase apply(EntityLivingBase entity) {
		for (EntityEquipmentSlot e : EntityEquipmentSlot.values())
			entity.setItemStackToSlot(e, slotMap.get(e).copy());
		if (!possibleMainhands.isEmpty()) entity.setHeldItem(EnumHand.MAIN_HAND, WeightedRandom.getRandomItem(ThreadLocalRandom.current(), possibleMainhands).getStack().copy());
		return entity;
	}

	public NBTTagCompound apply(NBTTagCompound entity) {
		ItemStack main = mainhand;
		if (!possibleMainhands.isEmpty()) main = WeightedRandom.getRandomItem(ThreadLocalRandom.current(), possibleMainhands).getStack();
		return TagBuilder.setEquipment(entity, main, offhand, feet, legs, chest, head);
	}

	public ArmorSet addExtraMains(ItemStack... tools) {
		setupList();
		for (ItemStack s : tools)
			possibleMainhands.add(new WeightedRandomStack(s, 1));
		return this;
	}

	public ArmorSet addExtraMains(Object... tools) {
		return addExtraMains(PlaceboUtil.toStackArray(tools));
	}

	public List<WeightedRandomStack> getPossibleMainhands() {
		return possibleMainhands;
	}

	public void setupList() {
		if (possibleMainhands.isEmpty()) {
			possibleMainhands.add(new WeightedRandomStack(mainhand, 3));
		}
	}

	public static class WeightedRandomStack extends WeightedRandom.Item {

		final ItemStack stack;

		public WeightedRandomStack(ItemStack stack, int weight) {
			super(weight);
			this.stack = stack;
		}

		public ItemStack getStack() {
			return stack;
		}
	}

	public static void sortSets() {
		List<ArmorSet> sorted = LEVEL_TO_SETS.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).map(a -> a.getValue()).collect(Collectors.toList());
		SORTED_SETS.clear();
		SORTED_SETS.addAll(sorted);
	}

}
