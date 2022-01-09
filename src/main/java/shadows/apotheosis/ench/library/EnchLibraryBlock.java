package shadows.apotheosis.ench.library;

import java.util.Arrays;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import shadows.placebo.container.SimplerMenuProvider;

public class EnchLibraryBlock extends HorizontalDirectionalBlock implements EntityBlock {

	public static final Component NAME = new TranslatableComponent("apotheosis.ench.library");

	public EnchLibraryBlock() {
		super(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(5.0F, 1200.0F));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (world.isClientSide) return InteractionResult.SUCCESS;
		NetworkHooks.openGui((ServerPlayer) player, this.getMenuProvider(state, world, pos), pos);
		return InteractionResult.CONSUME;
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
		return new SimplerMenuProvider<>(world, pos, EnchLibraryContainer::new);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
		return this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new EnchLibraryTile(pPos, pState);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		ItemStack s = new ItemStack(this);
		BlockEntity te = world.getBlockEntity(pos);
		if (te != null) s.getOrCreateTag().put("BlockEntityTag", te.saveWithoutMetadata());
		return s;
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te != null) {
			te.load(stack.getOrCreateTagElement("BlockEntityTag"));
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder ctx) {
		ItemStack s = new ItemStack(this);
		BlockEntity te = ctx.getParameter(LootContextParams.BLOCK_ENTITY);
		if (te != null) s.getOrCreateTag().put("BlockEntityTag", te.saveWithoutMetadata());
		return Arrays.asList(s);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter world, List<Component> list, TooltipFlag advanced) {
		CompoundTag tag = stack.getTagElement("BlockEntityTag");
		if (tag != null && tag.contains("Points")) {
			list.add(new TranslatableComponent("tooltip.enchlib.item", tag.getCompound("Points").size()).withStyle(ChatFormatting.GOLD));
		}
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != this) {
			world.removeBlockEntity(pos);
		}
	}

}
