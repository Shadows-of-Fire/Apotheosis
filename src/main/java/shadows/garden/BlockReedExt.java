package shadows.garden;

import java.util.Random;

import net.minecraft.block.BlockReed;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockReedExt extends BlockReed {

	public BlockReedExt() {
		setHardness(0.0F);
		setSoundType(SoundType.PLANT);
		setTranslationKey("reeds");
		disableStats();
		setRegistryName(new ResourceLocation("reeds"));
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn.getBlockState(pos.down()).getBlock() == Blocks.REEDS || this.checkForDrop(worldIn, pos, state)) {
			if (worldIn.isAirBlock(pos.up())) {
				int i;

				for (i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; ++i) {
					;
				}

				if (i < GardenModule.maxReedHeight) {
					int j = ((Integer) state.getValue(AGE)).intValue();

					if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, true)) {
						if (j == 15) {
							worldIn.setBlockState(pos.up(), this.getDefaultState());
							worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(0)), 4);
						} else {
							worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(j + 1)), 4);
						}
						net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
					}
				}
			}
		}
	}

}
