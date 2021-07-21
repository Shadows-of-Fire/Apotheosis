package shadows.apotheosis.garden;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import shadows.placebo.util.IReplacementBlock;

public class ApothCactusBlock extends CactusBlock implements IReplacementBlock {

	public ApothCactusBlock() {
		super(AbstractBlock.Properties.copy(Blocks.CACTUS));
		this.setRegistryName(new ResourceLocation("cactus"));
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!world.isAreaLoaded(pos, 1)) return; // Forge: prevent growing cactus from loading unloaded chunks with block update
		if (!state.canSurvive(world, pos)) {
			world.destroyBlock(pos, true);
		} else {
			BlockPos blockpos = pos.above();
			if (pos.getY() != 255 && world.isEmptyBlock(blockpos)) {
				int i = 1;

				if (GardenModule.maxCactusHeight != 255) for (; world.getBlockState(pos.below(i)).getBlock() == this; ++i)
					;

				if (i < GardenModule.maxCactusHeight) {
					int j = state.getValue(AGE);

					if (ForgeHooks.onCropsGrowPre(world, blockpos, state, true)) {
						if (j == 15) {
							world.setBlockAndUpdate(blockpos, this.defaultBlockState());
							BlockState iblockstate = state.setValue(AGE, Integer.valueOf(0));
							world.setBlock(pos, iblockstate, 4);
							iblockstate.neighborChanged(world, blockpos, this, pos, false);
						} else {
							world.setBlock(pos, state.setValue(AGE, Integer.valueOf(j + 1)), 4);
						}
						ForgeHooks.onCropsGrowPost(world, pos, state);
					}
				}
			}
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