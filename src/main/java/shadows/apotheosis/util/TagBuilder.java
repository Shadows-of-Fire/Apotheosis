package shadows.apotheosis.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.placebo.util.SpawnerBuilder;

/**
 * Util class to manipulate entities before they are spawned.
 * @author Shadows
 *
 */
public class TagBuilder {

	public static final String HAND_ITEMS = "HandItems";
	public static final String ARMOR_ITEMS = "ArmorItems";
	public static final String PASSENGERS = "Passengers";
	public static final String PERSISTENT = "PersistenceRequired";
	public static final String HEALTH = "Health";
	public static final String OFFSET = "Offset";
	public static final String MOTION = "Motion";
	public static final String DIRECTION = "direction";
	public static final String ENTITY_FIRE = "Fire";
	public static final String ARROW_PICKUP = "pickup";
	public static final String ARROW_DAMAGE = "damage";
	public static final CompoundNBT ARROW = getDefaultTag(EntityType.ARROW);
	public static final String EFFECTS = "ActiveEffects";
	public static final String TIME = "Time";
	public static final String DROP_ITEM = "DropItem";
	public static final String HURT_ENTITIES = "HurtEntities";
	public static final String FALL_HURT_AMOUNT = "FallHurtAmount";
	public static final String FALL_HURT_MAX = "FallHurtMax";
	public static final String TILE_ENTITY_DATA = "TileEntityData";
	public static final CompoundNBT TNT = getDefaultTag(EntityType.TNT);
	public static final String FUSE = "Fuse";

	/**
	 * Creates a tag that will spawn this entity, with all default values.
	 */
	public static CompoundNBT getDefaultTag(EntityType<? extends Entity> entity) {
		CompoundNBT tag = new CompoundNBT();
		tag.putString(SpawnerBuilder.ID, entity.getRegistryName().toString());
		return tag;
	}

	/**
	 * Sets this entity's hp.
	 */
	public static CompoundNBT setHealth(CompoundNBT entity, float health) {
		entity.putFloat(HEALTH, health);
		return entity;
	}

	/**
	 * Tells this entity to not despawn naturally.
	 */
	public static CompoundNBT setPersistent(CompoundNBT entity, boolean persistent) {
		entity.putBoolean(PERSISTENT, persistent);
		return entity;
	}

	/**
	 * Sets the equipment of a written entity.
	 * @param tag The entity tag, created by {@link Entity#writeToNBT}
	 * @param equipment Stacks to represent the equipment, in the order of EntityEquipmentSlot
	 */
	public static CompoundNBT setEquipment(CompoundNBT entity, ItemStack... equipment) {
		ItemStack[] stacks = fixStacks(equipment);
		ListNBT tagListHands = new ListNBT();
		for (int i = 0; i < 2; i++)
			tagListHands.add(new CompoundNBT());

		ListNBT tagListArmor = new ListNBT();
		for (int i = 0; i < 4; i++)
			tagListArmor.add(new CompoundNBT());

		for (EquipmentSlotType s : EquipmentSlotType.values()) {
			ItemStack stack = stacks[s.ordinal()];
			if (s.getSlotType() == EquipmentSlotType.Group.HAND && !stack.isEmpty()) {
				tagListHands.set(s.getIndex(), stack.write(new CompoundNBT()));
			} else if (!stack.isEmpty()) {
				tagListArmor.set(s.getIndex(), stack.write(new CompoundNBT()));
			}
		}

		entity.put(HAND_ITEMS, tagListHands);
		entity.put(ARMOR_ITEMS, tagListArmor);
		return entity;
	}

	private static ItemStack[] fixStacks(ItemStack[] unfixed) {
		ItemStack[] stacks = new ItemStack[] { ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, };
		for (int i = 0; i < unfixed.length; i++)
			if (unfixed[i] != null) stacks[i] = unfixed[i];
		return stacks;
	}

