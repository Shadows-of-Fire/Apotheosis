package dev.shadowsoffire.apotheosis.affix;

import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.GatherSkippedAttributeTooltipsEvent;

/**
 * This is a bit of a hack to allow affixes to report back their attribute modifier tooltips so we can mark them with the star prefix.
 * TODO: Fold this back into {@link Affix}?
 */
public interface AttributeProvidingAffix {

    void gatherModifierTooltips(AffixInstance inst, AttributeTooltipContext ctx, Consumer<Component> list);

    /**
     * Fires from the {@link GatherSkippedAttributeTooltipsEvent} to allow the affix to hide any relevant attribute modifiers.
     * <p>
     * When {@link WorldTier#isTutorialActive(Player)} is true, all modifiers should be skipped.
     *
     * @param inst The current affix instance.
     * @param ctx  The tooltip context.
     * @param skip A consumer that accepts resource locations to skip.
     */
    void skipModifierIds(AffixInstance inst, AttributeTooltipContext ctx, Consumer<Identifier> skip);

}
