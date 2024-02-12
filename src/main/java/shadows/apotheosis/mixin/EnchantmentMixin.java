package shadows.apotheosis.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shadows.apotheosis.ench.EnchModule;

@Mixin(value = Enchantment.class, priority = 1500)
public class EnchantmentMixin {

    /**
     * Adjusts the color of the enchantment text if above the vanilla max.
     */
    @Inject(method = "getFullname", at = @At("RETURN"), cancellable = true)
    public void apoth_modifyEnchColorForAboveMaxLevel(int level, CallbackInfoReturnable<Component> cir) {
        Enchantment ench = (Enchantment) (Object) this;
        if (!ench.isCurse() && level > ench.getMaxLevel() && cir.getReturnValue() instanceof MutableComponent mc) {
            cir.setReturnValue(mc.withStyle(s -> s.withColor(EnchModule.Colors.LIGHT_BLUE_FLASH)));
        }
    }

}