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
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
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
			MobSpawnerBaseLogic log = tile.spawnerLogic;
			boolean inverse = hand == EnumHand.MAIN_HAND && ItemStack.areItemsEqual(SpawnerModifiers.inverseItem, player.getHeldItemOffhand());
			if (ItemStack.areItemsEqual(stack, SpawnerModifiers.minDelay)) {
				log.minSpawnDelay += inverse ? 5 : -5;
				log.minSpawnDelay = MathHelper.clamp(log.minSpawnDelay, 0, Short.MAX_VALUE);
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.maxDelay)) {
				log.maxSpawnDelay += inverse ? 5 : -5;
				log.maxSpawnDelay = MathHelper.clamp(log.maxSpawnDelay, 10, Short.MAX_VALUE);
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.spawnCount)) {
				log.spawnCount += inverse ? -1 : 1;
				log.spawnCount = MathHelper.clamp(log.spawnCount, 1, Short.MAX_VALUE);
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.nearbyEntities)) {
				log.maxNearbyEntities += inverse ? -1 : 1;
				log.maxNearbyEntities = MathHelper.clamp(log.maxNearbyEntities, 0, Short.MAX_VALUE);
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.playerDist)) {
				log.activatingRangeFromPlayer += inverse ? -2 : 2;
				log.activatingRangeFromPlayer = MathHelper.clamp(log.activatingRangeFromPlayer, 0, Short.MAX_VALUE);
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.spawnRange)) {
				log.spawnRange += inverse ? -1 : 1;
				log.spawnRange = MathHelper.clamp(log.spawnRange, 0, Short.MAX_VALUE);
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.spawnConditions)) {
				tile.ignoresConditions = !inverse;
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.checkPlayers)) {
				tile.ignoresPlayers = !inverse;
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.ignoreCap)) {
				tile.ignoresCap = !inverse;
				stack.shrink(1);
				return true;
			} else if (ItemStack.areItemsEqual(stack, SpawnerModifiers.redstone)) {
				tile.redstoneEnabled = !inverse;
				stack.shrink(1);
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("spawner")) {
			NBTTagCompound tag = stack.getTagCompound().getCompoundTag("spawner");
			tooltip.add(I18n.format("spw.info.entity", EntityList.getTranslationName(new ResourceLocation(tag.getCompoundTag("SpawnData").getString("id")))));
			tooltip.add(I18n.format("spw.waila.mindelay", tag.getShort("MinSpawnDelay")));
			tooltip.add(I18n.format("spw.waila.maxdelay", tag.getShort("MaxSpawnDelay")));
			tooltip.add(I18n.format("spw.waila.spawncount", tag.getShort("SpawnCount")));
			tooltip.add(I18n.format("spw.waila.maxnearby", tag.getShort("MaxNearbyEntities")));
			tooltip.add(I18n.format("spw.waila.playerrange", tag.getShort("RequiredPlayerRange")));
			tooltip.add(I18n.format("spw.waila.spawnrange", tag.getShort("SpawnRange")));
			tooltip.add(I18n.format("spw.waila.ignoreplayers", tag.getBoolean("ignore_players")));
			tooltip.add(I18n.format("spw.waila.ignoreconditions", tag.getBoolean("ignore_conditions")));
			tooltip.add(I18n.format("spw.waila.ignorecap", tag.getBoolean("ignore_cap")));
			tooltip.add(I18n.format("spw.waila.redstone", tag.getBoolean("redstone_control")));
		}
	}

}
