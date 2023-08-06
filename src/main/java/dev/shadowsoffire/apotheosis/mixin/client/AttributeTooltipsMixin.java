package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.minecraftforge.client.event.RenderTooltipEvent;

@Pseudo
@Mixin(targets = "vazkii.quark.content.client.tooltip.AttributeTooltips")
public class AttributeTooltipsMixin {

    @Inject(at = @At("HEAD"), method = "makeTooltip", remap = false, cancellable = true)
    private static void apoth_disableQuarkTooltipsForAffixItems(RenderTooltipEvent.GatherComponents event, CallbackInfo ci) {
        if (AdventureConfig.disableQuarkOnAffixItems && !AffixHelper.getAffixes(event.getItemStack()).isEmpty()) ci.cancel();
    }

}
