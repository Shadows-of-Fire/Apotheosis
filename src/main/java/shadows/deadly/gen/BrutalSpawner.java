package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockStoneBrick;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import shadows.deadly.DeadlyLoot;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.config.DeadlyConstants;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.SpawnerBuilder;
import shadows.util.ChestBuilder;
import shadows.util.TagBuilder;

/**
 * Rogue spawners that have stronger-than-usual mobs.
 * @author Shadows
 *
 */
public class BrutalSpawner extends WorldFeature {

	public static final NBTTagCompound BASE_TAG = new NBTTagCompound();
	public static final List<SpawnerItem> BRUTAL_SPAWNERS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos, Random rand) {
		if (DeadlyConfig.brutalSpawnerChance <= rand.nextDouble()) return;
		int x = pos.getX() + MathHelper.getInt(rand, 4, 12);
		int z = pos.getY() + MathHelper.getInt(rand, 4, 12);
		int y = rand.nextInt(40) + 11;
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		for (byte state = 0; y > 4; y--) {
			if (world.getBlockState(mPos.setPos(x, y, z)).isNormalCube()) {
				if (state == 0) {
					if (this.canBePlaced(world, mPos.up(), rand)) {
						this.place(world, mPos.up(), rand);
						WorldGenerator.SUCCESSES.add(pos.toLong());
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
	public boolean canBePlaced(World world, BlockPos pos, Random rand) {
		return canPlace(world, pos) || canPlace(world, pos.up());
	}

	@Override
	public void place(World world, BlockPos pos, Random rand) {
		MutableBlockPos mPos = new MutableBlockPos(pos);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		ChestBuilder.place(world, rand, pos.down(), rand.nextInt(9) == 0 ? DeadlyLoot.CHEST_VALUABLE : DeadlyLoot.SPAWNER_BRUTAL);
		WeightedRandom.getRandomItem(rand, BRUTAL_SPAWNERS).place(world, pos);
		world.setBlockState(pos.up(), Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), 2);
		for (int y1 = 0; y1 < 2; y1++) {
			if (rand.nextInt(4) == 0 && canPlace(world, mPos.setPos(x - 1, y + y1, z))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x - 1, y + y1, z), Blocks.VINE, 8, 2);
			}
			if (rand.nextInt(4) == 0 && canPlace(world, mPos.setPos(x + 1, y + y1, z))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x + 1, y + y1, z), Blocks.VINE, 2, 2);
			}
			if (rand.nextInt(4) == 0 && canPlace(world, mPos.setPos(x, y + y1, z - 1))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x, y + y1, z - 1), Blocks.VINE, 1, 2);
			}
			if (rand.nextInt(4) == 0 && canPlace(world, mPos.setPos(x, y + y1, z + 1))) {
				PlaceboUtil.setBlockWithMeta(world, mPos.setPos(x, y + y1, z + 1), Blocks.VINE, 4, 2);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return !DeadlyConfig.BRUTAL_MOBS.isEmpty() && DeadlyConfig.brutalSpawnerChance > 0;
	}

	public static void init() {
		for (PotionEffect p : DeadlyConfig.BRUTAL_POTIONS) {
			TagBuilder.addPotionEffect(BrutalSpawner.BASE_TAG, p.getPotion(), p.getAmplifier());
		}
		SpawnerItem.addItems(BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_SPAWNER_STATS, DeadlyConfig.BRUTAL_MOBS);
		for (SpawnerItem i : BRUTAL_SPAWNERS)
			initBrutal(i);

	}

	/**
	 * Sets the spawner item to be brutal.
	 */
	public static void initBrutal(SpawnerItem item) {
		applyBrutalStats(item.getSpawner().getSpawnData());
		for (NBTBase tag : item.getSpawner().getPotentials()) {
			applyBrutalStats(getOrCreate((NBTTagCompound) tag, SpawnerBuilder.ENTITY));
		}
	}

	/**
	 * Copies brutal base stats from the base tag to the given entity tag.
	 */
	public static NBTTagCompound applyBrutalStats(NBTTagCompound tag) {
		TagBuilder.checkForSkeleton(tag);
		for (String name : BASE_TAG.getKeySet())
			tag.setTag(name, BASE_TAG.getTag(name).copy());
		return tag;
	}

	private static boolean canPlace(World world, BlockPos pos) {
		return world.isBlockLoaded(pos) && world.getBlockState(pos).getBlock().isReplaceable(world, pos);
	}

	private static NBTTagCompound getOrCreate(NBTTagCompound parent, String key) {
		if (!parent.hasKey(key, NBT.TAG_COMPOUND)) parent.setTag(key, new NBTTagCompound());
		return parent.getCompoundTag(key);
	}
}