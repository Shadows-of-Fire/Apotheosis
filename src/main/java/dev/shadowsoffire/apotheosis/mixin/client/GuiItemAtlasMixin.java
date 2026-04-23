package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.client.ApothRenderStateHolder;
import dev.shadowsoffire.apotheosis.client.PipelinedRenderer;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;

@Mixin(value = GuiItemAtlas.class, remap = false)
public abstract class GuiItemAtlasMixin {

    @WrapOperation(
        method = "drawToSlot",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V")
    )
    private void apoth_wrapEndBatchWithModifier(MultiBufferSource.BufferSource src, Operation<Void> original, @Local(argsOnly = true) ItemStackRenderState item) {
        Float alpha = ((ApothRenderStateHolder) item).apoth$getRenderAlpha();
        if (alpha == null) {
            original.call(src);
            return;
        }

        ResourceKey<PipelineModifier> key;
        if (Float.isNaN(alpha)) {
            key = AdventureModuleClient.GRAY_ITEM;
        }
        else {
            key = AdventureModuleClient.GHOST_ITEM_TIERS[PipelinedRenderer.nearestTier(alpha)];
        }

        RenderSystem.pushPipelineModifier(key);
        try {
            original.call(src);
        }
        finally {
            RenderSystem.popPipelineModifier();
        }
    }
}
