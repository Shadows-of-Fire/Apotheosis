package shadows.apotheosis.deadly.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import shadows.apotheosis.deadly.config.DeadlyConfig;

/**
 * Generates boss monsters with powerful stats, named gear, and incredible loot.
 * @author Shadows
 *
 */
public class BossGenerator extends WeightedGenerator {

	public static final List<BossItem> BOSS_ITEMS = new ArrayList<>();

	@Override
	public boolean generate(IWorld world, int chunkX, int chunkZ, Random rand) {
		if (DeadlyConfig.bossChance <= rand.nextDouble()) return false;
		int x = (chunkX << 4) + MathHelper.nextInt(rand, 4, 12);
		int z = (chunkZ << 4) + MathHelper.nextInt(rand, 4, 12);
		int y = 15 + rand.nextInt(35);
		BlockPos.Mutable mPos = new BlockPos.Mutable(x, y, z);
		BossItem item = WeightedRandom.getRandomItem(rand, BOSS_ITEMS);
		for (; y > 10; y--) {
			if (Block.hasEnoughSolidSide(world, mPos.setPos(x, y, z), Direction.UP)) {
				if (world.doesNotCollide(item.getAABB(world).offset(mPos.setPos(x, y + 1, z)))) {
					item.place(world, mPos, rand);
					DeadlyFeature.setSuccess(world.getDimension().getType().getRegistryName(), chunkX, chunkZ);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canBePlaced(IWorld world, BlockPos pos, Random rand) {
		return false;
	}

	@Override
	public void place(IWorld world, BlockPos pos, Random rand) {
	}

	public static void init() {
		for (Pair<Integer, ResourceLocation> pair : DeadlyConfig.BOSS_MOBS) {
			BossItem i = new BossItem(pair.getLeft(), pair.getRight());
			BOSS_ITEMS.add(i);
		}
	}

	@Override
	public boolean isEnabled() {
		return !BOSS_ITEMS.isEmpty() && DeadlyConfig.bossChance > 0;
	}
}