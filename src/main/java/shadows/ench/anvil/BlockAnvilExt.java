package shadows.ench.anvil;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockAnvil;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		ItemStack anvil = new ItemStack(this);
		if (te instanceof TileAnvil && ((TileAnvil) te).getUnbreaking() > 0) {
			EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.UNBREAKING, ((TileAnvil) te).getUnbreaking()), anvil);
		}
		spawnAsEntity(world, pos, anvil);
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileAnvil) {
			((TileAnvil) te).setUnbreaking(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack));
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
		TileEntity te = e.getWorldObj().getTileEntity(e.getOrigin());
		if (te instanceof TileAnvil) {
			e.tileEntityData = new NBTTagCompound();
			e.tileEntityData.setInteger("ub", ((TileAnvil) te).getUnbreaking());
			e.getWorldObj().removeTileEntity(e.getOrigin());
		}
	}
}