	/**
	 * Sets the equipment drop chances of a written entity.
	 * @param tag The entity tag, created by {@link Entity#writeToNBT}
	 * @param chances Drop chances for each slot, in the order of EntityEquipmentSlot.  If a chance is above 1, that slot can drop without requiring player damage.
	 */
	public static CompoundNBT setDropChances(CompoundNBT entity, float... chances) {
		float[] fixed = fixChances(chances);
		ListNBT tagListHands = new ListNBT();
		ListNBT tagListArmor = new ListNBT();

		for (EquipmentSlotType s : EquipmentSlotType.values()) {
			FloatNBT chance = FloatNBT.func_229689_a_(fixed[s.ordinal()]);
			if (s.getSlotType() == EquipmentSlotType.Group.HAND) {
				tagListHands.set(s.getIndex(), chance);
			} else tagListArmor.set(s.getIndex(), chance);

		}

		entity.put(HAND_ITEMS, tagListHands);
		entity.put(ARMOR_ITEMS, tagListArmor);
		return entity;
	}

	private static float[] fixChances(float[] unfixed) {
		float[] chances = new float[6];
		for (int i = 0; i < unfixed.length; i++)
			chances[i] = unfixed[i];
		return chances;
	}

	/**
	 * Sets this entity to spawn with a given offset from the spawner, instead of using random coordinates.
	 */
	public static CompoundNBT setOffset(CompoundNBT entity, double x, double y, double z) {
		entity.put(OFFSET, doubleTagList(x, y, z));
		return entity;
	}

	/**
	 * Sets the motion of an entity.
	 */
	public static CompoundNBT setMotion(CompoundNBT entity, double x, double y, double z) {
		entity.put(MOTION, doubleTagList(x, y, z));
		return entity;
	}

	/**
	 * Sets fireball motion, because fireballs are stupid and use their own key.
	 */
	public static CompoundNBT setFireballMotion(CompoundNBT entity, double x, double y, double z) {
		entity.put(DIRECTION, doubleTagList(x, y, z));
		return entity;
	}

	public static ListNBT doubleTagList(double... data) {
		ListNBT tagList = new ListNBT();
		for (double d : data)
			tagList.add(DoubleNBT.func_229684_a_(d));
		return tagList;
	}

