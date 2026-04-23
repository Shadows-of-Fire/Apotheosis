package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.block_entity.TickingEntityBlock;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.menu.SimplerMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GemCaseBlock extends HorizontalDirectionalBlock implements TickingEntityBlock {

    public static final Component NAME = Apotheosis.lang("menu", "gem_safe");

    public static final VoxelShape SHAPE = Shapes.join(box(0, 0, 0, 16, 16, 16), box(1, 13, 1, 15, 15, 15), BooleanOp.ONLY_FIRST);

    protected final BlockEntitySupplier<? extends GemCaseTile> tileSupplier;
    protected final int maxCount;

    public GemCaseBlock(BlockEntitySupplier<? extends GemCaseTile> tileSupplier, BlockBehaviour.Properties props, int maxCount) {
        super(props);
        this.tileSupplier = tileSupplier;
        this.maxCount = maxCount;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return MenuUtil.openGui(player, pos, GemCaseMenu::new);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimplerMenuProvider<>(world, pos, GemCaseMenu::new);
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
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null && includeData) {
            saveBlockEntityToItem(te, s, level.registryAccess());
        }
        return s;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        BlockEntity be = level.getBlockEntity(pos);
        if (data != null && be instanceof GemCaseTile lib) {
            data.loadInto(lib, level.registryAccess());
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder ctx) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = ctx.getParameter(LootContextParams.BLOCK_ENTITY);
        if (te != null) {
            saveBlockEntityToItem(te, s, ctx.getLevel().registryAccess());
        }
        return Arrays.asList(s);
    }

    private static void saveBlockEntityToItem(BlockEntity be, ItemStack stack, HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(be.problemPath(), Apotheosis.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            be.saveCustomOnly(output);
            BlockItem.setBlockEntityData(stack, be.getType(), output);
            stack.applyComponents(be.collectComponents());
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    private static DecimalFormat f = new DecimalFormat("##.#");

    static String format(int n) {
        int log = (int) StrictMath.log10(n);
        if (log <= 3) {
            return String.valueOf(n);
        }
        else if (log <= 6) {
            return f.format(n / 1000D) + "K";
        }
        else if (log <= 8) {
            return f.format(n / 1000000D) + "M";
        }
        else {
            return f.format(n / 1000000000D) + "B";
        }
    }

}
