package dev.shadowsoffire.apotheosis.affix.augmenting;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class AugmentingTableTile extends BlockEntity implements TickingBlockEntity, Clearable {

    public static int RISE_TIME = 30;
    public static int SPIN_CYCLE_TIME = 90;

    public int time = 0;
    public AnimationStage stage = AnimationStage.HIDING;

    protected InternalItemHandler inv = new InternalItemHandler(1){
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(Items.SIGIL_OF_ENHANCEMENT);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            AugmentingTableTile.this.setChanged();
        }
    };

    public AugmentingTableTile(BlockPos pPos, BlockState pBlockState) {
        super(Apoth.Tiles.AUGMENTING_TABLE, pPos, pBlockState);
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("inventory", this.inv);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("inventory", this.inv);
    }

    public ResourceHandler<ItemResource> getInventory() {
        return this.inv;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level == null) {
            return;
        }
        for (int i = 0; i < this.inv.size(); i++) {
            ItemResource res = this.inv.getResource(i);
            int amount = this.inv.getAmountAsInt(i);
            if (!res.isEmpty() && amount > 0) {
                Block.popResource(this.level, pos, res.toStack(amount));
            }
        }
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.inv.size(); i++) {
            this.inv.set(i, ItemResource.EMPTY, 0);
        }
    }

    public static enum AnimationStage {
        HIDING,
        RISING,
        FALLING,
        SPINNING;
    }

}
