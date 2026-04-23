package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.client.ApothRenderStateHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(value = GuiGraphicsExtractor.class, remap = false)
public abstract class GuiGraphicsExtractorMixin {

    @Inject(
        method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;updateForTopItem(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V",
            shift = At.Shift.AFTER
        ),
        require = 1
    )
    private void apoth_markRenderAlpha(LivingEntity owner, Level level, ItemStack stack, int x, int y, int seed, CallbackInfo ci, @Local TrackingItemStackRenderState state) {
        Float alpha = stack.get(Apoth.Components.RENDER_ALPHA);
        if (alpha != null) {
            ((ApothRenderStateHolder) state).apoth$setRenderAlpha(alpha);
            state.appendModelIdentityElement(alpha);
        }
    }
}
