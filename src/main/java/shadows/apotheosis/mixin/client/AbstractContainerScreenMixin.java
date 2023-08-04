package shadows.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(at = @At("RETURN"), method = "mouseDragged(DDIDD)Z", cancellable = true, require = 1)
    public void apoth_superMouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen) cir.setReturnValue(super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY));
    }

}
