package shadows.apotheosis.ench.anvil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BookItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.ench.anvil.compat.IAnvilBlock;
import shadows.apotheosis.ench.anvil.compat.IAnvilTile;

public class BlockAnvilExt extends AnvilBlock implements IAnvilBlock {

	public BlockAnvilExt() {
		super(Block.Properties.create(Material.ANVIL, MaterialColor.IRON).hardnessAndResistance(5.0F, 1200.0F).sound(SoundType.ANVIL));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileAnvil();
	}

	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
		ItemStack anvil = new ItemStack(this);
		if (te instanceof IAnvilTile) {
			IAnvilTile anv = (IAnvilTile) te;
			Map<Enchantment, Integer> ench = new HashMap<>();
			if (anv.getUnbreaking() > 0) ench.put(Enchantments.UNBREAKING, anv.getUnbreaking());
			if (anv.getSplitting() > 0) ench.put(ApotheosisObjects.SPLITTING, anv.getSplitting());
			EnchantmentHelper.setEnchantments(ench, anvil);
		}
		spawnAsEntity(world, pos, anvil);
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IAnvilTile) {
			((IAnvilTile) te).setUnbreaking(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack));
			((IAnvilTile) te).setSplitting(EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SPLITTING, stack));
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.emptyList();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (!stack.hasEffect()) tooltip.add(new TranslationTextComponent("info.apotheosis.anvil"));
	}

	@Override
	protected void onStartFalling(FallingBlockEntity e) {
		super.onStartFalling(e);
		TileEntity te = e.world.getTileEntity(new BlockPos(e));
		e.tileEntityData = new CompoundNBT();
		if (te instanceof TileAnvil) {
			te.write(e.tileEntityData);
		}
	}

	@Override
	public void onEndFalling(World world, BlockPos pos, BlockState fallState, BlockState hitState) {
		super.onEndFalling(world, pos, fallState, hitState);

		List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)));
		FallingBlockEntity anvil = world.getEntitiesWithinAABB(FallingBlockEntity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1))).get(0);
		if (anvil.tileEntityData == null) return;
		int split = anvil.tileEntityData.getInt("splitting");
		int ub = anvil.tileEntityData.getInt("ub");
		if (split > 0) for (ItemEntity entity : items) {
			ItemStack stack = entity.getItem();
			if (stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() instanceof BookItem) {
				if (world.rand.nextInt(Math.max(1, 6 - split)) == 0) {
					ListNBT enchants = EnchantedBookItem.getEnchantments(stack);
					if (stack.getItem() instanceof BookItem) enchants = stack.getEnchantmentTagList();
					if (enchants.size() < 1) continue;
					entity.remove();
					for (INBT nbt : enchants) {
						CompoundNBT tag = (CompoundNBT) nbt;
						ItemStack book = EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(tag.getString("id"))), tag.getInt("lvl")));
						Block.spawnAsEntity(world, pos.up(), book);
					}
					world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(5, 5, 5), EntityPredicates.NOT_SPECTATING).forEach(p -> {
						AdvancementTriggers.SPLIT_BOOK.trigger(p.getAdvancements());
					});
				}
				if (world.rand.nextInt(1 + ub) == 0) {
					BlockState dmg = damage(fallState);
					if (dmg == null) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
						world.playEvent(1029, pos, 0);
					} else world.setBlockState(pos, dmg);
				}
				break;
			}
		}
	}
}