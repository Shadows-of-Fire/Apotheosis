package dev.shadowsoffire.apotheosis.adventure.affix.salvaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Blocks;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Menus;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingRecipe.OutputData;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import dev.shadowsoffire.placebo.menu.FilteredSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SalvagingMenu extends BlockEntityMenu<SalvagingTableTile> {

    protected final Player player;
    protected final InternalItemHandler inputInv = new InternalItemHandler(12);

    public SalvagingMenu(int id, Inventory inv, BlockPos pos) {
        super(Menus.SALVAGE.get(), id, inv, pos);
        this.player = inv.player;
        int leftOffset = 17;
        int topOffset = 17;
        for (int i = 0; i < 12; i++) {
            this.addSlot(new UpdatingSlot(this.inputInv, i, leftOffset + i % 4 * 19, topOffset + i / 4 * 19, s -> findMatch(this.level, s) != null){

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }
            });
        }

        for (int i = 0; i < 6; i++) {
            leftOffset = 124;
            topOffset = 17;
            this.addSlot(new FilteredSlot(this.tile.output, i, leftOffset + i % 2 * 19, topOffset + i / 2 * 19, Predicates.alwaysFalse()));
        }

        this.addPlayerSlots(inv, 8, 92);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && findMatch(this.level, stack) != null, 0, 12);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
        this.registerInvShuffleRules();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.isClientSide) return true;
        return this.level.getBlockState(this.pos).getBlock() == Blocks.SALVAGING_TABLE.get();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!this.level.isClientSide) {
            this.clearContainer(player, new RecipeWrapper(this.inputInv));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            this.salvageAll();
            this.level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.99F, this.level.random.nextFloat() * 0.25F + 1F);
            this.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.34F, this.level.random.nextFloat() * 0.2F + 0.8F);
            this.level.playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.45F, this.level.random.nextFloat() * 0.5F + 0.75F);
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    protected void giveItem(Player player, ItemStack stack) {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer) player).hasDisconnected()) {
            player.drop(stack, false);
        }
        else {
            Inventory inventory = player.getInventory();
            if (inventory.player instanceof ServerPlayer) {
                inventory.placeItemBackInInventory(stack);
            }
        }
    }

    protected void salvageAll() {
        for (int inSlot = 0; inSlot < 12; inSlot++) {
            Slot s = this.getSlot(inSlot);
            ItemStack stack = s.getItem();
            List<ItemStack> outputs = salvageItem(this.level, stack);
            s.set(ItemStack.EMPTY);
            for (ItemStack out : outputs) {
                for (int outSlot = 0; outSlot < 6; outSlot++) {
                    if (out.isEmpty()) break;
                    out = this.tile.output.insertItem(outSlot, out, false);
                }
                if (!out.isEmpty()) this.giveItem(this.player, out);
            }
        }
    }

    public static int getSalvageCount(OutputData output, ItemStack stack, RandomSource rand) {
        int[] counts = getSalvageCounts(output, stack);
        return rand.nextInt(counts[0], counts[1] + 1);
    }

    public static int[] getSalvageCounts(OutputData output, ItemStack stack) {
        int[] out = { output.min, output.max };
        if (stack.isDamageableItem()) {
            out[1] = Math.max(out[0], Math.round(out[1] * (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage()));
        }
        return out;
    }

    public static List<ItemStack> salvageItem(Level level, ItemStack stack) {
        var recipe = findMatch(level, stack);
        if (recipe == null) return Collections.emptyList();
        List<ItemStack> outputs = new ArrayList<>();
        for (OutputData d : recipe.getOutputs()) {
            ItemStack out = d.stack.copy();
            out.setCount(getSalvageCount(d, stack, level.random));
            outputs.add(out);
        }
        return outputs;
    }

    public static List<ItemStack> getBestPossibleSalvageResults(Level level, ItemStack stack) {
        var recipe = findMatch(level, stack);
        if (recipe == null) return Collections.emptyList();
        List<ItemStack> outputs = new ArrayList<>();
        for (OutputData d : recipe.getOutputs()) {
            ItemStack out = d.stack.copy();
            out.setCount(getSalvageCounts(d, stack)[1]);
            outputs.add(out);
        }
        return outputs;
    }

    @Nullable
    public static SalvagingRecipe findMatch(Level level, ItemStack stack) {
        for (var recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypes.SALVAGING)) {
            if (recipe.matches(stack)) return recipe;
        }
        return null;
    }

}
