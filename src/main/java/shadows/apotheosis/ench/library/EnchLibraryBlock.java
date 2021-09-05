package shadows.apotheosis.ench.library;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class EnchLibraryBlock extends HorizontalBlock {

	public static final ITextComponent NAME = new TranslationTextComponent("apotheosis.ench.library");

	public EnchLibraryBlock() {
		super(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(5.0F, 1200.0F));
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (world.isClientSide) return ActionResultType.SUCCESS;
		NetworkHooks.openGui((ServerPlayerEntity) player, this.getMenuProvider(state, world, pos), pos);
		return ActionResultType.CONSUME;
	}

	@Override
	public INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedContainerProvider((id, inv, player) -> new EnchLibraryContainer(id, inv, world, pos), NAME);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
		return this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection().getOpposite());
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EnchLibraryTile();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack s = new ItemStack(this);
		TileEntity te = world.getBlockEntity(pos);
		if (te != null) te.save(s.getOrCreateTagElement("BlockEntityTag"));
		return s;
	}

	@Override
	public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		TileEntity te = world.getBlockEntity(pos);
		if (te != null) {
			te.load(state, stack.getOrCreateTagElement("BlockEntityTag"));
			te.setPosition(pos);
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder ctx) {
		ItemStack s = new ItemStack(this);
		TileEntity te = ctx.getParameter(LootParameters.BLOCK_ENTITY);
		if (te != null) te.save(s.getOrCreateTagElement("BlockEntityTag"));
		return Arrays.asList(s);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader world, List<ITextComponent> list, ITooltipFlag advanced) {
		CompoundNBT tag = stack.getTagElement("BlockEntityTag");
		if (tag != null && tag.contains("Points")) {
			list.add(new TranslationTextComponent("tooltip.enchlib.item", tag.getCompound("Points").size()).withStyle(TextFormatting.GOLD));
		}
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != this) {
			world.removeBlockEntity(pos);
		}
	}

}
