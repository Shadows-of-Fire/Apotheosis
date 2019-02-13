package shadows.garden;

import java.util.Random;

import net.minecraft.block.BlockCactus;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCactusExt extends BlockCactus {

	public BlockCactusExt() {
		setHardness(0.4F);
		setSoundType(SoundType.CLOTH);
		setTranslationKey("cactus");
		setRegistryName(new ResourceLocation("cactus"));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isAreaLoaded(pos, 1)) return; // Forge: prevent growing cactus from loading unloaded chunks with block update
		BlockPos blockpos = pos.up();

		if (world.isAirBlock(blockpos)) {
			int i;

			for (i = 1; world.getBlockState(pos.down(i)).getBlock() == this; ++i)
				;

			if (i < GardenModule.maxCactusHeight) {
				int j = ((Integer) state.getValue(AGE)).intValue();

				if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(world, blockpos, state, true)) {
					if (j == 15) {
						world.setBlockState(blockpos, this.getDefaultState());
						IBlockState iblockstate = state.withProperty(AGE, Integer.valueOf(0));
						world.setBlockState(pos, iblockstate, 4);
						iblockstate.neighborChanged(world, blockpos, this, pos);
					} else {
						world.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(j + 1)), 4);
					}
					net.minecraftforge.common.ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
				}
			}
		}
	}

}
