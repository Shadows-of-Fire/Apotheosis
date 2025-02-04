package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

@Mixin(value = ItemStack.class, priority = 500, remap = false)
public class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    public void apoth_affixItemName(CallbackInfoReturnable<Component> cir) {
        ItemStack ths = (ItemStack) (Object) this;
        if (ths.has(Components.AFFIX_NAME)) {
            try {
                Component component = AffixHelper.getName(ths).copy();
                if (component.getContents() instanceof TranslatableContents tContents) {
                    int idx = "misc.apotheosis.affix_name.four".equals(tContents.getKey()) ? 2 : 1;
                    tContents.getArgs()[idx] = cir.getReturnValue();
                    cir.setReturnValue(component);
                }
                else {
                    ths.remove(Components.AFFIX_NAME);
                }
            }
            catch (Exception exception) {
                ths.remove(Components.AFFIX_NAME);
            }
        }

        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(ths);
        if (rarity.isBound()) {
            Component recolored = cir.getReturnValue().copy().withStyle(s -> s.withColor(rarity.get().color()));
            cir.setReturnValue(recolored);
        }
    }

}
