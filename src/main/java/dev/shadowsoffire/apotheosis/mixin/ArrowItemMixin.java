package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.Ench;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;

@Mixin(ArrowItem.class)
public class ArrowItemMixin {

    @Inject(method = "isInfinite", at = @At(value = "RETURN"), remap = false, cancellable = true)
    public void apoth_isInfinite(ItemStack stack, ItemStack bow, Player player, CallbackInfoReturnable<Boolean> ci) {
        if (!ci.getReturnValueZ() && Apotheosis.enableEnch) {
            ci.setReturnValue(Ench.Enchantments.ENDLESS_QUIVER.get().isTrulyInfinite(stack, bow, player));
        }
    }

}
