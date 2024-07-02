package dev.shadowsoffire.apotheosis.adventure.affix.augmenting;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class AugmentingTableTile extends BlockEntity implements TickingBlockEntity {

    public static int RISE_TIME = 30;
    public static int SPIN_CYCLE_TIME = 90;

    public int time = 0;
    public AnimationStage stage = AnimationStage.HIDING;

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
    public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {
        Player player = pLevel.getNearestPlayer(pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, 4, false);
        switch (this.stage) {
            case HIDING -> {
                if (player != null) {
                    this.stage = AnimationStage.RISING;
                    this.time = 0;
                }
            }
            case RISING -> {
                if (player != null) {
                    this.time++;
                    if (this.time >= RISE_TIME) {
                        this.stage = AnimationStage.SPINNING;
                        this.time = 0;
                    }
                }
                else {
                    this.stage = AnimationStage.FALLING;
                    // Keep the same time counter, falling operates on a backwards scale
                }
            }
            case FALLING -> {
                if (player != null) {
                    this.stage = AnimationStage.RISING;
                }
                else {
                    this.time--;
                    if (this.time <= 0) {
                        this.stage = AnimationStage.HIDING;
                        this.time = 0;
                    }
                }
            }
            case SPINNING -> {
                this.time++;
                if (player == null) {
                    if (this.time % SPIN_CYCLE_TIME == 0) {
                        this.stage = AnimationStage.FALLING;
                        this.time = RISE_TIME;
                    }
                }
            }
        }
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

    public static enum AnimationStage {
        HIDING,
        RISING,
        FALLING,
        SPINNING;
    }

}
