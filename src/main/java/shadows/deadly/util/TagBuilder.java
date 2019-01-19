package shadows.deadly.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.util.DeadlyConstants.TrapType;
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

	/**
	 * Makes a creeper spawner, with charged chance respecting the config.
	 * @return A SpawnerBuilder typed to Creeper, with potentials for Creeper and Charged Creeper.
	 */
	public static SpawnerBuilder createMobSpawnerCreeper() {
		SpawnerBuilder sb = new SpawnerBuilder();
		NBTTagCompound creeper = getDefaultTag(EntityCreeper.class);
		sb.setSpawnData(creeper);
		if (DeadlyConfig.chargedCreeperChance > 0) {
			int normal = 100 - DeadlyConfig.chargedCreeperChance;
			int charged = DeadlyConfig.chargedCreeperChance;
			NBTTagCompound chargedNbt = creeper.copy();
			chargedNbt.setBoolean("powered", true);
			sb.setPotentials(new WeightedSpawnerEntity(normal, creeper.copy()), new WeightedSpawnerEntity(charged, chargedNbt));
		}
		return sb;
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
				if (EntitySkeleton.class.isAssignableFrom(e.getEntityClass())) setEquipment(tag, new ItemStack(Items.BOW));
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
	 * Makes a spawner for traps (tnt hat spawner).
	 * @param type The trap type, which decides the base spawner type to setup.
	 * @param entity The entity, if using TrapType.NORMAL, otherwise null.
	 * @return
	 */
	public static SpawnerBuilder createMobSpawnerTrap(TrapType type, @Nullable ResourceLocation entity) {
		SpawnerBuilder sb;

		if (type == DeadlyConstants.TrapType.RANDOM) sb = TagBuilder.createMobSpawnerRandom();
		else if (type == DeadlyConstants.TrapType.CREEPER) sb = TagBuilder.createMobSpawnerCreeper();
		else sb = new SpawnerBuilder().setType(entity);

		NBTTagCompound data = sb.getSpawnData();
		if (type == DeadlyConstants.TrapType.NORMAL && EntitySkeleton.class.isAssignableFrom(EntityList.getClass(entity))) {
			setEquipment(data, new ItemStack(Items.BOW));
		}

		applyTNTHat(data);

		NBTTagList potentials = sb.getPotentials();
		for (NBTBase tag : potentials) {
			NBTTagCompound entityTag = ((NBTTagCompound) tag).getCompoundTag(SpawnerBuilder.ENTITY);
			TagBuilder.applyTNTHat(entityTag);
			TagBuilder.setOffset(entityTag, 0.5, 1.0, 0.5);
		}

		TagBuilder.setOffset(data, 0.5, 1.0, 0.5);
		sb.setType(EntityTNTPrimed.class);
		sb.setDelay(0);
		sb.setSpawnRange(0);
		sb.setPlayerRange(4);
		sb.setSpawnCount(1);
		sb.setSpawnData(data);
		return sb;
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
	 * Makes your standard arrow shooter.
	 * @param onFire If the arrows are flaming.
	 * @return A SpawnerBuilder, setup to make arrow towers.
	 */
	public static SpawnerBuilder createArrowSpawner(boolean onFire) {
		SpawnerBuilder sb = new SpawnerBuilder();
		WeightedSpawnerEntity[] potentials = new WeightedSpawnerEntity[48];
		NBTTagCompound baseTag = ARROW.copy();
		baseTag.setByte(ARROW_PICKUP, (byte) 2);
		baseTag.setDouble(ARROW_DAMAGE, DeadlyConfig.towerArrowDamage);
		if (onFire) {
			baseTag.setShort(ENTITY_FIRE, (short) 2000);
		}
		double step = 2.0 * Math.PI / 16;
		double rotation = step / 2.0;
		double elevation = -0.15;
		double xMotion, zMotion, xOffset, zOffset;
		for (int i = 48; i-- > 0;) {
			potentials[i] = new WeightedSpawnerEntity(1, baseTag.copy());
			rotation += step;
			xMotion = MathHelper.sin((float) rotation);
			zMotion = MathHelper.cos((float) rotation);
			if (Math.abs(xMotion) < Math.abs(zMotion)) {
				xOffset = xMotion * 0.6 + 0.5;
				zOffset = zMotion < 0.0 ? -0.1 : 1.1;
			} else if (Math.abs(xMotion) > Math.abs(zMotion)) {
				xOffset = xMotion < 0.0 ? -0.1 : 1.1;
				zOffset = zMotion * 0.6 + 0.5;
			} else {
				xOffset = xMotion < 0.0 ? -0.1 : 1.1;
				zOffset = zMotion < 0.0 ? -0.1 : 1.1;
			}
			TagBuilder.setOffset(potentials[i].getNbt(), xOffset, elevation + 0.35, zOffset);
			TagBuilder.setMotion(potentials[i].getNbt(), xMotion, elevation, zMotion);
			if (i % 16 == 0) {
				rotation += step / 2.0;
				elevation += 0.15;
			}
		}
		sb.setType(EntityTippedArrow.class);
		sb.setDelay(-1);
		sb.setMinAndMaxDelay(4, 8);
		sb.setSpawnCount(1);
		sb.setSpawnRange(-1);
		sb.setPlayerRange(8);
		sb.setSpawnData(baseTag);
		sb.setPotentials(potentials);
		return sb;
	}

	/**
	 * Similar to the arrow spawner, but with fire blocks.  Very safe.
	 * @return Pure death.
	 */
	public static SpawnerBuilder createFireSpawner() {
		SpawnerBuilder sb = new SpawnerBuilder();
		WeightedSpawnerEntity[] potentials = new WeightedSpawnerEntity[48];
		NBTTagCompound baseTag = TagBuilder.fallingBlock(Blocks.FIRE.getDefaultState(), 1, 1);
		baseTag.setShort("Fire", (short) 2000);
		double step = 2.0 * Math.PI / 16;
		double rotation = step / 2.0;
		double elevation = 0.1;
		double xMotion, zMotion;
		for (int i = 48; i-- > 0;) {
			potentials[i] = new WeightedSpawnerEntity(1, baseTag.copy());
			rotation += step;
			xMotion = MathHelper.sin((float) rotation) * 0.2;
			zMotion = MathHelper.cos((float) rotation) * 0.2;
			TagBuilder.setOffset(potentials[i].getNbt(), 0.5, 1.5, 0.5);
			TagBuilder.setMotion(potentials[i].getNbt(), xMotion, elevation, zMotion);
			if (i % 16 == 0) {
				rotation += step / 2.0;
				elevation += 0.25;
			}
		}
		sb.setType(EntityFallingBlock.class);
		sb.setMinAndMaxDelay(2, 4);
		sb.setSpawnCount(1);
		sb.setSpawnRange(-1);
		sb.setPlayerRange(5);
		sb.setSpawnData(baseTag);
		sb.setPotentials(potentials);
		return sb;
	}

	/**
	 * Makes a potion trap.  Queries config for level/duration.
	 */
	public static SpawnerBuilder createPotionSpawner(Potion... potions) {
		SpawnerBuilder sb = new SpawnerBuilder();

		ItemStack stack = new ItemStack(Items.SPLASH_POTION);
		List<PotionEffect> fx = new ArrayList<>();
		for (Potion p : potions) {
			int level = DeadlyConfig.getPotencyForType(p) - 1;
			int duration = DeadlyConfig.getDurationForType(p);
			if (level >= 0) fx.add(new PotionEffect(p, duration, level));
		}

		PotionUtils.appendEffects(stack, fx);

		NBTTagCompound entity = getDefaultTag(EntityPotion.class);
		entity.setTag("Potion", stack.writeToNBT(new NBTTagCompound()));

		TagBuilder.setOffset(entity, 0.5, 1.1, 0.5);
		TagBuilder.setMotion(entity, 0.0, 0.35, 0.0);
		sb.setType(EntityPotion.class);
		sb.setMinAndMaxDelay(30, 50);
		sb.setSpawnRange(-1);
		sb.setPlayerRange(3);
		sb.setSpawnCount(1);
		sb.setSpawnData(entity);
		return sb;
	}

	/**
	 * Makes your standard proximity bomb.
	 */
	public static SpawnerBuilder createTNTSpawner() {
		SpawnerBuilder sb = new SpawnerBuilder();
		NBTTagCompound tag = TNT.copy();
		tag.setShort(FUSE, (short) 5);
		TagBuilder.setMotion(tag, 0.0, 0.4, 0.0);
		TagBuilder.setOffset(tag, 0.5, 0.5, 0.5);
		sb.setType(EntityTNTPrimed.class);
		sb.setDelay(0);
		sb.setMinAndMaxDelay(50, 100);
		sb.setSpawnRange(2);
		sb.setPlayerRange(5);
		sb.setSpawnCount(4);
		sb.setMaxNearbyEntities(3);
		sb.setSpawnData(tag);
		sb.setPotentials(new WeightedSpawnerEntity(1, tag));
		return sb;
	}

	public static NBTTagCompound checkForSkeleton(NBTTagCompound entity) {
		if (EntitySkeleton.class.isAssignableFrom(EntityList.getClass(new ResourceLocation(entity.getString(SpawnerBuilder.ID))))) {
			TagBuilder.setEquipment(entity, new ItemStack(Items.BOW));
		}
		return entity;
	}

	/// Makes your standard proximity bomb.
	/// NYI, this won't work as-is.
	/*
	public static NBTTagCompound createCaveInSpawner(String type) {
		SpawnerBuilder sb = new SpawnerBuilder();
		String[] types = { "FallingSand", "Fireball" };
		int[] weights = { 24, 1 };
		NBTTagCompound[] properties = new NBTTagCompound[2];
		if (type.equals("normal")) {
			properties[0] = TagBuilder.fallingBlock(Blocks.COBBLESTONE.getDefaultState(), 1, 1.5F);
		} else if (type.equals("silverfish")) {
			properties[0] = TagBuilder.fallingBlock(Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.COBBLESTONE), 1, 1.5F);
		} else if (type.equals("gravel")) {
			properties[0] = TagBuilder.fallingBlock(Blocks.GRAVEL.getDefaultState(), 1, 1);
		} else return null;
		properties[1] = new NBTTagCompound();
		properties[1].setInteger("ExplosionPower", 2);
		properties[1].setTag("direction", TagBuilder.doubleNBTTagList(0.0, -1.0, 0.0));
		TagBuilder.setPosition(properties[1], 0.5, 0.5, 0.5);
		tag.setType("FallingSand");
		tag.setDelay(50);
		tag.setMinAndMaxDelay(3, 6);
		tag.setSpawnRange(9);
		tag.setPlayerRange(7);
		tag.setSpawnCount(16);
		tag.setMaxNearbyEntities(Short.MAX_VALUE);
		tag.setSpawnData(properties[0]);
		tag.setPotentials(types, weights, properties);
		return tag.spawnerTag;
	}
	*/

	/// Makes your standard fireball shooter.
	/* NYI - Fireballs cannot be spawned yet.
	 * public static NBTTagCompound createFireballSpawner() {
	 * TagBuilder tag = new TagBuilder(new NBTTagCompound());
	 * String[] types = new String[48];
	 * int[] weights = new int[48];
	 * NBTTagCompound[] properties = new NBTTagCompound[48];
	 * NBTTagCompound baseTag = new NBTTagCompound();
	 * double step = 2.0 * Math.PI / (double)16;
	 * double rotation = step / 2.0;
	 * double elevation = -0.02;
	 * double xMotion, zMotion, xOffset, zOffset;
	 * for (int i = 48; i-- > 0;) {
	 * types[i] = "SmallFireball";
	 * weights[i] = 1;
	 * properties[i] = (NBTTagCompound)baseTag.copy();
	 * rotation += step;
	 * xMotion = (double)MathHelper.sin((float)rotation) * 0.02;
	 * zMotion = (double)MathHelper.cos((float)rotation) * 0.02;
	 * if (Math.abs(xMotion) < Math.abs(zMotion)) {
	 * xOffset = xMotion * 0.6 + 0.5;
	 * zOffset = zMotion < 0.0 ? -0.1 : 1.1;
	 * }
	 * else if (Math.abs(xMotion) > Math.abs(zMotion)) {
	 * xOffset = xMotion < 0.0 ? -0.1 : 1.1;
	 * zOffset = zMotion * 0.6 + 0.5;
	 * }
	 * else {
	 * xOffset = xMotion < 0.0 ? -0.1 : 1.1;
	 * zOffset = zMotion < 0.0 ? -0.1 : 1.1;
	 * }
	 * setPosition(properties[i], xOffset, 0.5, zOffset);
	 * setFireballHeading(properties[i], xMotion, elevation, zMotion);
	 * if (i % 16 == 0) {
	 * rotation += step / 2.0;
	 * elevation += 0.02;
	 * }
	 * }
	 * tag.setType("SmallFireball");
	 * tag.setDelay(-1);
	 * tag.setMinAndMaxDelay(6, 10);
	 * tag.setSpawnCount(1);
	 * tag.setSpawnRange(-1);
	 * tag.setPlayerRange(10);
	 * tag.setSpawnData(baseTag);
	 * tag.setPotentials(types, weights, properties);
	 * return tag.spawnerTag;
	 * }
	 */
}
