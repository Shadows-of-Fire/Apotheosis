package shadows.apotheosis.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.placebo.util.PlaceboUtil;

/**
 * Util class to contain the full equipment for an entity.
 * @author Shadows
 *
 */
public class ArmorSet {

	private static final Multimap<Integer, ArmorSet> LEVEL_TO_SETS = HashMultimap.create();
	private static final Map<ResourceLocation, ArmorSet> REGISTRY = new HashMap<>();
	private static int maxLevel = 0;

	final int level;
	final ResourceLocation name;

	ItemStack mainhand;
	ItemStack offhand;
	ItemStack feet;
	ItemStack legs;
	ItemStack chest;
	ItemStack head;

	EnumMap<EquipmentSlotType, ItemStack> slotMap = new EnumMap<>(EquipmentSlotType.class);
	List<WeightedRandomStack> possibleMainhands = new ArrayList<>();

	/**
	 * Construcs an armor set, using the order of EquipmentSlotType.
	 * @param set A 6-length ItemStack array, ordered to match EquipmentSlotType.  Stacks may be empty.
	 */
	public ArmorSet(ResourceLocation name, int level, ItemStack... set) {
		this.level = level;
		mainhand = set[0];
		offhand = set[1];
		feet = set[2];
		legs = set[3];
		chest = set[4];
		head = set[5];
		for (int i = 0; i < 6; i++)
			slotMap.put(EquipmentSlotType.values()[i], set[i]);
		this.name = name;
	}

	/**
	 * A helper constructor that will take either ItemStack, Block, or Item.
	 * @param set A 6-length ItemStack/Item/Block array, ordered to match EquipmentSlotType.  Stacks may be empty, may not pass null.
	 */
	public ArmorSet(ResourceLocation name, int level, Object... set) {
		this(name, level, PlaceboUtil.toStackArray(set));
	}

	/**
	 * Makes the entity wear this armor set.  Returns the entity for convenience.
	 */
	public LivingEntity apply(LivingEntity entity) {
		for (EquipmentSlotType e : EquipmentSlotType.values())
			entity.setItemStackToSlot(e, slotMap.get(e).copy());
		if (!possibleMainhands.isEmpty()) entity.setHeldItem(Hand.MAIN_HAND, WeightedRandom.getRandomItem(ThreadLocalRandom.current(), possibleMainhands).getStack().copy());
		return entity;
	}

	public CompoundNBT apply(CompoundNBT entity) {
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

	public static void register(ArmorSet set) {
		if (!REGISTRY.containsKey(set.name)) {
			REGISTRY.put(set.name, set);
			LEVEL_TO_SETS.put(set.level, set);
			if (set.level > maxLevel) maxLevel = set.level;
		} else DeadlyModule.LOGGER.error("Attempted to register an ArmorSet with name {}, but it already exists!", set.name);
	}

	@Nullable
	public static ArmorSet getByName(ResourceLocation name) {
		return REGISTRY.get(name);
	}

	public static void unregister(ResourceLocation name) {
		ArmorSet set = REGISTRY.remove(name);
		if (set != null) {
			LEVEL_TO_SETS.remove(set.level, set);
			if (!LEVEL_TO_SETS.containsKey(maxLevel)) {
				for (int i = maxLevel; i >= 0; i--) {
					if (LEVEL_TO_SETS.containsKey(i) || i == 0) {
						maxLevel = i;
						break;
					}
				}
			}
		}
	}

	public static int getMaxLevel() {
		return maxLevel;
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

	public static ArmorSet getSetFor(int level, Random random) {
		Collection<ArmorSet> sets = null;
		while (sets == null || sets.isEmpty()) {
			sets = LEVEL_TO_SETS.get(level--);
		}
		return sets.stream().skip(random.nextInt(sets.size())).findFirst().get();
	}

}