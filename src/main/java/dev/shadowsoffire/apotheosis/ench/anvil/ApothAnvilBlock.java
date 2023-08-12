package dev.shadowsoffire.apotheosis.ench.anvil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.advancements.AdvancementTriggers;
import dev.shadowsoffire.apotheosis.util.INBTSensitiveFallingBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class ApothAnvilBlock extends AnvilBlock implements INBTSensitiveFallingBlock, EntityBlock {

    public ApothAnvilBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundType.ANVIL));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AnvilTile(pPos, pState);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof AnvilTile anvil) {
            Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(stack);
            ench.keySet().removeIf(e -> !this.asItem().canApplyAtEnchantingTable(stack, e));
            anvil.getEnchantments().putAll(ench);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack anvil = new ItemStack(this);
        if (builder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof AnvilTile te) {
            Map<Enchantment, Integer> ench = te.getEnchantments();
            ench = ench.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            EnchantmentHelper.setEnchantments(ench, anvil);
        }
        return List.of(anvil);
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
        if (!stack.hasFoil()) tooltip.add(Component.translatable("info.apotheosis.anvil").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!Apoth.Tiles.ANVIL.get().isValid(newState)) {
            world.removeBlockEntity(pos);
        }
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
        if (isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight()) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            FallingBlockEntity e = FallingBlockEntity.fall(pLevel, pPos, pState);
            if (be instanceof AnvilTile anvil) {
                e.blockData = new CompoundTag();
                anvil.saveAdditional(e.blockData);
            }
            this.falling(e);
        }
    }

    @Override
    public void onLand(Level world, BlockPos pos, BlockState fallState, BlockState hitState, FallingBlockEntity anvil) {
        super.onLand(world, pos, fallState, hitState, anvil);
        List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos.offset(1, 1, 1)));
        if (anvil.blockData == null) return;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.deserializeEnchantments(anvil.blockData.getList("enchantments", Tag.TAG_COMPOUND));
        int oblit = enchantments.getOrDefault(dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.OBLITERATION.get(), 0);
        int split = enchantments.getOrDefault(dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.SPLITTING.get(), 0);
        int ub = enchantments.getOrDefault(Enchantments.UNBREAKING, 0);
        if (split > 0 || oblit > 0) for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
                ListTag enchants = EnchantedBookItem.getEnchantments(stack);
                boolean handled = false;
                if (enchants.size() == 1 && oblit > 0) {
                    handled = this.handleObliteration(world, pos, entity, enchants);
                }
                else if (enchants.size() > 1 && split > 0) {
                    handled = this.handleSplitting(world, pos, entity, enchants);
                }
                if (handled) {
                    if (world.random.nextInt(1 + ub) == 0) {
                        BlockState dmg = damage(fallState);
                        if (dmg == null) {
                            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            world.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, pos, 0);
                        }
                        else world.setBlockAndUpdate(pos, dmg);
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
