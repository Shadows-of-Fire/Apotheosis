package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

@Mixin(value = ItemStack.class, priority = 500, remap = false)
public class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    public void apoth_affixItemName(CallbackInfoReturnable<Component> cir) {
        ItemStack ths = (ItemStack) (Object) this;
        Component afxName = AffixHelper.getModifiedStackName(ths, cir.getReturnValue());
        if (afxName != null) {
            cir.setReturnValue(afxName);
        }

        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(ths);
        if (rarity.isBound()) {
            Component recolored = cir.getReturnValue().copy().withStyle(s -> s.withColor(rarity.get().color()));
            cir.setReturnValue(recolored);
        }
    }

    /**
     * Allows for the injection of item right-click behavior after evaluation of any existing item right-click behavior.
     * <p>
     * The existing {@link UseItemOnBlockEvent} only allows for injecting behavior before evaluating the item, which breaks user expectations.
     */
    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    public void apoth_useItemOnBlockPost(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir, @Local UseItemOnBlockEvent event) {
        if (!cir.getReturnValue().consumesAction() && !event.isCanceled()) {
            ItemStack s = (ItemStack) (Object) this;
            InteractionResult socketRes = SocketHelper.getGems(s).onItemUse(ctx);
            if (socketRes != null) {
                cir.setReturnValue(socketRes);
                return;
            }

            InteractionResult afxRes = AffixHelper.streamAffixes(s).map(afx -> afx.onItemUse(ctx)).filter(Predicates.notNull()).findFirst().orElse(null);
            if (afxRes != null) {
                cir.setReturnValue(afxRes);
                return;
            }
        }
    }

}
