package shadows.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.deadly.config.DeadlyConfig;
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
	public static final NBTTagCompound ARROW = getDefaultTag(EntityTippedArrow.class);
	public static final String EFFECTS = "ActiveEffects";
	public static final String TIME = "Time";
	public static final String DROP_ITEM = "DropItem";
	public static final String HURT_ENTITIES = "HurtEntities";
	public static final String FALL_HURT_AMOUNT = "FallHurtAmount";
	public static final String FALL_HURT_MAX = "FallHurtMax";
	public static final String TILE_ENTITY_DATA = "TileEntityData";
	public static final NBTTagCompound TNT = getDefaultTag(EntityTNTPrimed.class);
	public static final String FUSE = "Fuse";

	/**
	 * Creates a tag that will spawn this entity, with all default values.
	 */
	public static NBTTagCompound getDefaultTag(Class<? extends Entity> entity) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString(SpawnerBuilder.ID, EntityList.getKey(entity).toString());
		return tag;
	}

	/**
	 * Sets this entity's hp.
	 */
	public static NBTTagCompound setHealth(NBTTagCompound entity, float health) {
		entity.setFloat(HEALTH, health);
		return entity;
	}

	/**
	 * Tells this entity to not despawn naturally.
	 */
	public static NBTTagCompound setPersistent(NBTTagCompound entity, boolean persistent) {
		entity.setBoolean(PERSISTENT, persistent);
		return entity;
	}

	/**
	 * Sets the equipment of a written entity.
	 * @param tag The entity tag, created by {@link Entity#writeToNBT}
	 * @param equipment Stacks to represent the equipment, in the order of EntityEquipmentSlot
	 */
	public static NBTTagCompound setEquipment(NBTTagCompound entity, ItemStack... equipment) {
		ItemStack[] stacks = fixStacks(equipment);
		NBTTagList tagListHands = new NBTTagList();
		NBTTagList tagListArmor = new NBTTagList();

		for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) {
			ItemStack stack = stacks[s.ordinal()];
			if (s.getSlotType() == EntityEquipmentSlot.Type.HAND && !stack.isEmpty()) {
				tagListHands.set(s.getIndex(), stack.writeToNBT(new NBTTagCompound()));
			} else if (!stack.isEmpty()) {
				tagListArmor.set(s.getIndex(), stack.writeToNBT(new NBTTagCompound()));
			}
		}

		entity.setTag(HAND_ITEMS, tagListHands);
		entity.setTag(ARMOR_ITEMS, tagListArmor);
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
	public static NBTTagCompound setDropChances(NBTTagCompound entity, float... chances) {
		float[] fixed = fixChances(chances);
		NBTTagList tagListHands = new NBTTagList();
		NBTTagList tagListArmor = new NBTTagList();

		for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) {
			NBTTagFloat chance = new NBTTagFloat(fixed[s.ordinal()]);
			if (s.getSlotType() == EntityEquipmentSlot.Type.HAND) {
				tagListHands.set(s.getIndex(), chance);
			} else tagListArmor.set(s.getIndex(), chance);

		}

		entity.setTag(HAND_ITEMS, tagListHands);
		entity.setTag(ARMOR_ITEMS, tagListArmor);
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
	public static NBTTagCompound setOffset(NBTTagCompound entity, double x, double y, double z) {
		entity.setTag(OFFSET, doubleTagList(x, y, z));
		return entity;
	}

	/**
	 * Sets the motion of an entity.
	 */
	public static NBTTagCompound setMotion(NBTTagCompound entity, double x, double y, double z) {
		entity.setTag(MOTION, doubleTagList(x, y, z));
		return entity;
	}

	/**
	 * Sets fireball motion, because fireballs are stupid and use their own key.
	 */
	public static NBTTagCompound setFireballMotion(NBTTagCompound entity, double x, double y, double z) {
		entity.setTag(DIRECTION, doubleTagList(x, y, z));
		return entity;
	}

	public static NBTTagList doubleTagList(double... data) {
		NBTTagList tagList = new NBTTagList();
		for (double d : data)
			tagList.appendTag(new NBTTagDouble(d));
		return tagList;
	}

	/**
	 * Adds a potion effect to the stack and returns it.
	 */
	public static ItemStack addPotionEffect(ItemStack stack, Potion potion, int duration, int amplifier) {
		return PotionUtils.appendEffects(stack, Arrays.asList(new PotionEffect(potion, duration, amplifier)));
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static void addPotionEffect(NBTTagCompound tag, Potion potion, int amplifier) {
		TagBuilder.addPotionEffect(tag, potion, Integer.MAX_VALUE, amplifier, false);
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static void addPotionEffect(NBTTagCompound tag, Potion potion, int amplifier, boolean showParticles) {
		TagBuilder.addPotionEffect(tag, potion, Integer.MAX_VALUE, amplifier, showParticles);
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static void addPotionEffect(NBTTagCompound tag, Potion potion, int duration, int amplifier) {
		TagBuilder.addPotionEffect(tag, potion, duration, amplifier, false);
	}

	/**
	 * Adds a potion effect to this entity.
	 */
	public static NBTTagCompound addPotionEffect(NBTTagCompound entity, Potion potion, int duration, int amplifier, boolean showParticles) {
		NBTTagList effects = entity.getTagList(EFFECTS, 10);
		PotionEffect fx = new PotionEffect(potion, duration, amplifier, false, showParticles);
		effects.appendTag(fx.writeCustomPotionEffectToNBT(new NBTTagCompound()));
		entity.setTag(EFFECTS, effects);
		return entity;
	}

	/**
	 * Makes a falling block tag.
	 */
	public static NBTTagCompound fallingBlock(IBlockState state, int time) {
		return TagBuilder.fallingBlock(state, time, false, 2, 40, false, null);
	}

	/**
	 * Makes a falling block tag.
	 */
	public static NBTTagCompound fallingBlock(IBlockState state, int time, float fallDamage) {
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
	 * @return An NBTTagCompound containing a falling block.
	 */
	public static NBTTagCompound fallingBlock(IBlockState state, int time, boolean hurtEntities, float fallDamage, int maxFallDamage, boolean dropItem, NBTTagCompound tileData) {
		NBTTagCompound tag = getDefaultTag(EntityFallingBlock.class);
		tag.setString("Block", state.getBlock().getRegistryName().toString());
		tag.setByte("Data", (byte) state.getBlock().getMetaFromState(state));
		tag.setInteger(TIME, time);
		tag.setBoolean(DROP_ITEM, dropItem);
		tag.setBoolean(HURT_ENTITIES, hurtEntities);
		tag.setFloat(FALL_HURT_AMOUNT, fallDamage);
		tag.setInteger(FALL_HURT_MAX, maxFallDamage);
		if (tileData != null) tag.setTag(TILE_ENTITY_DATA, tileData);
		return tag;
	}

	private static List<WeightedSpawnerEntity> randomPotentials = new ArrayList<>();

	/**
	 * Creates a spawner that pulls from all IMobs enabled in the config.
	 * @return A SpawnerBuilder with potentials for every enabled IMob.
	 */
	public static SpawnerBuilder createMobSpawnerRandom() {
		if (randomPotentials.isEmpty()) {
			List<EntityEntry> valid = new ArrayList<>();
			for (EntityEntry e : ForgeRegistries.ENTITIES)
				if (IMob.class.isAssignableFrom(e.getEntityClass())) valid.add(e);

			for (EntityEntry e : valid) {
				NBTTagCompound tag = getDefaultTag(e.getEntityClass());
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
	public static NBTTagCompound applyTNTHat(NBTTagCompound tag) {
		TagBuilder.setMotion(tag, 0.0, 0.3, 0.0);
		TagBuilder.addPotionEffect(tag, MobEffects.SPEED, 1);
		TagBuilder.addPotionEffect(tag, MobEffects.RESISTANCE, -6);
		addPassengers(tag, TNT.copy());
		return tag;
	}

	/**
	 * Sets the provided passengers to ride on the given entity.
	 */
	public static NBTTagCompound addPassengers(NBTTagCompound entity, NBTTagCompound... passengers) {
		NBTTagList list = entity.getTagList(PASSENGERS, 10);
		if (list.isEmpty()) entity.setTag(PASSENGERS, list);
		for (NBTTagCompound nbt : passengers)
			list.appendTag(nbt);
		return entity;
	}

	/**
	 * Forcibly gives skeletons bows.
	 */
	public static NBTTagCompound checkForSkeleton(NBTTagCompound entity) {
		if (EntitySkeleton.class.isAssignableFrom(EntityList.getClass(new ResourceLocation(entity.getString(SpawnerBuilder.ID))))) {
			TagBuilder.setEquipment(entity, new ItemStack(Items.BOW));
		}
		return entity;
	}

}