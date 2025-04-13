package dev.shadowsoffire.apotheosis.affix;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * This is a bit of a hack to allow affixes to report back their attribute modifier tooltips so we can mark them with the star prefix.
 * TODO: Fold this back into {@link Affix}?
 */
public interface AttributeProvidingAffix {

    void gatherModifierTooltips(AffixInstance inst, AttributeTooltipContext ctx, Consumer<Component> list);

}
