package shadows.apotheosis.util;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public interface INBTSensitiveFallingBlock {

	public ItemStack toStack(BlockState state, CompoundNBT tag);

}
