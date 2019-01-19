package shadows.deadly.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.items.BossItem;

/**
 * Generates boss monsters with powerful stats, named gear, and incredible loot.
 * TODO: There may be a few issues relating to names being recycled very often.
 * @author Shadows
 *
 */
public class BossFeature extends WorldFeature {

	public static final List<BossItem> BOSS_ITEMS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos) {
		if (DeadlyConfig.bossChance <= world.rand.nextDouble()) return;
		int x = pos.getX() + world.rand.nextInt(16);
		int z = pos.getZ() + world.rand.nextInt(16);
		int y = world.rand.nextInt(30) + 12;
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		BossItem item = WeightedRandom.getRandomItem(world.rand, BOSS_ITEMS);
		for (byte state = 0; y > 5; y--) {
			if (world.getBlockState(mPos.setPos(x, y, z)).getBlockFaceShape(world, mPos, EnumFacing.UP) == BlockFaceShape.SOLID) {
				if (state == 0) {
					if (!world.checkBlockCollision(item.getAABB(world).offset(mPos.setPos(x, y + 1, z)))) {
						item.place(world, mPos);
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
		//NO-OP
		return false;
	}

	@Override
	public void place(World world, BlockPos pos) {
		//NO-OP
	}

	public static void init() {
		for (Pair<Integer, ResourceLocation> pair : DeadlyConfig.bossWeightedMobs) {
			BossItem i = new BossItem(pair.getLeft(), pair.getRight());
			BOSS_ITEMS.add(i);
		}
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.bossChance > 0;
	}
}