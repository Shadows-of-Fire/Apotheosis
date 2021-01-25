package shadows.apotheosis.mixin;

import net.minecraft.block.BambooBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import shadows.apotheosis.garden.GardenModule;

@Mixin(BambooBlock.class)
public class MixinBambooBlock {
	@ModifyConstant(method = {
		"randomTick",
		"canGrow",
		"grow(Lnet/minecraft/world/server/ServerWorld;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
		"getNumBambooBlocksAbove",
		"getNumBambooBlocksBelow"
	}, constant = @Constant(intValue = 16))
	public int modifyMaxBambooHeight(int maxHeightIn) {
		return GardenModule.maxBambooHeight;
	}

	// This is a separate injection because the constant is 15 rather than 16 in source.
	@ModifyConstant(method = "grow(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;I)V", constant = @Constant(intValue = 15))
	public int modifyMaxBambooHeightGrow(int maxHeightIn) {
		return GardenModule.maxBambooHeight - 1;
	}

	@ModifyConstant(method = "grow(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;I)V", constant = @Constant(intValue = 11))
	public int modifyBambooSlowdownHeightThreshold(int slowdownThresholdIn) {
		// Return about 2/3 of the maximum height:
		// nudged upwards to be compatible with vanilla (returns 11 when maxBambooHeight = 16)
		return GardenModule.maxBambooHeight * 2 / 3 + 1;
	}
}
