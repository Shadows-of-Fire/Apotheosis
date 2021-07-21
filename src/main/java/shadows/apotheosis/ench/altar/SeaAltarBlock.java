package shadows.apotheosis.ench.altar;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.items.ItemStackHandler;

public class SeaAltarBlock extends Block {

	public static final VoxelShape DISPLAY = Block.box(0, 0, 0, 1, 10, 1);
	//Formatter::off
	public static final List<VoxelShape> BOXES = ImmutableList.of(
			Block.box(0, 0, 0, 16, 1, 16),
			Block.box(1, 1, 1, 15, 4, 15),
			Block.box(2, 4, 2, 4, 10, 4),
			Block.box(2, 4, 12, 4, 10, 14),
			Block.box(12, 4, 2, 14, 10, 4),
			Block.box(12, 4, 12, 14, 10, 14)
			);
	//Formatter::on
	public static final List<VoxelShape> PILLARS = ImmutableList.of(BOXES.get(2), BOXES.get(3), BOXES.get(4), BOXES.get(5));
	public static final VoxelShape SHAPE = merge(BOXES);

	public SeaAltarBlock() {
		super(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(1.5F, 10).sound(SoundType.STONE));
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (world.isClientSide) return ActionResultType.SUCCESS;
		TileEntity te = world.getBlockEntity(pos);
		if (!(te instanceof SeaAltarTile)) return ActionResultType.FAIL;
		SeaAltarTile altar = (SeaAltarTile) te;
		Vector3d eyes = player.getEyePosition(1);
		Vector3d look = player.getViewVector(1);
		double reach = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
		Vector3d block = eyes.add(look.x * reach, look.y * reach, look.z * reach);

		for (int i = 0; i < 4; i++) {
			if (this.rayTrace(pos, eyes, block, PILLARS.get(i)) != null) return this.attemptSwap(altar, i, player, hand);
		}

		double hitX = hit.getLocation().x - pos.getX();
		double hitZ = hit.getLocation().z - pos.getZ();

		if (hitX >= 5.5 / 16 && hitX <= 1 - 5.5 / 16 || hitZ >= 5.5 / 16 && hitZ <= 1 - 5.5 / 16) {
			this.attemptSwap(altar, 4, player, hand);
		}

		return ActionResultType.SUCCESS;
	}

	@Nullable
	protected RayTraceResult rayTrace(BlockPos pos, Vector3d start, Vector3d end, VoxelShape boundingBox) {
		BlockRayTraceResult result = boundingBox.clip(start, end, pos);
		return result == null ? null : new BlockRayTraceResult(result.getLocation().add(pos.getX(), pos.getY(), pos.getZ()), result.getDirection(), pos, result.isInside());
	}

	protected ActionResultType attemptSwap(SeaAltarTile altar, int slot, PlayerEntity player, Hand hand) {
		ItemStackHandler inv = altar.getInv();
		ItemStack inAltar = inv.getStackInSlot(slot);
		ItemStack inHand = player.getItemInHand(hand);
		if (slot == 4 && !inHand.isEmpty()) return ActionResultType.FAIL;
		if (inAltar.isEmpty() && (inHand.isEnchanted() || inHand.getItem() == Items.ENCHANTED_BOOK)) {
			ItemStack toAltar = inHand.copy();
			inHand.shrink(1);
			toAltar.setCount(1);
			inv.setStackInSlot(slot, toAltar);
			altar.markAndNotify();
			return ActionResultType.SUCCESS;
		} else if (!inAltar.isEmpty() && inHand.isEmpty()) {
			player.setItemInHand(hand, inAltar.copy());
			inAltar.setCount(0);
			altar.markAndNotify();
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.FAIL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SeaAltarTile();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return 8;
	}

	@Override
	@Deprecated
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		TileEntity te = world.getBlockEntity(pos);
		if (te instanceof SeaAltarTile) {
			ItemStackHandler inv = ((SeaAltarTile) te).inv;
			for (int i = 0; i < inv.getSlots(); i++) {
				Block.popResource(world, pos, inv.getStackInSlot(i));
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	private static VoxelShape merge(List<VoxelShape> shapes) {
		VoxelShape shape = shapes.get(0);
		for (VoxelShape s : shapes) {
			shape = VoxelShapes.joinUnoptimized(shape, s, IBooleanFunction.OR);
		}
		return shape;
	}

}