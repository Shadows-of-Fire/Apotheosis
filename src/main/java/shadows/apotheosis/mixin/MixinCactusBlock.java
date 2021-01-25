package shadows.apotheosis.mixin;

import net.minecraft.block.CactusBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import shadows.apotheosis.garden.GardenModule;

@Mixin(CactusBlock.class)
public class MixinCactusBlock {
	@ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
	public int modifyMaxCactusHeight(int maxHeightIn) {
		int height = GardenModule.maxCactusHeight;
		return height == 255 ? Integer.MAX_VALUE : height;
	}
}
