package dev.shadowsoffire.apotheosis.village.fletching;

import dev.shadowsoffire.placebo.util.IReplacementBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FletchingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class ApothFletchingBlock extends FletchingTableBlock implements IReplacementBlock {

    public static final Component NAME = Component.translatable("apotheosis.recipes.fletching");

    public ApothFletchingBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).ignitedByLava().strength(2.5F).sound(SoundType.WOOD));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) return InteractionResult.SUCCESS;
        player.openMenu(this.getMenuProvider(state, worldIn, pos));
        return InteractionResult.CONSUME;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return What.getMenuProvider(state, world, pos);
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

    // I literally cannot fathom why this is necessary https://github.com/Shadows-of-Fire/Apotheosis/issues/441
    // TODO: Remove after update to FG 5 - appears to have been bug in SpecialSource.
    private static class What {

        static MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
            return new SimpleMenuProvider((id, inv, player) -> new FletchingContainer(id, inv, world, pos), NAME);
        }
    }
}
