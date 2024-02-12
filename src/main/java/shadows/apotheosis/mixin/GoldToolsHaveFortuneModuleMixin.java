package shadows.apotheosis.mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "vazkii.quark.content.tweaks.module.GoldToolsHaveFortuneModule", remap = false)
public class GoldToolsHaveFortuneModuleMixin {

    @Inject(method = "fakeEnchantmentTooltip", at = @At("HEAD"), cancellable = true)
    private static void apoth_blockQuarkEnchTooltip1(ItemStack stack, List<Component> components, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "hideSmallerEnchantments", at = @At("HEAD"), cancellable = true)
    private static void apoth_blockQuarkEnchTooltip2(ItemStack stack, ListTag tag, CallbackInfoReturnable<ListTag> cir) {
        cir.setReturnValue(tag);
    }

}