	/**
	 * Adds a potion effect to the stack and returns it.
	 */
	public static ItemStack addPotionEffect(ItemStack stack, Effect potion, int duration, int amplifier) {
		return PotionUtils.appendEffects(stack, Arrays.asList(new EffectInstance(potion, duration, amplifier)));
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static void addPotionEffect(CompoundNBT tag, Effect potion, int amplifier) {
		TagBuilder.addPotionEffect(tag, potion, Integer.MAX_VALUE, amplifier, false);
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static void addPotionEffect(CompoundNBT tag, Effect potion, int amplifier, boolean showParticles) {
		TagBuilder.addPotionEffect(tag, potion, Integer.MAX_VALUE, amplifier, showParticles);
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static void addPotionEffect(CompoundNBT tag, Effect potion, int duration, int amplifier) {
		TagBuilder.addPotionEffect(tag, potion, duration, amplifier, false);
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static CompoundNBT addPotionEffect(CompoundNBT entity, Effect potion, int duration, int amplifier, boolean showParticles) {
		ListNBT effects = entity.getList(EFFECTS, 10);
		EffectInstance fx = new EffectInstance(potion, duration, amplifier, false, showParticles);
		effects.add(fx.write(new CompoundNBT()));
		entity.put(EFFECTS, effects);
		return entity;
	}

	/**
	 * Makes a falling block tag.
	 */
	public static CompoundNBT fallingBlock(BlockState state, int time) {
		return TagBuilder.fallingBlock(state, time, false, 2, 40, false, null);
	}

	/**
	 * Makes a falling block tag.
	 */
	public static CompoundNBT fallingBlock(BlockState state, int time, float fallDamage) {
		return TagBuilder.fallingBlock(state, time, true, fallDamage, 40, false, null);
	}

	/**
	 * Makes a falling block.
	 * @param state The state to show.
	 * @param time How long we have been falling.  Despawn after 600 ticks, or if starting below 0, once we hit 0.
	 * @param hurtEntities If we damage entities.
	 * @param fallDamage How much we damage entities for, if hurtEntities is true.
	 * @param maxFallDamage The max amount we can damage an entity for.
	 * @param dropItem If we drop our block on despawn.
	 * @param tileData Tile entity data, to be set if we hit the ground/
	 * @return An CompoundNBT containing a falling block.
	 */
	public static CompoundNBT fallingBlock(BlockState state, int time, boolean hurtEntities, float fallDamage, int maxFallDamage, boolean dropItem, CompoundNBT tileData) {
		CompoundNBT tag = getDefaultTag(EntityType.FALLING_BLOCK);
		tag.put("BlockState", NBTUtil.writeBlockState(state));
		tag.putInt(TIME, time);
		tag.putBoolean(DROP_ITEM, dropItem);
		tag.putBoolean(HURT_ENTITIES, hurtEntities);
		tag.putFloat(FALL_HURT_AMOUNT, fallDamage);
		tag.putInt(FALL_HURT_MAX, maxFallDamage);
		if (tileData != null) tag.put(TILE_ENTITY_DATA, tileData);
		return tag;
	}

	private static List<WeightedSpawnerEntity> randomPotentials = new ArrayList<>();

	/**
	 * Creates a spawner that pulls from all IMobs enabled in the config.
	 * @return A SpawnerBuilder with potentials for every enabled IMob.
	 */
	public static SpawnerBuilder createMobSpawnerRandom() {
		if (randomPotentials.isEmpty()) {
			List<EntityType<?>> valid = new ArrayList<>();
			for (EntityType<?> e : ForgeRegistries.ENTITIES)
				if (e.getClassification() == EntityClassification.MONSTER) valid.add(e);

			for (EntityType<?> e : valid) {
				CompoundNBT tag = getDefaultTag(e);
				checkForSkeleton(tag);
				int weight = DeadlyConfig.getWeightForEntry(e);
				if (weight > 0) randomPotentials.add(new WeightedSpawnerEntity(weight, tag));
			}
		}

		SpawnerBuilder sb = new SpawnerBuilder();
		sb.setPotentials(randomPotentials.toArray(new WeightedSpawnerEntity[0]));
		return sb;
	}

	public static WeightedSpawnerEntity getRandomEntity(Random rand) {
		return WeightedRandom.getRandomItem(rand, randomPotentials);
	}

	/**
	 * Converts a standard entity tag into one of tnt riding the entity.
	 * @param tag An entity written to NBT.
	 * @return The provided entity, now with TNT on it's head.
	 */
	public static CompoundNBT applyTNTHat(CompoundNBT tag) {
		TagBuilder.setMotion(tag, 0.0, 0.3, 0.0);
		TagBuilder.addPotionEffect(tag, Effects.SPEED, 1);
		TagBuilder.addPotionEffect(tag, Effects.RESISTANCE, -6);
		addPassengers(tag, TNT.copy());
		return tag;
	}

	/**
	 * Sets the provided passengers to ride on the given entity.
	 */
	public static CompoundNBT addPassengers(CompoundNBT entity, CompoundNBT... passengers) {
		ListNBT list = entity.getList(PASSENGERS, 10);
		if (list.isEmpty()) entity.put(PASSENGERS, list);
		for (CompoundNBT nbt : passengers)
			list.add(nbt);
		return entity;
	}

	/**
	 * Forcibly gives skeletons bows.
	 */
	public static CompoundNBT checkForSkeleton(CompoundNBT entity) {
		if (entity.getString(SpawnerBuilder.ID).contains("skeleton")) {
			TagBuilder.setEquipment(entity, new ItemStack(Items.BOW));
		}
		return entity;
	}

	public static CompoundNBT checkForCreeper(CompoundNBT entity) {
		if (entity.getString(SpawnerBuilder.ID).contains("creeper")) {
			ListNBT effects = entity.getList(EFFECTS, 10);
			for (INBT nbt : effects) {
				((CompoundNBT) nbt).putInt("Duration", 300);
			}
			return entity;
		}
		return entity;
	}

}