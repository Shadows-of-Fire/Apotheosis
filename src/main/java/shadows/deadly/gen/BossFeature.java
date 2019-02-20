package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;

/**
 * Generates boss monsters with powerful stats, named gear, and incredible loot.
 * @author Shadows
 *
 */
public class BossFeature extends WorldFeature {

	public static final List<BossItem> BOSS_ITEMS = new ArrayList<>();

	@Override
	public void generate(World world, int chunkX, int chunkZ, Random rand) {
		if (DeadlyConfig.bossChance <= rand.nextDouble()) return;
		int x = (chunkX << 4) + MathHelper.getInt(rand, 4, 12);
		int z = (chunkZ << 4) + MathHelper.getInt(rand, 4, 12);
		int y = 15 + rand.nextInt(35);
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		BossItem item = WeightedRandom.getRandomItem(rand, BOSS_ITEMS);
		for (; y > 10; y--) {
			if (world.getBlockState(mPos.setPos(x, y, z)).getBlockFaceShape(world, mPos, EnumFacing.UP) == BlockFaceShape.SOLID) {
				if (!world.checkBlockCollision(item.getAABB(world).offset(mPos.setPos(x, y + 1, z)))) {
					item.place(world, mPos, rand);
					WorldGenerator.SUCCESSES.add(ChunkPos.asLong(chunkX, chunkZ));
					return;
				}
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos, Random rand) {
		return false;
	}

	@Override
	public void place(World world, BlockPos pos, Random rand) {
	}

	public static void init() {
		for (Pair<Integer, ResourceLocation> pair : DeadlyConfig.BOSS_MOBS) {
			BossItem i = new BossItem(pair.getLeft(), pair.getRight());
			BOSS_ITEMS.add(i);
		}
	}

	@Override
	public boolean isEnabled() {
		return !DeadlyConfig.BOSS_MOBS.isEmpty() && DeadlyConfig.bossChance > 0;
	}
}