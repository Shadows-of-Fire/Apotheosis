package shadows.apotheosis.ench.anvil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.block.AbstractBlock;
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
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext.Builder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.advancements.AdvancementTriggers;

public class ApothAnvilBlock extends AnvilBlock {

	public ApothAnvilBlock() {
		super(AbstractBlock.Properties.create(Material.ANVIL, MaterialColor.IRON).hardnessAndResistance(5.0F, 1200.0F).sound(SoundType.ANVIL));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new AnvilTile();
	}

	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
		ItemStack anvil = new ItemStack(this);
		if (te instanceof AnvilTile) {
			Map<Enchantment, Integer> ench = ((AnvilTile) te).getEnchantments();
			ench = ench.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			EnchantmentHelper.setEnchantments(ench, anvil);
		}
		spawnAsEntity(world, pos, anvil);
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof AnvilTile) {
			((AnvilTile) te).getEnchantments().putAll(EnchantmentHelper.getEnchantments(stack));
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.emptyList();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack anvil = new ItemStack(this);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof AnvilTile) {
			Map<Enchantment, Integer> ench = ((AnvilTile) te).getEnchantments();
			ench = ench.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			EnchantmentHelper.setEnchantments(ench, anvil);
		}
		return anvil;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (!stack.hasEffect()) tooltip.add(new TranslationTextComponent("info.apotheosis.anvil").mergeStyle(TextFormatting.GRAY));
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!ApotheosisObjects.ANVIL.isValidBlock(newState.getBlock())) {
			world.removeTileEntity(pos);
		}
	}

	@Override
	protected void onStartFalling(FallingBlockEntity e) {
		super.onStartFalling(e);
		TileEntity te = e.world.getTileEntity(new BlockPos(e.getPositionVec()));
		e.tileEntityData = new CompoundNBT();
		if (te instanceof AnvilTile) {
			te.write(e.tileEntityData);
		}
	}

	@Override
	public void onEndFalling(World world, BlockPos pos, BlockState fallState, BlockState hitState, FallingBlockEntity anvil) {
		super.onEndFalling(world, pos, fallState, hitState, anvil);
		List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)));
		if (anvil.tileEntityData == null) return;
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.deserializeEnchantments(anvil.tileEntityData.getList("enchantments", Constants.NBT.TAG_COMPOUND));
		int oblit = enchantments.getOrDefault(ApotheosisObjects.OBLITERATION, 0);
		int split = enchantments.getOrDefault(ApotheosisObjects.SPLITTING, 0);
		int ub = enchantments.getOrDefault(Enchantments.UNBREAKING, 0);
		if (split > 0 || oblit > 0) for (ItemEntity entity : items) {
			ItemStack stack = entity.getItem();
			if (stack.getItem() == Items.ENCHANTED_BOOK) {
				ListNBT enchants = EnchantedBookItem.getEnchantments(stack);
				boolean handled = false;
				if (enchants.size() == 1 && oblit > 0) {
					handled = handleObliteration(world, pos, oblit, entity, enchants);
				} else if (enchants.size() > 1 && split > 0) {
					handled = handleSplitting(world, pos, split, entity, enchants);
				}
				if (handled) {
					if (world.rand.nextInt(1 + ub) == 0) {
						BlockState dmg = damage(fallState);
						if (dmg == null) {
							world.setBlockState(pos, Blocks.AIR.getDefaultState());
							world.playEvent(Constants.WorldEvents.ANVIL_DESTROYED_SOUND, pos, 0);
						} else world.setBlockState(pos, dmg);
					}
					break;
				}
			}
		}
	}

	protected boolean handleSplitting(World world, BlockPos pos, int split, ItemEntity entity, ListNBT enchants) {
		if (world.rand.nextFloat() < 0.2F * split) {
			entity.remove();
			for (INBT nbt : enchants) {
				CompoundNBT tag = (CompoundNBT) nbt;
				int level = tag.getInt("lvl");
				Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(tag.getString("id")));
				ItemStack book = EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(enchant, level));
				Block.spawnAsEntity(world, pos.up(), book);
			}
			world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(5, 5, 5), EntityPredicates.NOT_SPECTATING).forEach(p -> {
				AdvancementTriggers.SPLIT_BOOK.trigger(p.getAdvancements());
			});
		}
		return true;
	}

	protected boolean handleObliteration(World world, BlockPos pos, int oblit, ItemEntity entity, ListNBT enchants) {
		if (world.rand.nextFloat() < 0.2F * oblit) {
			CompoundNBT nbt = enchants.getCompound(0);
			int level = nbt.getInt("lvl") - 1;
			if (level <= 0) return false;
			Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(nbt.getString("id")));
			ItemStack book = EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(enchant, level));
			entity.remove();
			Block.spawnAsEntity(world, pos.up(), book);
			Block.spawnAsEntity(world, pos.up(), book.copy());
		}
		return true;
	}
}