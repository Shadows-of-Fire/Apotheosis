package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(value = EntityRenderer.class, remap = false)
public class EntityRendererMixin {

    @Inject(at = @At("HEAD"), method = "getShadowRadius", cancellable = true)
    private void apoth_getShadowRadius(Entity entity, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof ItemEntity item) {
            ItemStack stack = item.getItem();
            DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(stack);
            if (rarity.isBound()) {
                cir.setReturnValue(0F);
            }
        }
    }
}
