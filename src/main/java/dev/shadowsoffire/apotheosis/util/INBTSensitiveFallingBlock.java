package dev.shadowsoffire.apotheosis.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface INBTSensitiveFallingBlock {

    /**
     * Called from {@link FallingBlockEntity#spawnAtLocation}.<br>
     * Allows the Block to utilize {@link FallingBlockEntity#blockData} when determining what Item to drop.
     *
     * @param state The state of the falling block
     * @param tag   {@link FallingBlockEntity#blockData}
     * @return The itemstack to drop when the entity fails to place as a block.
     */
    public ItemStack toStack(BlockState state, CompoundTag tag);

}
