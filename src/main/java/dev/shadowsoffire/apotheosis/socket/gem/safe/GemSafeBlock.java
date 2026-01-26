package dev.shadowsoffire.apotheosis.socket.gem.safe;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.menu.SimplerMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GemSafeBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final Component NAME = Apotheosis.lang("menu", "gem_safe");

    protected final BlockEntitySupplier<? extends GemSafeTile> tileSupplier;

    public GemSafeBlock(BlockEntitySupplier<? extends GemSafeTile> tileSupplier, BlockBehaviour.Properties props) {
        super(props);
        this.tileSupplier = tileSupplier;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return MenuUtil.openGui(player, pos, GemSafeMenu::new);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimplerMenuProvider<>(world, pos, GemSafeMenu::new);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return this.tileSupplier.create(pPos, pState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null) {
            te.saveToItem(s, level.registryAccess());
        }
        return s;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        BlockEntity be = level.getBlockEntity(pos);
        if (!data.isEmpty() && be instanceof GemSafeTile lib) {
            data.loadInto(lib, level.registryAccess());
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder ctx) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = ctx.getParameter(LootContextParams.BLOCK_ENTITY);
        if (te != null) {
            te.saveToItem(s, ctx.getLevel().registryAccess());
        }
        return Arrays.asList(s);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {

    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            world.removeBlockEntity(pos);
        }
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

}
