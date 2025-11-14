package dev.shadowsoffire.apotheosis.socket.gem.cutting;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe.CuttingRecipeInput;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class GemCuttingTableTile extends BlockEntity {
    private static final int SOUND_COOLDOWN_TICKS = 20; // 1 second at 20 TPS
    protected final InternalItemHandler inv = new InternalItemHandler(4) {
        @Override
        public int getSlotLimit(int slot) {
            // Base slot holds a single gem.
            if (slot == GemCuttingMenu.BASE_SLOT) {
                return 1;
            }
            return super.getSlotLimit(slot);
        }
    };
    protected final IItemHandler itemHandler = new GemCuttingItemHandler();
    private final CuttingRecipeInput rInput = new CuttingRecipeInput(this.inv);
    private long lastSoundGameTime = Long.MIN_VALUE;
    private boolean autoMode = false;

    public GemCuttingTableTile(BlockPos pos, BlockState state) {
        super(Apoth.Tiles.GEM_CUTTING_TABLE, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GemCuttingTableTile tile) {
        if (level.isClientSide) {
            return;
        }
        if (!tile.autoMode) {
            return;
        }
        tile.processOneRecipe();
    }

    public boolean isAutoMode() {
        return this.autoMode;
    }

    public void setAutoMode(boolean autoMode) {
        if (this.autoMode == autoMode) {
            return;
        }
        this.autoMode = autoMode;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    public InternalItemHandler getInventory() {
        return this.inv;
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        tag.put("inv", this.inv.serializeNBT(regs));
        tag.putBoolean("autoMode", this.autoMode);
        super.saveAdditional(tag, regs);
    }

    private void playThrottledServerSound(Level level) {
        long now = level.getGameTime();

        if (this.lastSoundGameTime != Long.MIN_VALUE) {
            long delta = now - this.lastSoundGameTime;

            // If delta went negative, we hit an overflow or time went backwards.
            // In that case, just allow a sound and reset the timestamp.
            if (delta >= 0 && delta < SOUND_COOLDOWN_TICKS) {
                return;
            }
        }

        this.lastSoundGameTime = now;

        level.playSound(
                null,
                this.worldPosition,
                SoundEvents.AMETHYST_BLOCK_BREAK,
                SoundSource.BLOCKS,
                1.0F,
                1.5F + 0.35F * (1 - 2 * level.random.nextFloat())
        );
    }


    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        if (tag.contains("inv")) {
            this.inv.deserializeNBT(regs, tag.getCompound("inv"));
        }
        if (tag.contains("autoMode")) {                 // <--- new
            this.autoMode = tag.getBoolean("autoMode");
        }
        super.loadAdditional(tag, regs);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider regs) {
        CompoundTag tag = super.getUpdateTag(regs);
        tag.putBoolean("autoMode", this.autoMode);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider regs) {
        super.handleUpdateTag(tag, regs);
        if (tag.contains("autoMode")) {
            this.autoMode = tag.getBoolean("autoMode");
        }
    }

    /**
     * Attempts to process a single gem cutting recipe using the current inventory.
     *
     * @return True if a recipe was matched and processed, false otherwise.
     */
    public boolean processOneRecipe() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return false;
        }

        CuttingRecipeInput input = new CuttingRecipeInput(this.inv);

        for (RecipeHolder<GemCuttingRecipe> holder : GemCuttingMenu.getRecipes(level)) {
            GemCuttingRecipe recipe = holder.value();
            if (!recipe.matches(input, level)) {
                continue;
            }

            ItemStack out = recipe.assemble(input, level.registryAccess());
            recipe.decrementInputs(input, level);

            // Replace base with the output
            this.inv.setStackInSlot(GemCuttingMenu.BASE_SLOT, out);
            this.setChanged();


            if (this.isAutoMode()) {
                this.playThrottledServerSound(level);
            }


            return true;
        }

        return false;
    }

    private boolean validateSlot(ItemStack stack, SlotValidator validator) {
        Level level = this.level;
        if (level == null || stack.isEmpty()) {
            return false;
        }

        for (RecipeHolder<GemCuttingRecipe> holder : GemCuttingMenu.getRecipes(level)) {
            GemCuttingRecipe recipe = holder.value();
            if (validator.test(recipe, this.rInput, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidBase(ItemStack stack) {
        return validateSlot(stack, GemCuttingRecipe::isValidBaseItem);
    }

    private boolean isValidTop(ItemStack stack) {
        return validateSlot(stack, GemCuttingRecipe::isValidTopItem);
    }

    private boolean isValidLeft(ItemStack stack) {
        return validateSlot(stack, GemCuttingRecipe::isValidLeftItem);
    }

    private boolean isValidRight(ItemStack stack) {
        return validateSlot(stack, GemCuttingRecipe::isValidRightItem);
    }

    @FunctionalInterface
    private interface SlotValidator {
        boolean test(GemCuttingRecipe recipe, CuttingRecipeInput input, ItemStack stack);
    }

    protected class GemCuttingItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return GemCuttingTableTile.this.inv.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= getSlots()) {
                return ItemStack.EMPTY;
            }
            return GemCuttingTableTile.this.inv.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= getSlots()) {
                return stack;
            }
            if (stack.isEmpty()) {
                return stack;
            }
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return GemCuttingTableTile.this.inv.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // Only allow automated extraction from the BASE slot.
            if (slot != GemCuttingMenu.BASE_SLOT || amount <= 0) {
                return ItemStack.EMPTY;
            }

            ItemStack base = GemCuttingTableTile.this.inv.getStackInSlot(GemCuttingMenu.BASE_SLOT);
            if (base.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack top = GemCuttingTableTile.this.inv.getStackInSlot(GemCuttingMenu.TOP_SLOT);

            // If there is no top gem, nothing is being upgraded -> safe to extract.
            if (top.isEmpty()) {
                return GemCuttingTableTile.this.inv.extractItem(slot, amount, simulate);
            }

            // Interpret both as UnsocketedGem metadata.
            UnsocketedGem baseGem = UnsocketedGem.of(base);
            UnsocketedGem topGem = UnsocketedGem.of(top);

            // If either isn't a valid gem, be conservative and deny extraction.
            // (Should never happen in theory, but we never know)
            if (!baseGem.isValid() || !topGem.isValid()) {
                Apotheosis.LOGGER.warn("Invalid gem in slot {}: {} vs. {}", slot, baseGem, topGem);
                return ItemStack.EMPTY;
            }

            Purity basePurity = baseGem.purity();
            Purity topPurity = topGem.purity();

            // Only allow extraction if the base purity is strictly higher than the top purity.
            // With your enum ordering: CRACKED < CHIPPED < FLAWED < NORMAL < FLAWLESS < PERFECT
            // an upgrade will move from CRACKED -> CHIPPED, so base > top after upgrade.
            if (basePurity.ordinal() <= topPurity.ordinal()) {
                return ItemStack.EMPTY;
            }

            // At this point, we consider the gem "upgraded" and allow automation to pull it out.
            return GemCuttingTableTile.this.inv.extractItem(slot, amount, simulate);
        }


        @Override
        public int getSlotLimit(int slot) {
            if (slot < 0 || slot >= getSlots()) {
                return 0;
            }
            return GemCuttingTableTile.this.inv.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }

            return switch (slot) {
                case GemCuttingMenu.BASE_SLOT -> GemCuttingTableTile.this.isValidBase(stack);
                case GemCuttingMenu.TOP_SLOT -> GemCuttingTableTile.this.isValidTop(stack);
                case GemCuttingMenu.LEFT_SLOT -> GemCuttingTableTile.this.isValidLeft(stack);
                case GemCuttingMenu.RIGHT_SLOT -> GemCuttingTableTile.this.isValidRight(stack);
                default -> false;
            };
        }
    }

}
