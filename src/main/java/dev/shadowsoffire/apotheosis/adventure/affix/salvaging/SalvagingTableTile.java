package dev.shadowsoffire.apotheosis.adventure.affix.salvaging;

import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class SalvagingTableTile extends BlockEntity {

    public SalvagingTableTile(BlockPos pPos, BlockState pBlockState) {
        super(Apoth.Tiles.SALVAGING_TABLE.get(), pPos, pBlockState);
    }

    /**
     * "Real" output inventory, as reflected in the container menu.
     */
    protected final InternalItemHandler output = new InternalItemHandler(6);

    /**
     * External-facing inventory handler, which automatically salvages input items.
     */
    protected final LazyOptional<SalvagingItemHandler> itemHandler = LazyOptional.of(SalvagingItemHandler::new);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return this.itemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("output", this.output.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("output")) this.output.deserializeNBT(tag.getCompound("output"));
        super.load(tag);
    }

    protected class SalvagingItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1 + SalvagingTableTile.this.output.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0) return ItemStack.EMPTY;
            else return SalvagingTableTile.this.output.getStackInSlot(slot - 1);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0) return stack;
            List<ItemStack> outputs = SalvagingMenu.getBestPossibleSalvageResults(SalvagingTableTile.this.level, stack);
            if (outputs.isEmpty()) return stack;
            IntSet skipSlots = new IntOpenHashSet();
            // Simulate inserting all outputs.
            for (ItemStack out : outputs) {
                // I've made an assumption with this logic that a Salvaging Recipe won't have two stacks with the same item in the output.
                // Thus, if the size changes, we can assume that part of that stack fit in that slot, and that any further insertions would fail.
                for (int i = 0; i < 6; i++) {
                    if (skipSlots.contains(i)) continue;
                    int size = out.getCount();
                    out = SalvagingTableTile.this.output.insertItem(i, out, true); // Always simulate during this check.
                    if (size != out.getCount()) skipSlots.add(i);
                    if (out.isEmpty()) break;
                }
                if (!out.isEmpty()) return stack; // If any output fails to insert to the output inventory, we abort.
            }
            // Now, if we passed the checks we aren't simulating, do the actual insertion.
            if (!simulate) {
                for (ItemStack out : outputs) {
                    for (int i = 0; i < 6; i++) {
                        out = SalvagingTableTile.this.output.insertItem(i, out, false);
                        if (out.isEmpty()) break;
                    }
                    if (!out.isEmpty()) return stack; // If any output fails to insert to the output inventory, we abort.
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0) return ItemStack.EMPTY;
            return SalvagingTableTile.this.output.extractItem(slot - 1, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == 0) return 1;
            return SalvagingTableTile.this.output.getSlotLimit(slot - 1);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) return SalvagingMenu.findMatch(SalvagingTableTile.this.level, stack) != null;
            return false;
        }

    }

}
