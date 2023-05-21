package shadows.apotheosis.adventure.affix.reforging;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.block_entity.TickingEntityBlock;
import shadows.placebo.container.ContainerUtil;
import shadows.placebo.container.SimplerMenuProvider;

public class ReforgingTableBlock extends Block implements TickingEntityBlock {
	public static final Component TITLE = Component.translatable("container.apotheosis.reforge");
	public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

	protected final LootRarity maxRarity;

	public ReforgingTableBlock(BlockBehaviour.Properties properties, LootRarity maxRarity) {
		super(properties);
		this.maxRarity = maxRarity;
	}

	public LootRarity getMaxRarity() {
		return this.maxRarity;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState pState) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		return ContainerUtil.openGui(player, pos, ReforgingMenu::new);
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
		return new SimplerMenuProvider<>(world, pos, ReforgingMenu::new);
	}

	@Override
	public void appendHoverText(ItemStack pStack, BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
		list.add(Component.translatable(Apoth.Blocks.REFORGING_TABLE.get().getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
		if (this.maxRarity != LootRarity.ANCIENT) list.add(Component.translatable(Apoth.Blocks.REFORGING_TABLE.get().getDescriptionId() + ".desc2", this.getMaxRarity().toComponent()).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new ReforgingTableTile(pPos, pState);
	}

	@Override
	@Deprecated
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == this && newState.getBlock() == this) return;
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof ReforgingTableTile ref) {
			for (int i = 0; i < ref.inv.getSlots(); i++) {
				popResource(world, pos, ref.inv.getStackInSlot(i));
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}
}