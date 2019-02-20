package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import shadows.deadly.DeadlyLoot;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.config.DeadlyConstants;
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
	public void generate(World world, int chunkX, int chunkZ, Random rand) {
		if (DeadlyConfig.brutalSpawnerChance <= rand.nextDouble()) return;
		int x = (chunkX << 4) + MathHelper.getInt(rand, 4, 12);
		int z = (chunkZ << 4) + MathHelper.getInt(rand, 4, 12);
		int y = 15 + rand.nextInt(35);
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		for (; y > 10; y--) {
			if (canBePlaced(world, mPos.setPos(x, y, z), rand)) {
				place(world, mPos.setPos(x, y, z), rand);
				WorldGenerator.SUCCESSES.add(ChunkPos.asLong(chunkX, chunkZ));
				return;
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos, Random rand) {
		return world.getBlockState(pos.down()).isSideSolid(world, pos, EnumFacing.UP) && WorldGenerator.STONE_TEST.apply(world.getBlockState(pos)) && world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos);
	}

	@Override
	public void place(World world, BlockPos pos, Random rand) {
		WeightedRandom.getRandomItem(rand, BRUTAL_SPAWNERS).place(world, pos);
		ChestBuilder.place(world, rand, pos.down(), rand.nextInt(9) == 0 ? DeadlyLoot.CHEST_VALUABLE : DeadlyLoot.SPAWNER_BRUTAL);
		world.setBlockState(pos.up(), Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), 2);
		for (EnumFacing f : EnumFacing.HORIZONTALS) {
			if (world.getBlockState(pos.offset(f)).getBlock().isReplaceable(world, pos.offset(f))) {
				PropertyBool side = (PropertyBool) Blocks.VINE.getBlockState().getProperty(f.getOpposite().getName());
				world.setBlockState(pos.offset(f), Blocks.VINE.getDefaultState().withProperty(side, true));
			}
		}
		WorldGenerator.debugLog(pos, "Brutal Spawner");
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

	private static NBTTagCompound getOrCreate(NBTTagCompound parent, String key) {
		if (!parent.hasKey(key, NBT.TAG_COMPOUND)) parent.setTag(key, new NBTTagCompound());
		return parent.getCompoundTag(key);
	}
}