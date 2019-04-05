package shadows.ench;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;

public class BlockHellBookshelf extends Block {

	public static final PropertyInteger INFUSION = PropertyInteger.create("infusion", 0, 15);

	public BlockHellBookshelf(ResourceLocation name) {
		super(Material.ROCK, MapColor.BLACK);
		setHardness(2.0F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setTranslationKey(Apotheosis.MODID + ".hellshelf");
		setRegistryName(name);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == this) return 2 + state.getValue(INFUSION) * 0.2F;
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> info, ITooltipFlag flag) {
		info.add(I18n.format("info.apotheosis.hellshelf", String.valueOf(2 + Math.min(15, EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.HELL_INFUSION, stack)) * 0.2F).substring(0, 3)));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, INFUSION);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(INFUSION, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(INFUSION);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		ItemStack stack = placer.getHeldItem(hand);
		return getDefaultState().withProperty(INFUSION, Math.min(15, EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.HELL_INFUSION, stack)));
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ItemStack stack = new ItemStack(this);
		if (state.getValue(INFUSION) > 0) EnchantmentHelper.setEnchantments(ImmutableMap.of(ApotheosisObjects.HELL_INFUSION, state.getValue(INFUSION)), stack);
		drops.add(stack);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack stack = new ItemStack(this);
		if (state.getValue(INFUSION) > 0) EnchantmentHelper.setEnchantments(ImmutableMap.of(ApotheosisObjects.HELL_INFUSION, state.getValue(INFUSION)), stack);
		return stack;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

}
