package dev.shadowsoffire.apotheosis.socket.gem.cutting;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apoth.Menus;
import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe.CuttingRecipeInput;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.PlaceboContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public class GemCuttingMenu extends PlaceboContainerMenu {

    public static final int BASE_SLOT = 0;
    public static final int TOP_SLOT = 1;
    public static final int LEFT_SLOT = 2;
    public static final int RIGHT_SLOT = 3;

    protected final Player player;
    protected final ContainerLevelAccess access;
    protected final InternalItemHandler inv = new InternalItemHandler(4){
        @Override
        public int getSlotLimit(int slot) {
            return slot == BASE_SLOT ? 1 : super.getSlotLimit(slot);
        };
    };
    protected final CuttingRecipeInput rInput = new CuttingRecipeInput(this.inv);
    @Nullable
    Runnable slotChangedCallback = null;

    public GemCuttingMenu(int id, Inventory playerInv) {
        this(id, playerInv, ContainerLevelAccess.NULL);
    }

    public GemCuttingMenu(int id, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.GEM_CUTTING, id, playerInv);
        this.player = playerInv.player;
        this.access = access;
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

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
                GemCuttingRecipe r = holder.value();
                if (r.matches(this.rInput, player.level())) {
                    ItemStack out = r.assemble(this.rInput, player.level().registryAccess());
                    r.decrementInputs(this.rInput, player.level());
                    this.inv.setStackInSlot(0, out);
                    this.level.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1, 1.5F + 0.35F * (1 - 2 * this.level.random.nextFloat()));
                    Apoth.Triggers.GEM_CUTTING.trigger((ServerPlayer) player, out);
                    return true;
                }
            }
        }
        return false;
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
        return this.access.evaluate((level, pos) -> level.getBlockState(pos).is(Blocks.GEM_CUTTING_TABLE), true);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.access.execute((level, pos) -> {
            this.clearContainer(pPlayer, this.inv);
        });
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.slotChangedCallback != null) {
            this.slotChangedCallback.run();
        }
    }

    public static int getDustCost(Purity purity) {
        return 1 + purity.ordinal() * 2;
    }

    public static List<RecipeHolder<GemCuttingRecipe>> getRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(RecipeTypes.GEM_CUTTING);
    }
}
