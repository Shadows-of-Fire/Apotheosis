package shadows.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

    @ModifyConstant(method = "renderLabels(Lcom/mojang/blaze3d/vertex/PoseStack;II)V", constant = @Constant(intValue = 40))
    public int apoth_removeLevelCap(int old) {
        return Integer.MAX_VALUE;
    }

}
