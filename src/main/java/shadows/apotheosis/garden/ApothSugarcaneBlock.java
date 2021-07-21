package shadows.apotheosis.garden;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import shadows.placebo.util.IReplacementBlock;

public class ApothSugarcaneBlock extends SugarCaneBlock implements IReplacementBlock {

	public ApothSugarcaneBlock() {
		super(AbstractBlock.Properties.copy(Blocks.SUGAR_CANE));
		this.setRegistryName(new ResourceLocation("sugar_cane"));
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (!state.canSurvive(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
		} else if (worldIn.isEmptyBlock(pos.above())) {
			int i = 0;
			if (GardenModule.maxReedHeight != 255) for (i = 1; worldIn.getBlockState(pos.below(i)).getBlock() == this; ++i)
				;

			if (i < GardenModule.maxReedHeight) {
				int j = state.getValue(AGE);
				if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, true)) {
					if (j == 15) {
						worldIn.setBlockAndUpdate(pos.above(), this.defaultBlockState());
						worldIn.setBlock(pos, state.setValue(AGE, Integer.valueOf(0)), 4);
					} else {
						worldIn.setBlock(pos, state.setValue(AGE, Integer.valueOf(j + 1)), 4);
					}
					ForgeHooks.onCropsGrowPost(worldIn, pos, state);
				}
			}
		}

	}

	@Override
	@Deprecated
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos origin, boolean isMoving) {
		if (pos.getY() != origin.getY()) {
			super.neighborChanged(state, world, pos, block, origin, isMoving);
		}
	}

	@Override
	public void _setDefaultState(BlockState state) {
		this.registerDefaultState(state);
	}

	protected StateContainer<Block, BlockState> container;

	@Override
	public void setStateContainer(StateContainer<Block, BlockState> container) {
		this.container = container;
	}

	@Override
	public StateContainer<Block, BlockState> getStateDefinition() {
		return this.container == null ? super.getStateDefinition() : this.container;
	}

}