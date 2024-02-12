package dev.shadowsoffire.apotheosis.village.fletching;

import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class FletchingContainer extends AbstractContainerMenu {

    protected final CraftingContainer craftMatrix = new TransientCraftingContainer(this, 1, 3);
    protected final ResultContainer craftResult = new ResultContainer();
    protected final Level world;
    protected final BlockPos pos;
    protected final Player player;

    public FletchingContainer(int id, Inventory inv, Level world, BlockPos pos) {
        super(Apoth.Menus.FLETCHING.get(), id);
        this.world = world;
        this.pos = pos;
        this.player = inv.player;
        this.addSlot(new FletchingResultSlot(inv.player, this.craftMatrix, this.craftResult, 0, 124, 35));

        for (int i = 0; i < 3; ++i) {
            this.addSlot(new Slot(this.craftMatrix, i, 48, 17 + i * 18));
        }

        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(inv, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(inv, l, 8 + l * 18, 142));
        }

    }

    @SuppressWarnings("deprecation")
    public FletchingContainer(int id, Inventory inv) {
        this(id, inv, DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().level), BlockPos.ZERO);
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (!this.world.isClientSide) {
            ServerPlayer serverplayerentity = (ServerPlayer) this.player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<FletchingRecipe> optional = this.player.getServer().getRecipeManager().getRecipeFor(RecipeTypes.FLETCHING, this.craftMatrix, this.world);
            if (optional.isPresent()) {
                FletchingRecipe icraftingrecipe = optional.get();
                itemstack = icraftingrecipe.assemble(this.craftMatrix, this.player.level().registryAccess());
            }

            this.craftResult.setItem(0, itemstack);
            serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, 0, 0, itemstack));
        }
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.clearContainer(playerIn, this.craftMatrix);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.world.getBlockState(this.pos).getBlock() == Blocks.FLETCHING_TABLE && player.distanceToSqr(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 64;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                itemstack1.getItem().onCraftedBy(itemstack1, this.world, playerIn);
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            }
            else if (index >= 4 && index < 31) {
                if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 31 && index < 40) {
                if (!this.moveItemStackTo(itemstack1, 4, 31, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
            if (index == 0) {
                playerIn.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        return slotIn.container != this.craftResult && super.canTakeItemForPickAll(stack, slotIn);
    }

    protected class FletchingResultSlot extends ResultSlot {

        protected FletchingResultSlot(Player player, CraftingContainer inv, Container result, int slot, int x, int y) {
            super(player, inv, result, slot, x, y);
        }

        @Override
        public void onTake(Player thePlayer, ItemStack stack) {
            this.checkTakeAchievements(stack);
            net.minecraftforge.common.ForgeHooks.setCraftingPlayer(thePlayer);
            NonNullList<ItemStack> nonnulllist = thePlayer.level().getRecipeManager().getRemainingItemsFor(RecipeTypes.FLETCHING, FletchingContainer.this.craftMatrix, thePlayer.level());
            net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
            for (int i = 0; i < nonnulllist.size(); ++i) {
                ItemStack itemstack = FletchingContainer.this.craftMatrix.getItem(i);
                ItemStack itemstack1 = nonnulllist.get(i);
                if (!itemstack.isEmpty()) {
                    FletchingContainer.this.craftMatrix.removeItem(i, 1);
                    itemstack = FletchingContainer.this.craftMatrix.getItem(i);
                }

                if (!itemstack1.isEmpty()) {
                    if (itemstack.isEmpty()) {
                        FletchingContainer.this.craftMatrix.setItem(i, itemstack1);
                    }
                    else if (ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
                        itemstack1.grow(itemstack.getCount());
                        FletchingContainer.this.craftMatrix.setItem(i, itemstack1);
                    }
                    else if (!FletchingContainer.this.player.getInventory().add(itemstack1)) {
                        FletchingContainer.this.player.drop(itemstack1, false);
                    }
                }
            }

        }

    }
}
