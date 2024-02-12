package dev.shadowsoffire.apotheosis.ench.table;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import dev.shadowsoffire.placebo.util.IReplacementBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ApothEnchantBlock extends EnchantmentTableBlock implements IReplacementBlock {

    public ApothEnchantBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(state -> 7).strength(5.0F, 1200.0F));
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof ApothEnchantTile) {
            Component itextcomponent = ((Nameable) tileentity).getDisplayName();
            return new SimpleMenuProvider((id, inventory, player) -> new ApothEnchantmentMenu(id, inventory, ContainerLevelAccess.create(world, pos), (ApothEnchantTile) tileentity), itextcomponent);
        }
        else {
            return null;
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ApothEnchantTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof ApothEnchantTile) {
                Block.popResource(world, pos, ((ApothEnchantTile) tileentity).inv.getStackInSlot(0));
                world.removeBlockEntity(pos);
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        for (BlockPos offset : BOOKSHELF_OFFSETS) {

            BlockState shelfState = level.getBlockState(pos.offset(offset));
            ((IEnchantingBlock) shelfState.getBlock()).spawnTableParticle(shelfState, level, rand, pos, offset);
        }

    }

}
