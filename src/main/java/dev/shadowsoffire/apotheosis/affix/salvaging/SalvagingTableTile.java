package dev.shadowsoffire.apotheosis.affix.salvaging;

import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SalvagingTableTile extends BlockEntity {

    public SalvagingTableTile(BlockPos pPos, BlockState pBlockState) {
        super(Apoth.Tiles.SALVAGING_TABLE, pPos, pBlockState);
    }

    /**
     * "Real" output inventory, as reflected in the container menu.
     */
    protected final InternalItemHandler output = new InternalItemHandler(6);
    protected final ResourceHandler<ItemResource> itemHandler = new SalvagingItemHandler();

    public ResourceHandler<ItemResource> getItemHandler() {
        return this.itemHandler;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putChild("output", this.output);
    }

    @Override
    public void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.readChild("output", this.output);
    }

    protected class SalvagingItemHandler implements ResourceHandler<ItemResource> {

        @Override
        public int size() {
            return 1 + SalvagingTableTile.this.output.size();
        }

        @Override
        public ItemResource getResource(int index) {
            if (index == 0) return ItemResource.EMPTY;
            return SalvagingTableTile.this.output.getResource(index - 1);
        }

        @Override
        public long getAmountAsLong(int index) {
            if (index == 0) return 0;
            return SalvagingTableTile.this.output.getAmountAsLong(index - 1);
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (index == 0) return isValid(index, resource) ? 1 : 0;
            return SalvagingTableTile.this.output.getCapacityAsLong(index - 1, resource);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (index == 0) {
                return !SalvagingMenu.findMatch(SalvagingTableTile.this.level, resource.toStack()).isEmpty();
            }
            return false;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 0 || amount <= 0) return 0;

            ItemStack inStack = resource.toStack(1);
            List<ItemStack> outputs = SalvagingMenu.getSalvageResults(SalvagingTableTile.this.level, inStack);
            if (outputs.isEmpty()) return 0;

            try (Transaction probe = Transaction.open(transaction)) {
                for (ItemStack out : outputs) {
                    int remaining = out.getCount();
                    ItemResource outRes = ItemResource.of(out);
                    for (int i = 0; i < 6 && remaining > 0; i++) {
                        remaining -= SalvagingTableTile.this.output.insert(i, outRes, remaining, probe);
                    }
                    if (remaining > 0) {
                        return 0; // Could not fit all outputs, abort.
                    }
                }
                probe.commit();
            }
            return 1;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index == 0) return 0;
            return SalvagingTableTile.this.output.extract(index - 1, resource, amount, transaction);
        }

    }

}
