package dev.shadowsoffire.apotheosis.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.client.ApothRenderStateHolder;
import net.minecraft.client.renderer.item.ItemStackRenderState;

@Mixin(value = ItemStackRenderState.class, remap = false)
public abstract class ItemStackRenderStateMixin implements ApothRenderStateHolder {

    @Unique
    private Float apoth$renderAlpha = null;

    @Override
    @Unique
    public @Nullable Float apoth$getRenderAlpha() {
        return this.apoth$renderAlpha;
    }

    @Override
    @Unique
    public void apoth$setRenderAlpha(@Nullable Float alpha) {
        this.apoth$renderAlpha = alpha;
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void apoth_clearRenderAlpha(CallbackInfo ci) {
        this.apoth$renderAlpha = null;
    }
}
