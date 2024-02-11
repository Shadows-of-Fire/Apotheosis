package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.minecraft.world.item.ItemStack;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.content.client.tooltip.AttributeTooltips")
public class AttributeTooltipsMixin {

    // TODO: Fix, doesn't run after Zeta refactor.
    /**
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;hasShiftDown()Z", ordinal = 0), method = "makeTooltip", remap = false, cancellable = true, require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void apoth_disableQuarkTooltipsForAffixItems(@Coerce Object event, CallbackInfo ci, ItemStack stack) {
        if (AdventureConfig.disableQuarkOnAffixItems && !AffixHelper.getAffixes(stack).isEmpty()) ci.cancel();
    }
    **/

}
