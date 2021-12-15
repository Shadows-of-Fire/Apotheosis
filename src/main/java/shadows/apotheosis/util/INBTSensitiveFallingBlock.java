package shadows.apotheosis.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface INBTSensitiveFallingBlock {

	public ItemStack toStack(BlockState state, CompoundTag tag);

}
