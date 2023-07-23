package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.client.event.RenderTooltipEvent;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.AffixHelper;

@Pseudo
@Mixin(targets = "vazkii.quark.content.client.tooltip.AttributeTooltips")
public class AttributeTooltipsMixin {

    @Inject(at = @At("HEAD"), method = "makeTooltip", remap = false, cancellable = true)
    private static void apoth_disableQuarkTooltipsForAffixItems(RenderTooltipEvent.GatherComponents event, CallbackInfo ci) {
        if (AdventureConfig.disableQuarkOnAffixItems && !AffixHelper.getAffixes(event.getItemStack()).isEmpty()) ci.cancel();
    }

}
