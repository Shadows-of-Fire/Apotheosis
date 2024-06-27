package dev.shadowsoffire.apotheosis.adventure.affix.augmenting;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class AugmentingTableTile extends BlockEntity implements TickingBlockEntity {

    protected InternalItemHandler inv = new InternalItemHandler(1){
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(Items.SIGIL_OF_ENHANCEMENT.get());
        };

        @Override
        protected void onContentsChanged(int slot) {
            AugmentingTableTile.this.setChanged();
        };
    };

    public AugmentingTableTile(BlockPos pPos, BlockState pBlockState) {
        super(Apoth.Tiles.AUGMENTING_TABLE.get(), pPos, pBlockState);
    }

    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state) {
        TickingBlockEntity.super.clientTick(level, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", this.inv.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inv.deserializeNBT(tag.getCompound("inventory"));
    }

    LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> this.inv);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return this.invCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.invCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.invCap = LazyOptional.of(() -> this.inv);
    }

}
