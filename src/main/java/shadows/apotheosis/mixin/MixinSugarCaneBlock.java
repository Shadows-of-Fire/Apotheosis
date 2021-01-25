package shadows.apotheosis.mixin;

import net.minecraft.block.SugarCaneBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import shadows.apotheosis.garden.GardenModule;

@Mixin(SugarCaneBlock.class)
public class MixinSugarCaneBlock {
	@ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
	public int modifyMaxSugarCaneHeight(int maxHeightIn) {
		int height = GardenModule.maxReedHeight;
		return height == 255 ? Integer.MAX_VALUE : height;
	}
}
