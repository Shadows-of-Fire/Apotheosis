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

public class ApothCactusBlock extends CactusBlock {

	CactusBlock old = (CactusBlock) Blocks.CACTUS;

	public ApothCactusBlock() {
		super(AbstractBlock.Properties.from(Blocks.CACTUS));
		setRegistryName(new ResourceLocation("cactus"));
		this.setDefaultState(old.getDefaultState());
		this.getStateContainer().getValidStates().forEach(b -> b.instance = this);
		this.getStateContainer().owner = this;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!world.isAreaLoaded(pos, 1)) return; // Forge: prevent growing cactus from loading unloaded chunks with block update
		if (!state.isValidPosition(world, pos)) {
			world.destroyBlock(pos, true);
		} else {
			BlockPos blockpos = pos.up();
			if (pos.getY() != 255 && world.isAirBlock(blockpos)) {
				int i = 1;

				if (GardenModule.maxCactusHeight != 255) for (; world.getBlockState(pos.down(i)).getBlock() == this; ++i)
					;

				if (i < GardenModule.maxCactusHeight) {
					int j = state.get(AGE);

					if (ForgeHooks.onCropsGrowPre(world, blockpos, state, true)) {
						if (j == 15) {
							world.setBlockState(blockpos, getDefaultState());
							BlockState iblockstate = state.with(AGE, Integer.valueOf(0));
							world.setBlockState(pos, iblockstate, 4);
							iblockstate.neighborChanged(world, blockpos, this, pos, false);
						} else {
							world.setBlockState(pos, state.with(AGE, Integer.valueOf(j + 1)), 4);
						}
						ForgeHooks.onCropsGrowPost(world, pos, state);
					}
				}
			}
		}
	}

	@Override
	public StateContainer<Block, BlockState> getStateContainer() {
		return old.getStateContainer();
	}
}