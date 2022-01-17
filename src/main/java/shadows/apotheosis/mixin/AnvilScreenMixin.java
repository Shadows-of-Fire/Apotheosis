package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

	@ModifyConstant(method = "renderLabels(Lcom/mojang/blaze3d/vertex/PoseStack;II)V")
	public int apoth_removeLevelCap(int old) {
		if (old == 40) return Integer.MAX_VALUE;
		return old;
	}

}
