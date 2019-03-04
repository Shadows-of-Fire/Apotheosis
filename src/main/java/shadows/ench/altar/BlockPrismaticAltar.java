package shadows.ench.altar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import shadows.Apotheosis;

public class BlockPrismaticAltar extends Block {

	static final double px = 1D / 16;

	public static final AxisAlignedBB DISPLAY = new AxisAlignedBB(0, 0, 0, 1, 10 * px, 1);
	//Formatter::off
	public static final List<AxisAlignedBB> BOXES = ImmutableList.of(
			new AxisAlignedBB(0, 0, 0, 1, px, 1),
			new AxisAlignedBB(px, px, px, 1 - px, px * 3, 1 - px),
			new AxisAlignedBB(2 * px, 4 * px, 2 * px, 4 * px, 10 * px, 4 * px),
			new AxisAlignedBB(2 * px, 4 * px, 12 * px, 4 * px, 10 * px, 14 * px),
			new AxisAlignedBB(12 * px, 4 * px, 2 * px, 14 * px, 10 * px, 4 * px),
			new AxisAlignedBB(12 * px, 4 * px, 12 * px, 14 * px, 10 * px, 14 * px)
			);
	//Formatter::on
	public static final List<AxisAlignedBB> PILLARS = ImmutableList.of(BOXES.get(2), BOXES.get(3), BOXES.get(4), BOXES.get(5));

	public BlockPrismaticAltar() {
		super(Material.ROCK);
		setHardness(1.5F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setRegistryName(Apotheosis.MODID, "prismatic_altar");
		setTranslationKey(Apotheosis.MODID + ".prismatic_altar");
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return true;
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof TilePrismaticAltar)) return false;
		TilePrismaticAltar altar = (TilePrismaticAltar) te;
		Vec3d eyes = player.getPositionEyes(1);
		Vec3d look = player.getLook(1);
		double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
		Vec3d block = eyes.add(look.x * reach, look.y * reach, look.z * reach);

		for (int i = 0; i < 4; i++) {
			if (this.rayTrace(pos, eyes, block, PILLARS.get(i)) != null) return attemptSwap(altar, i, player, hand);
		}

		if (hitX >= 5.5 * px && hitX <= 1 - 5.5 * px || hitZ >= 5.5 * px && hitZ <= 1 - 5.5 * px) {
			attemptSwap(altar, 4, player, hand);
		}

		return true;
	}

	protected boolean attemptSwap(TilePrismaticAltar altar, int slot, EntityPlayer player, EnumHand hand) {
		ItemStackHandler inv = altar.getInv();
		ItemStack inAltar = inv.getStackInSlot(slot);
		ItemStack inHand = player.getHeldItem(hand);
		if (slot == 4 && !inHand.isEmpty()) return true;
		if (inAltar.isEmpty() && (inHand.isItemEnchanted() || inHand.getItem() == Items.ENCHANTED_BOOK)) {
			ItemStack toAltar = inHand.copy();
			inHand.shrink(1);
			toAltar.setCount(1);
			inv.setStackInSlot(slot, toAltar);
			altar.markAndNotify();
		} else if (!inAltar.isEmpty() && inHand.isEmpty()) {
			player.setHeldItem(hand, inAltar.copy());
			inAltar.setCount(0);
			altar.markAndNotify();
		}
		return true;
	}

	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
	}

	@Override
	@Deprecated
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		for (AxisAlignedBB bb : BOXES)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
		List<RayTraceResult> list = new ArrayList<>();

		for (AxisAlignedBB bb : BOXES)
			list.add(this.rayTrace(pos, start, end, bb));

		RayTraceResult result = null;
		double d1 = 0.0D;

		for (RayTraceResult raytraceresult : list) {
			if (raytraceresult != null) {
				double d0 = raytraceresult.hitVec.squareDistanceTo(end);

				if (d0 > d1) {
					result = raytraceresult;
					d1 = d0;
				}
			}
		}

		return result;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return DISPLAY;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TilePrismaticAltar();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

}
