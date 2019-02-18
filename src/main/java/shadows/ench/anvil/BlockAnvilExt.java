package shadows.ench.anvil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.ApotheosisObjects;

public class BlockAnvilExt extends BlockAnvil {

	public BlockAnvilExt() {
		setRegistryName("minecraft", "anvil");
		setHardness(5);
		setSoundType(SoundType.ANVIL);
		setResistance(2000);
		setTranslationKey("anvil");
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileAnvil();
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		ItemStack anvil = new ItemStack(this, 1, damageDropped(state));
		if (te instanceof TileAnvil) {
			TileAnvil anv = ((TileAnvil) te);
			Map<Enchantment, Integer> ench = new HashMap<>();
			if (anv.getUnbreaking() > 0) ench.put(Enchantments.UNBREAKING, anv.getUnbreaking());
			if (anv.getSplitting() > 0) ench.put(ApotheosisObjects.SPLITTING, anv.getSplitting());
			EnchantmentHelper.setEnchantments(ench, anvil);
		}
		spawnAsEntity(world, pos, anvil);
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileAnvil) {
			((TileAnvil) te).setUnbreaking(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack));
			((TileAnvil) te).setSplitting(EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SPLITTING, stack));
		}
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (!stack.hasEffect()) tooltip.add(I18n.format("info.apotheosis.anvil"));
	}

	@Override
	protected void onStartFalling(EntityFallingBlock e) {
		super.onStartFalling(e);
		TileEntity te = e.getWorldObj().getTileEntity(e.getOrigin());
		if (te instanceof TileAnvil) {
			e.tileEntityData = new NBTTagCompound();
			te.writeToNBT(e.tileEntityData);
		}
	}

	@Override
	public void onEndFalling(World world, BlockPos pos, IBlockState fallState, IBlockState hitState) {
		super.onEndFalling(world, pos, fallState, hitState);
		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)));
		EntityFallingBlock anvil = world.getEntitiesWithinAABB(EntityFallingBlock.class, new AxisAlignedBB(pos, pos.add(1, 1, 1))).get(0);
		int split = anvil.tileEntityData.getInteger("splitting");
		int ub = anvil.tileEntityData.getInteger("ub");
		if (split > 0) for (EntityItem entity : items) {
			ItemStack stack = ((EntityItem) entity).getItem();
			if (stack.getItem() == Items.ENCHANTED_BOOK) {
				if (world.rand.nextInt(Math.max(1, 6 - split)) == 0) {
					NBTTagList enchants = ItemEnchantedBook.getEnchantments(stack);
					entity.setDead();
					for (NBTBase nbt : enchants) {
						NBTTagCompound tag = (NBTTagCompound) nbt;
						ItemStack book = ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantment.getEnchantmentByID(tag.getInteger("id")), tag.getInteger("lvl")));
						Block.spawnAsEntity(world, pos, book);
					}
				}
				if (world.rand.nextInt(1 + ub) == 0) {
					int dmg = fallState.getValue(BlockAnvil.DAMAGE) + 1;
					if (dmg > 2) {
						world.setBlockToAir(pos);
						world.playEvent(1029, pos, 0);
					} else world.setBlockState(pos, fallState.withProperty(BlockAnvil.DAMAGE, dmg));
				}
				break;
			}
		}
	}
}