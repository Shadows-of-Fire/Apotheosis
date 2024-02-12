package dev.shadowsoffire.apotheosis.garden;

import dev.shadowsoffire.placebo.util.IReplacementBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.common.ForgeHooks;

public class ApothCactusBlock extends CactusBlock implements IReplacementBlock {

    public ApothCactusBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.CACTUS));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockPos blockpos = pos.above();
        if (!world.isOutsideBuildHeight(blockpos) && world.isEmptyBlock(blockpos)) {
            int i = 1;

            if (GardenModule.maxCactusHeight <= 32) for (; world.getBlockState(pos.below(i)).getBlock() == this; ++i)
                ;

            if (i < GardenModule.maxCactusHeight) {
                int j = state.getValue(AGE);

                if (ForgeHooks.onCropsGrowPre(world, blockpos, state, true)) {
                    if (j == 15) {
                        world.setBlockAndUpdate(blockpos, this.defaultBlockState());
                        BlockState newState = state.setValue(AGE, Integer.valueOf(0));
                        world.setBlock(pos, newState, 4);
                        world.neighborChanged(newState, blockpos, this, pos, false);
                    }
                    else {
                        world.setBlock(pos, state.setValue(AGE, Integer.valueOf(j + 1)), 4);
                    }
                    ForgeHooks.onCropsGrowPost(world, pos, state);
                }
            }
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
