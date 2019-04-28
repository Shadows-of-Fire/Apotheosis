package shadows.ench.anvil.compat;

import knightminer.inspirations.recipes.block.BlockSmashingAnvil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InspirationsCompat {

	public static void onEndFalling(World world, BlockPos pos, IBlockState fallState, IBlockState hitState) {
		BlockPos down = pos.down();
		if (!BlockSmashingAnvil.smashBlock(world, down, world.getBlockState(down))) {
			world.playEvent(1031, pos, 0);
		}
	}
}
