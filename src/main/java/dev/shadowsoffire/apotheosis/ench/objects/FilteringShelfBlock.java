package dev.shadowsoffire.apotheosis.ench.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.ench.Ench;
import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import dev.shadowsoffire.placebo.recipe.VanillaPacketDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;

public class FilteringShelfBlock extends ChiseledBookShelfBlock implements IEnchantingBlock {

    public FilteringShelfBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Set<Enchantment> getBlacklistedEnchantments(BlockState state, LevelReader world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof FilteringShelfTile shelf) {
            Set<Enchantment> blacklist = new HashSet<>();
            for (ItemStack s : shelf.getBooks()) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(s);
                if (enchants.size() != 1) continue; // Only books with one enchantment are legal.
                Enchantment ench = enchants.keySet().stream().findFirst().orElse(null);
                if (ench != null) {
                    blacklist.add(ench);
                }
            }
            return blacklist;
        }
        return Collections.emptySet();
    }

    @Override
    public float getMaxEnchantingPower(BlockState state, LevelReader world, BlockPos pos) {
        return 30F;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilteringShelfTile shelf) {
            return shelf.getBooks().size() * 0.5F;
        }
        return 0;
    }

    @Override
    public float getArcanaBonus(BlockState state, LevelReader world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof FilteringShelfTile shelf) {
            return shelf.getBooks().size();
        }
        return 0;
    }

    @Override
    public ParticleOptions getTableParticle(BlockState state) {
        return Apoth.Particles.ENCHANT_WATER.get();
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof ChiseledBookShelfBlockEntity shelf) {
            Optional<Vec2> hitResult = getRelativeHitCoordinatesForBlockFace(pHit, pState.getValue(HorizontalDirectionalBlock.FACING));
            if (hitResult.isEmpty()) {
                return InteractionResult.PASS;
            }
            else {
                int slot = getHitSlot(hitResult.get());
                if (pState.getValue(SLOT_OCCUPIED_PROPERTIES.get(slot))) {
                    removeBook(pLevel, pPos, pPlayer, shelf, slot);
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }
                else {
                    ItemStack stack = pPlayer.getItemInHand(pHand);
                    if (canInsert(stack)) {
                        addBook(pLevel, pPos, pPlayer, shelf, stack, slot);
                        return InteractionResult.sidedSuccess(pLevel.isClientSide);
                    }
                    else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FilteringShelfTile(pPos, pState);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("info.apotheosis.filtering_shelf").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static boolean canInsert(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && EnchantedBookItem.getEnchantments(stack).size() == 1;
    }

    public static class FilteringShelfTile extends ChiseledBookShelfBlockEntity {

        public FilteringShelfTile(BlockPos pPos, BlockState pState) {
            super(pPos, pState);
        }

        @Override
        public boolean canPlaceItem(int pIndex, ItemStack pStack) {
            return canInsert(pStack);
        }

        @Override
        public BlockEntityType<?> getType() {
            return Ench.Tiles.FILTERING_SHELF.get();
        }

        public List<ItemStack> getBooks() {
            List<ItemStack> books = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                ItemStack stack = this.getItem(i);
                if (!stack.isEmpty()) books.add(stack);
            }
            return books;
        }

        @Override
        public CompoundTag getUpdateTag() {
            CompoundTag tag = new CompoundTag();
            this.saveAdditional(tag);
            return tag;
        }

        @Override
        public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
            this.load(pkt.getTag());
        }

        @Override
        public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        @Override
        public void setItem(int pSlot, ItemStack pStack) {
            super.setItem(pSlot, pStack);
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }

    }

}
