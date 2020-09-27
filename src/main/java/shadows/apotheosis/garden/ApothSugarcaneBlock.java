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

public class ApothSugarcaneBlock extends SugarCaneBlock {

	SugarCaneBlock old = (SugarCaneBlock) Blocks.SUGAR_CANE;

	public ApothSugarcaneBlock() {
		super(AbstractBlock.Properties.from(Blocks.SUGAR_CANE));
		setRegistryName(new ResourceLocation("sugar_cane"));
		this.setDefaultState(old.getDefaultState());
		this.getStateContainer().getValidStates().forEach(b -> b.instance = this);
		this.getStateContainer().owner = this;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (!state.isValidPosition(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
		} else if (worldIn.isAirBlock(pos.up())) {
			int i = 0;
			if (GardenModule.maxReedHeight != 255) for (i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; ++i)
				;

			if (i < GardenModule.maxReedHeight) {
				int j = state.get(AGE);
				if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, true)) {
					if (j == 15) {
						worldIn.setBlockState(pos.up(), this.getDefaultState());
						worldIn.setBlockState(pos, state.with(AGE, Integer.valueOf(0)), 4);
					} else {
						worldIn.setBlockState(pos, state.with(AGE, Integer.valueOf(j + 1)), 4);
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
	public StateContainer<Block, BlockState> getStateContainer() {
		return old.getStateContainer();
	}

}