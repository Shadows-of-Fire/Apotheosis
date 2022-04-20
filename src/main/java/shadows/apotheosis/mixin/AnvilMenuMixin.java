package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.inventory.AnvilMenu;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

	@ModifyConstant(method = "createResult()V", constant = @Constant(intValue = 40))
	public int apoth_removeLevelCap(int old) {
		return Integer.MAX_VALUE;
	}

}
