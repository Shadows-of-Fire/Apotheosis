package shadows.apotheosis.ench.altar;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.items.ItemStackHandler;

public class SeaAltarBlock extends BaseEntityBlock {

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
		super(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(1.5F, 10).sound(SoundType.STONE).lightLevel(s -> 8));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (world.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity te = world.getBlockEntity(pos);
		if (!(te instanceof SeaAltarTile)) return InteractionResult.FAIL;
		SeaAltarTile altar = (SeaAltarTile) te;
		Vec3 eyes = player.getEyePosition(1);
		Vec3 look = player.getViewVector(1);
		double reach = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
		Vec3 block = eyes.add(look.x * reach, look.y * reach, look.z * reach);

		for (int i = 0; i < 4; i++) {
			if (this.rayTrace(pos, eyes, block, PILLARS.get(i)) != null) return this.attemptSwap(altar, i, player, hand);
		}

		double hitX = hit.getLocation().x - pos.getX();
		double hitZ = hit.getLocation().z - pos.getZ();

		if (hitX >= 5.5 / 16 && hitX <= 1 - 5.5 / 16 || hitZ >= 5.5 / 16 && hitZ <= 1 - 5.5 / 16) {
			this.attemptSwap(altar, 4, player, hand);
		}

		return InteractionResult.SUCCESS;
	}

	@Nullable
	protected HitResult rayTrace(BlockPos pos, Vec3 start, Vec3 end, VoxelShape boundingBox) {
		BlockHitResult result = boundingBox.clip(start, end, pos);
		return result == null ? null : new BlockHitResult(result.getLocation().add(pos.getX(), pos.getY(), pos.getZ()), result.getDirection(), pos, result.isInside());
	}

	protected InteractionResult attemptSwap(SeaAltarTile altar, int slot, Player player, InteractionHand hand) {
		ItemStackHandler inv = altar.getInv();
		ItemStack inAltar = inv.getStackInSlot(slot);
		ItemStack inHand = player.getItemInHand(hand);
		if (slot == 4 && !inHand.isEmpty()) return InteractionResult.FAIL;
		if (inAltar.isEmpty() && (inHand.isEnchanted() || inHand.getItem() == Items.ENCHANTED_BOOK)) {
			ItemStack toAltar = inHand.copy();
			inHand.shrink(1);
			toAltar.setCount(1);
			inv.setStackInSlot(slot, toAltar);
			altar.markAndNotify();
			return InteractionResult.SUCCESS;
		} else if (!inAltar.isEmpty() && inHand.isEmpty()) {
			player.setItemInHand(hand, inAltar.copy());
			inAltar.setCount(0);
			altar.markAndNotify();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SeaAltarTile(pos, state);
	}

	@Override
	@Deprecated
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		BlockEntity te = world.getBlockEntity(pos);
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
			shape = Shapes.joinUnoptimized(shape, s, BooleanOp.OR);
		}
		return shape;
	}

}