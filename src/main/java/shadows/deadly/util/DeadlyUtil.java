package shadows.deadly.util;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.placebo.util.PlaceboUtil;

public class DeadlyUtil {

	/**
	 * Covers up a trap, if random numbers an the config allow it.
	 */
	public static void coverTrap(World world, BlockPos pos) {
		pos = pos.up();
		if (world.rand.nextFloat() <= DeadlyConfig.coverChance && world.isAirBlock(pos)) {
			if (world.rand.nextFloat() <= DeadlyConfig.carpetChance) {
				PlaceboUtil.setBlockWithMeta(world, pos, Blocks.CARPET, 8, 2);
			} else {
				world.setBlockState(pos, Blocks.STONE_PRESSURE_PLATE.getDefaultState(), 2);
			}
		}
	}

}
