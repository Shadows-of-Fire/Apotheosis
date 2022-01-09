package shadows.apotheosis.ench.anvil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.util.INBTSensitiveFallingBlock;

public class ApothAnvilBlock extends AnvilBlock implements INBTSensitiveFallingBlock, EntityBlock {

	public ApothAnvilBlock() {
		super(BlockBehaviour.Properties.of(Material.HEAVY_METAL, MaterialColor.METAL).strength(5.0F, 1200.0F).sound(SoundType.ANVIL));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new AnvilTile(pPos, pState);
	}

	@Override
	public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity te, ItemStack stack) {
		ItemStack anvil = new ItemStack(this);
		if (te instanceof AnvilTile) {
			Map<Enchantment, Integer> ench = ((AnvilTile) te).getEnchantments();
			ench = ench.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			EnchantmentHelper.setEnchantments(ench, anvil);
		}
		popResource(world, pos, anvil);
		super.playerDestroy(world, player, pos, state, te, stack);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof AnvilTile) {
			((AnvilTile) te).getEnchantments().putAll(EnchantmentHelper.getEnchantments(stack));
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.emptyList();
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		ItemStack anvil = new ItemStack(this);
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof AnvilTile) {
			Map<Enchantment, Integer> ench = ((AnvilTile) te).getEnchantments();
			ench = ench.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			EnchantmentHelper.setEnchantments(ench, anvil);
		}
		return anvil;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter world, List<Component> tooltip, TooltipFlag flagIn) {
		if (!stack.hasFoil()) tooltip.add(new TranslatableComponent("info.apotheosis.anvil").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!Apoth.Tiles.ANVIL.isValid(newState)) {
			world.removeBlockEntity(pos);
		}
	}

	@Override
	protected void falling(FallingBlockEntity e) {
		super.falling(e);
		BlockEntity te = e.level.getBlockEntity(new BlockPos(e.position()));
		e.blockData = new CompoundTag();
		if (te instanceof AnvilTile) {
			e.blockData = te.saveWithFullMetadata();
		}
	}

	@Override
	public void onLand(Level world, BlockPos pos, BlockState fallState, BlockState hitState, FallingBlockEntity anvil) {
		super.onLand(world, pos, fallState, hitState, anvil);
		List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos.offset(1, 1, 1)));
		if (anvil.blockData == null) return;
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.deserializeEnchantments(anvil.blockData.getList("enchantments", Tag.TAG_COMPOUND));
		int oblit = enchantments.getOrDefault(Apoth.Enchantments.OBLITERATION, 0);
		int split = enchantments.getOrDefault(Apoth.Enchantments.SPLITTING, 0);
		int ub = enchantments.getOrDefault(Enchantments.UNBREAKING, 0);
		if (split > 0 || oblit > 0) for (ItemEntity entity : items) {
			ItemStack stack = entity.getItem();
			if (stack.getItem() == Items.ENCHANTED_BOOK) {
				ListTag enchants = EnchantedBookItem.getEnchantments(stack);
				boolean handled = false;
				if (enchants.size() == 1 && oblit > 0) {
					handled = this.handleObliteration(world, pos, entity, enchants);
				} else if (enchants.size() > 1 && split > 0) {
					handled = this.handleSplitting(world, pos, entity, enchants);
				}
				if (handled) {
					if (world.random.nextInt(1 + ub) == 0) {
						BlockState dmg = damage(fallState);
						if (dmg == null) {
							world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
							world.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, pos, 0);
						} else world.setBlockAndUpdate(pos, dmg);
					}
					break;
				}
			}
		}
	}

	protected boolean handleSplitting(Level world, BlockPos pos, ItemEntity entity, ListTag enchants) {
		entity.remove(RemovalReason.DISCARDED);
		for (Tag nbt : enchants) {
			CompoundTag tag = (CompoundTag) nbt;
			int level = tag.getInt("lvl");
			Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(tag.getString("id")));
			if (enchant == null) continue;
			ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, level));
			Block.popResource(world, pos.above(), book);
		}
		world.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(5, 5, 5), EntitySelector.NO_SPECTATORS).forEach(p -> {
			AdvancementTriggers.SPLIT_BOOK.trigger(p.getAdvancements());
		});
		return true;
	}

	protected boolean handleObliteration(Level world, BlockPos pos, ItemEntity entity, ListTag enchants) {
		CompoundTag nbt = enchants.getCompound(0);
		int level = nbt.getInt("lvl") - 1;
		if (level <= 0) return false;
		Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(nbt.getString("id")));
		if (enchant == null) return false;
		ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, level));
		entity.remove(RemovalReason.DISCARDED);
		Block.popResource(world, pos.above(), book);
		Block.popResource(world, pos.above(), book.copy());
		return true;
	}

	@Override
	public ItemStack toStack(BlockState state, CompoundTag tag) {
		ItemStack anvil = new ItemStack(this);
		Map<Enchantment, Integer> ench = EnchantmentHelper.deserializeEnchantments(tag.getList("enchantments", Tag.TAG_COMPOUND));
		ench = ench.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		EnchantmentHelper.setEnchantments(ench, anvil);
		return anvil;
	}
}