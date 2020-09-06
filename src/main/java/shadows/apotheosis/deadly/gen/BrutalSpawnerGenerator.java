package shadows.apotheosis.deadly.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.NBT;
import shadows.apotheosis.deadly.DeadlyLoot;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.config.DeadlyConstants;
import shadows.apotheosis.util.TagBuilder;
import shadows.placebo.util.ChestBuilder;
import shadows.placebo.util.SpawnerBuilder;

/**
 * Rogue spawners that have stronger-than-usual mobs.
 * @author Shadows
 *
 */
public class BrutalSpawnerGenerator extends WeightedGenerator {

	public static final CompoundNBT BASE_TAG = new CompoundNBT();
	public static final List<SpawnerItem> BRUTAL_SPAWNERS = new ArrayList<>();

	@Override
	public boolean generate(IWorld world, int chunkX, int chunkZ, Random rand) {
		if (DeadlyConfig.brutalSpawnerChance <= rand.nextDouble()) return false;
		int x = (chunkX << 4) + MathHelper.nextInt(rand, 4, 12);
		int z = (chunkZ << 4) + MathHelper.nextInt(rand, 4, 12);
		int y = 15 + rand.nextInt(35);
		BlockPos.Mutable mPos = new BlockPos.Mutable(x, y, z);
		for (; y > 10; y--) {
			if (canBePlaced(world, mPos.setPos(x, y, z), rand)) {
				place(world, mPos.setPos(x, y, z), rand);
				DeadlyFeature.setSuccess(world.getDimension().getType().getRegistryName(), chunkX, chunkZ);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBePlaced(IWorld world, BlockPos pos, Random rand) {
		BlockState state = world.getBlockState(pos);
		BlockState downState = world.getBlockState(pos.down());
		BlockState upState = world.getBlockState(pos.up());
		return DeadlyFeature.STONE_TEST.test(downState) && upState.isAir(world, pos.up()) && (state.isAir(world, pos) || DeadlyFeature.STONE_TEST.test(state));
	}

	@Override
	public void place(IWorld world, BlockPos pos, Random rand) {
		WeightedRandom.getRandomItem(rand, BRUTAL_SPAWNERS).place(world, pos);
		ChestBuilder.place(world, rand, pos.down(), rand.nextInt(9) == 0 ? DeadlyLoot.CHEST_VALUABLE : DeadlyLoot.SPAWNER_BRUTAL);
		world.setBlockState(pos.up(), Blocks.CRACKED_STONE_BRICKS.getDefaultState(), 2);
		for (Direction f : Plane.HORIZONTAL) {
			if (world.getBlockState(pos.offset(f)).isAir(world, pos.offset(f))) {
				BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateContainer().getProperty(f.getOpposite().getName());
				world.setBlockState(pos.offset(f), Blocks.VINE.getDefaultState().with(side, true), 2);
			}
		}
		DeadlyFeature.debugLog(pos, "Brutal Spawner");
	}

	@Override
	public boolean isEnabled() {
		return !BRUTAL_SPAWNERS.isEmpty() && DeadlyConfig.brutalSpawnerChance > 0;
	}

	public static void init() {
		for (EffectInstance p : DeadlyConfig.BRUTAL_POTIONS) {
			TagBuilder.addPotionEffect(BrutalSpawnerGenerator.BASE_TAG, p.getPotion(), p.getAmplifier());
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
		for (INBT tag : item.getSpawner().getPotentials()) {
			applyBrutalStats(getOrCreate((CompoundNBT) tag, SpawnerBuilder.ENTITY));
		}
	}

	/**
	 * Copies brutal base stats from the base tag to the given entity tag.
	 */
	public static CompoundNBT applyBrutalStats(CompoundNBT tag) {
		TagBuilder.checkForSkeleton(tag);
		for (String name : BASE_TAG.keySet())
			tag.put(name, BASE_TAG.get(name).copy());
		return TagBuilder.checkForCreeper(tag);
	}

	private static CompoundNBT getOrCreate(CompoundNBT parent, String key) {
		if (!parent.contains(key, NBT.TAG_COMPOUND)) parent.put(key, new CompoundNBT());
		return parent.getCompound(key);
	}
}