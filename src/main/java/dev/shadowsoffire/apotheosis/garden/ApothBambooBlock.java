package dev.shadowsoffire.apotheosis.garden;

import dev.shadowsoffire.placebo.util.IReplacementBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BambooLeaves;

public class ApothBambooBlock extends BambooStalkBlock implements IReplacementBlock {

    public ApothBambooBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.BAMBOO));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            if (random.nextInt(3) == 0 && worldIn.isEmptyBlock(pos.above()) && worldIn.getRawBrightness(pos.above(), 0) >= 9) {
                int i = this.getHeightBelowUpToMax(worldIn, pos) + 1;
                if (i < GardenModule.maxBambooHeight) {
                    this.growBamboo(state, worldIn, pos, random, i);
                }
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
        int i = this.getHeightAboveUpToMax(worldIn, pos);
        int j = this.getHeightBelowUpToMax(worldIn, pos);
        return i + j + 1 < GardenModule.maxBambooHeight && worldIn.getBlockState(pos.above(i)).getValue(STAGE) != 1;
    }

    @Override
    public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        int bambooAbove = this.getHeightAboveUpToMax(worldIn, pos);
        int bambooBelow = this.getHeightBelowUpToMax(worldIn, pos);
        int bambooSize = bambooAbove + bambooBelow + 1;
        int l = 1 + rand.nextInt(2);

        for (int i1 = 0; i1 < l; ++i1) {
            BlockPos blockpos = pos.above(bambooAbove);
            BlockState blockstate = worldIn.getBlockState(blockpos);
            if (bambooSize >= GardenModule.maxBambooHeight || blockstate.getValue(STAGE) == 1 || !worldIn.isEmptyBlock(blockpos.above())) {
                return;
            }
            this.growBamboo(blockstate, worldIn, blockpos, rand, bambooSize);
            ++bambooAbove;
            ++bambooSize;
        }
    }

    @Override
    protected int getHeightAboveUpToMax(BlockGetter worldIn, BlockPos pos) {
        int i;
        for (i = 0; i < GardenModule.maxBambooHeight && worldIn.getBlockState(pos.above(i + 1)).getBlock() == Blocks.BAMBOO; ++i) {

        }
        return i;
    }

    @Override
    protected int getHeightBelowUpToMax(BlockGetter worldIn, BlockPos pos) {
        int i;
        for (i = 0; i < GardenModule.maxBambooHeight && worldIn.getBlockState(pos.below(i + 1)).getBlock() == Blocks.BAMBOO; ++i) {

        }
        return i;
    }

    @Override
    protected void growBamboo(BlockState blockStateIn, Level worldIn, BlockPos posIn, RandomSource rand, int size) {
        BlockState blockstate = worldIn.getBlockState(posIn.below());
        BlockPos blockpos = posIn.below(2);
        BlockState blockstate1 = worldIn.getBlockState(blockpos);
        BambooLeaves bambooleaves = BambooLeaves.NONE;
        if (size >= 1) {
            if (blockstate.getBlock() == Blocks.BAMBOO && blockstate.getValue(LEAVES) != BambooLeaves.NONE) {
                if (blockstate.getBlock() == Blocks.BAMBOO && blockstate.getValue(LEAVES) != BambooLeaves.NONE) {
                    bambooleaves = BambooLeaves.LARGE;
                    if (blockstate1.getBlock() == Blocks.BAMBOO) {
                        worldIn.setBlock(posIn.below(), blockstate.setValue(LEAVES, BambooLeaves.SMALL), 3);
                        worldIn.setBlock(blockpos, blockstate1.setValue(LEAVES, BambooLeaves.NONE), 3);
                    }
                }
            }
            else {
                bambooleaves = BambooLeaves.SMALL;
            }
        }

        int i = blockStateIn.getValue(AGE) != 1 && blockstate1.getBlock() != Blocks.BAMBOO ? 0 : 1;
        int j = (size < GardenModule.maxBambooHeight - GardenModule.maxBambooHeight / 5D || !(rand.nextFloat() < 0.25F)) && size != GardenModule.maxBambooHeight - 1 ? 0 : 1;
        worldIn.setBlock(posIn.above(), this.defaultBlockState().setValue(AGE, Integer.valueOf(i)).setValue(LEAVES, bambooleaves).setValue(STAGE, Integer.valueOf(j)), 3);
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
