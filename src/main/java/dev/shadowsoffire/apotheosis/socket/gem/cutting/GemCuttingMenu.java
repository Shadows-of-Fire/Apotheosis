package dev.shadowsoffire.apotheosis.socket.gem.cutting;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apoth.Menus;
import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe.CuttingRecipeInput;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GemCuttingMenu extends BlockEntityMenu<GemCuttingTableTile> {

    public static final int BASE_SLOT = 0;
    public static final int TOP_SLOT = 1;
    public static final int LEFT_SLOT = 2;
    public static final int RIGHT_SLOT = 3;

    protected final Player player;
    protected final InternalItemHandler inv;
    protected final CuttingRecipeInput rInput;

    @Nullable
    Runnable slotChangedCallback = null;

    public GemCuttingMenu(int id, Inventory playerInv, BlockPos pos) {
        super(Menus.GEM_CUTTING, id, playerInv, pos);
        this.player = playerInv.player;
        this.inv = this.tile.getInventory();
        this.rInput = new CuttingRecipeInput(this.inv);

        this.addSlot(new UpdatingSlot(this.inv, BASE_SLOT, 62, 45, this::isValidBase));
        this.addSlot(new UpdatingSlot(this.inv, TOP_SLOT, 62, 12, this::isValidTop));
        this.addSlot(new UpdatingSlot(this.inv, LEFT_SLOT, 33, 64, this::isValidLeft));
        this.addSlot(new UpdatingSlot(this.inv, RIGHT_SLOT, 90, 64, this::isValidRight));

        this.addPlayerSlots(playerInv, 8, 98);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidBase(stack) && !this.getSlot(BASE_SLOT).hasItem(), BASE_SLOT, TOP_SLOT);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidTop(stack), TOP_SLOT, LEFT_SLOT);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidLeft(stack), LEFT_SLOT, RIGHT_SLOT);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidRight(stack), RIGHT_SLOT, RIGHT_SLOT + 1);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
        this.registerInvShuffleRules();
    }

    public static List<RecipeHolder<GemCuttingRecipe>> getRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(RecipeTypes.GEM_CUTTING);
    }

    public boolean isAutoMode() {
        return this.tile.isAutoMode();
    }

    public boolean inverseAutoMode() {
        this.tile.setAutoMode(!this.tile.isAutoMode());
        return this.tile.isAutoMode();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            // Let the tile execute the recipe so GUI and automation share logic.
            if (this.tile.processOneRecipe()) {
                if (player instanceof ServerPlayer sp) {
                    ItemStack out = this.inv.getStackInSlot(BASE_SLOT);
                    Apoth.Triggers.GEM_CUTTING.trigger(sp, out);
                }
                return true;
            }
            return false;
        }
        if (id == 1) {
            // Toggle auto/manual on the server
            if (!this.level.isClientSide) {
                this.tile.setAutoMode(!this.tile.isAutoMode());
            }
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    public boolean isValidBase(ItemStack stack) {
        for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
            GemCuttingRecipe r = holder.value();
            if (r.isValidBaseItem(this.rInput, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidTop(ItemStack stack) {
        for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
            GemCuttingRecipe r = holder.value();
            if (r.isValidTopItem(this.rInput, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidLeft(ItemStack stack) {
        for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
            GemCuttingRecipe r = holder.value();
            if (r.isValidLeftItem(this.rInput, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidRight(ItemStack stack) {
        for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
            GemCuttingRecipe r = holder.value();
            if (r.isValidRightItem(this.rInput, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (this.level.isClientSide) {
            return true;
        }
        return this.level.getBlockState(this.pos).is(Blocks.GEM_CUTTING_TABLE);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.slotChangedCallback != null) {
            this.slotChangedCallback.run();
        }
    }

}
