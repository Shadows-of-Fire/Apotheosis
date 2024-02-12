package dev.shadowsoffire.apotheosis.garden;

import dev.shadowsoffire.placebo.util.IReplacementBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.common.ForgeHooks;

public class ApothSugarcaneBlock extends SugarCaneBlock implements IReplacementBlock {

    public ApothSugarcaneBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.SUGAR_CANE));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        if (worldIn.isEmptyBlock(pos.above())) {
            int i = 0;
            if (GardenModule.maxReedHeight <= 32) for (i = 1; worldIn.getBlockState(pos.below(i)).getBlock() == this; ++i)
                ;

            if (i < GardenModule.maxReedHeight) {
                int j = state.getValue(AGE);
                if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, true)) {
                    if (j == 15) {
                        worldIn.setBlockAndUpdate(pos.above(), this.defaultBlockState());
                        worldIn.setBlock(pos, state.setValue(AGE, Integer.valueOf(0)), 4);
                    }
                    else {
                        worldIn.setBlock(pos, state.setValue(AGE, Integer.valueOf(j + 1)), 4);
                    }
                    ForgeHooks.onCropsGrowPost(worldIn, pos, state);
                }
            }
        }

    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos origin, boolean isMoving) {
        if (pos.getY() != origin.getY()) {
            super.neighborChanged(state, world, pos, block, origin, isMoving);
        }
    }

    @Override
    public void _setDefaultState(BlockState state) {
        this.registerDefaultState(state);
    }

    protected StateDefinition<Block, BlockState> container;

    @Override
    public void setStateContainer(StateDefinition<Block, BlockState> container) {
        this.container = container;
    }

    @Override
    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.container == null ? super.getStateDefinition() : this.container;
    }

}
