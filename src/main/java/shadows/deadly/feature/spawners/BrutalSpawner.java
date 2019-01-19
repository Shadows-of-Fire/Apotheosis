package shadows.deadly.feature.spawners;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockStoneBrick;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.feature.SpawnerItem;
import shadows.deadly.feature.WorldFeature;
import shadows.deadly.feature.WorldGenerator;
import shadows.deadly.util.ChestBuilder;
import shadows.deadly.util.DeadlyConstants;
import shadows.deadly.util.TagBuilder;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.SpawnerBuilder;

/**
 * Rogue spawners that have stronger-than-usual mobs.
 * TODO: Spawners are too common
 * @author Shadows
 *
 */
public class BrutalSpawner extends WorldFeature {

	public static final NBTTagCompound BASE_TAG = new NBTTagCompound();
	public static final List<SpawnerItem> BRUTAL_SPAWNERS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos) {
		if (DeadlyConfig.brutalSpawnerChance <= world.rand.nextDouble()) return;
		int x = pos.getX() + MathHelper.getInt(world.rand, 4, 12);
		int z = pos.getY() + MathHelper.getInt(world.rand, 4, 12);
		int y = world.rand.nextInt(40) + 11;
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		for (byte state = 0; y > 4; y--) {
			if (world.getBlockState(mPos.setPos(x, y, z)).isNormalCube()) {
				if (state == 0) {
					if (this.canBePlaced(world, mPos.up())) {
						this.place(world, mPos.up());
						return;
					}
					state = -1;
				}
			} else {
				state = 0;
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos) {
		return world.isAirBlock(pos) && world.isAirBlock(pos.up());
	}

	@Override
	public void place(World world, BlockPos pos) {
		MutableBlockPos mPos = new MutableBlockPos(pos);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		ChestBuilder.place(world, world.rand, pos.down(), ChestBuilder.SPAWNER_BRUTAL);
		WeightedRandom.getRandomItem(world.rand, BRUTAL_SPAWNERS).place(world, pos);
		world.setBlockState(pos.up(), Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), 2);
		for (int y1 = 0; y1 < 2; y1++) {
			if (world.rand.nextInt(4) == 0 && world.isAirBlock(mPos.setPos(x - 1, y + y1, z))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x - 1, y + y1, z), Blocks.VINE, 8, 2);
			}
			if (world.rand.nextInt(4) == 0 && world.isAirBlock(mPos.setPos(x + 1, y + y1, z))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x + 1, y + y1, z), Blocks.VINE, 2, 2);
			}
			if (world.rand.nextInt(4) == 0 && world.isAirBlock(mPos.setPos(x, y + y1, z - 1))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x, y + y1, z - 1), Blocks.VINE, 1, 2);
			}
			if (world.rand.nextInt(4) == 0 && world.isAirBlock(mPos.setPos(x, y + y1, z + 1))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x, y + y1, z + 1), Blocks.VINE, 4, 2);
			}
		}
		WorldGenerator.debugPillar(world, pos.up(3));
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.brutalSpawnerChance > 0;
	}

	public static void init() {
		Potion[] potions = { MobEffects.FIRE_RESISTANCE, MobEffects.REGENERATION, MobEffects.RESISTANCE, MobEffects.STRENGTH, MobEffects.SPEED, MobEffects.WATER_BREATHING };
		for (Potion p : potions) {
			int level = DeadlyConfig.config.getInt("Level: " + p.getRegistryName(), DeadlyConstants.BRUTAL_MOBS, 1, 0, Integer.MAX_VALUE, "The level of this potion for brutal mobs.  Set to 0 to disable.");
			if (level > 0) TagBuilder.addPotionEffect(BrutalSpawner.BASE_TAG, p, level - 1);
		}

		SpawnerItem.addItems(BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_SPAWNER_STATS, DeadlyConfig.brutalWeightedMobs);
		for (SpawnerItem i : BRUTAL_SPAWNERS)
			initBrutal(i);

	}

	/**
	 * Sets the spawner item to be brutal.
	 */
	public static void initBrutal(SpawnerItem item) {
		applyBrutalStats(item.getSpawner().getSpawnData());
		for (NBTBase tag : item.getSpawner().getPotentials()) {
			if (AbstractSkeleton.class.isAssignableFrom(EntityList.getClass(new ResourceLocation(((NBTTagCompound) tag).getCompoundTag(SpawnerBuilder.ENTITY).getString(SpawnerBuilder.ID))))) {
				TagBuilder.setEquipment(((NBTTagCompound) tag).getCompoundTag(SpawnerBuilder.ENTITY), new ItemStack(Items.BOW));
			}
			applyBrutalStats(((NBTTagCompound) tag).getCompoundTag(SpawnerBuilder.ENTITY));
		}
	}

	/**
	 * Copies brutal base stats from the base tag to the given entity tag.
	 */
	public static NBTTagCompound applyBrutalStats(NBTTagCompound tag) {
		for (String name : BASE_TAG.getKeySet())
			tag.setTag(name, BASE_TAG.getTag(name).copy());
		return tag;
	}
}