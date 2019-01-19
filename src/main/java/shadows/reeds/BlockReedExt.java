package shadows.reeds;

import java.util.Random;

import net.minecraft.block.BlockReed;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

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
		if (worldIn.getBlockState(pos.down()).getBlock() == this || this.checkForDrop(worldIn, pos, state)) {
			if (worldIn.isAirBlock(pos.up())) {
				int j = state.getValue(AGE);
				if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, true)) {
					if (j == 15) {
						worldIn.setBlockState(pos.up(), this.getDefaultState());
						worldIn.setBlockState(pos, state.withProperty(AGE, 0), 4);
					} else {
						worldIn.setBlockState(pos, state.withProperty(AGE, j + 1), 4);
					}
					ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
				}

			}
		}
	}

}
