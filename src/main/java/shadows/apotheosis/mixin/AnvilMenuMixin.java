package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.inventory.AnvilMenu;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

	@ModifyConstant(method = "createResult()V")
	public int apoth_removeLevelCap(int old) {
		if (old == 40) return Integer.MAX_VALUE;
		return old;
	}

}
