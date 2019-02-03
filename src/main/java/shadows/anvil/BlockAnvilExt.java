package shadows.anvil;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockAnvil;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
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
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		ItemStack stack = new ItemStack(this);
		if (te instanceof TileAnvil && ((TileAnvil) te).getUnbreaking() > 0) {
			EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.UNBREAKING, ((TileAnvil) te).getUnbreaking()), stack);
		}
		spawnAsEntity(world, pos, stack);
		super.breakBlock(world, pos, state);
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
}