package shadows.spawn;

import java.util.List;

import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSpawnerExt extends BlockMobSpawner {

	public BlockSpawnerExt() {
		setRegistryName("minecraft", "mob_spawner");
		setHardness(5.0F);
		setSoundType(SoundType.METAL);
		setTranslationKey("mobSpawner");
		disableStats();
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack s = new ItemStack(this);
		TileEntity te = world.getTileEntity(pos);
		if (te != null) te.writeToNBT(s.getOrCreateSubCompound("spawner"));
		return s;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null) {
			te.readFromNBT(stack.getOrCreateSubCompound("spawner"));
			te.setPos(pos);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
			ItemStack s = new ItemStack(this);
			if (te != null) te.writeToNBT(s.getOrCreateSubCompound("spawner"));
			spawnAsEntity(world, pos, s);
			player.getHeldItemMainhand().damageItem(35, player);
		}
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		this.onBlockHarvested(world, pos, state, player);
		if (player.capabilities.isCreativeMode) return world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
		return willHarvest;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileSpawnerExt();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return true;
		ItemStack stack = player.getHeldItem(hand);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileSpawnerExt) {
			TileSpawnerExt tile = (TileSpawnerExt) te;
			boolean inverse = SpawnerModifiers.inverseItem.apply(player.getHeldItem(hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
			for (SpawnerModifier sm : SpawnerModifiers.MODIFIERS) {
				if (sm.matches(stack)) {
					sm.modify(tile, inverse);
					stack.shrink(1);
					return sm.returnVal();
				}
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("spawner", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound tag = stack.getTagCompound().getCompoundTag("spawner");
			tooltip.add(I18n.format("info.spw.entity", EntityList.getTranslationName(new ResourceLocation(tag.getCompoundTag("SpawnData").getString("id")))));
			tooltip.add(I18n.format("waila.spw.mindelay", tag.getShort("MinSpawnDelay")));
			tooltip.add(I18n.format("waila.spw.maxdelay", tag.getShort("MaxSpawnDelay")));
			tooltip.add(I18n.format("waila.spw.spawncount", tag.getShort("SpawnCount")));
			tooltip.add(I18n.format("waila.spw.maxnearby", tag.getShort("MaxNearbyEntities")));
			tooltip.add(I18n.format("waila.spw.playerrange", tag.getShort("RequiredPlayerRange")));
			tooltip.add(I18n.format("waila.spw.spawnrange", tag.getShort("SpawnRange")));
			tooltip.add(I18n.format("waila.spw.ignoreplayers", tag.getBoolean("ignore_players")));
			tooltip.add(I18n.format("waila.spw.ignoreconditions", tag.getBoolean("ignore_conditions")));
			tooltip.add(I18n.format("waila.spw.ignorecap", tag.getBoolean("ignore_cap")));
			tooltip.add(I18n.format("waila.spw.redstone", tag.getBoolean("redstone_control")));
		}
	}

}
