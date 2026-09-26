package dev.shadowsoffire.apotheosis.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.effect.AttributeToggleAffix;
import dev.shadowsoffire.apotheosis.attachments.AttributeToggles;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(value = LivingEntity.class, remap = false)
public class LivingEntityMixin {

    @Unique
    @Nullable
    private Float originalHealthPercent;

    /**
     * Called from the head of {@link LivingEntity#detectEquipmentUpdates()} to record the original health percentage for non-player entities.
     */
    @Inject(method = "detectEquipmentUpdates", at = @At("HEAD"))
    private void apoth_cacheLastHealthPct(CallbackInfo ci) {
        if (!isPlayer()) {
            LivingEntity self = (LivingEntity) (Object) this;
            if (self.getHealth() > 0 && self.getMaxHealth() > 0) {
                originalHealthPercent = self.getHealth() / self.getMaxHealth();
            }
            else {
                originalHealthPercent = null;
            }
        }
    }

    /**
     * Then, from the tail of the same method, we update the health to keep the same percentage.
     * <p>
     * Generally speaking, we want entities equipped with armor that increases their health to do so immediately, since mobs don't heal.
     */
    @Inject(method = "detectEquipmentUpdates", at = @At("TAIL"))
    private void apoth_updateHealthPct(CallbackInfo ci) {
        if (originalHealthPercent != null) {
            LivingEntity self = (LivingEntity) (Object) this;
            self.setHealth(self.getMaxHealth() * originalHealthPercent);
            originalHealthPercent = null;
        }
    }

    /**
     * Caps the attribute value for players who have suppressed bonuses to it via an {@link AttributeToggleAffix}.
     * <p>
     * See {@link AttributeToggles#getCappedValue(AttributeInstance)} for the clamping rules.
     */
    @Inject(method = "getAttributeValue", at = @At("HEAD"), cancellable = true)
    private void apoth_suppressToggledAttributes(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
        if ((Object) this instanceof Player player) {
            AttributeToggles toggles = player.getData(Apoth.Attachments.ATTRIBUTE_TOGGLES);
            if (toggles.isSuppressed(attribute)) {
                AttributeInstance inst = player.getAttributes().getInstance(attribute);
                if (inst != null) {
                    cir.setReturnValue(toggles.getCappedValue(inst));
                }
            }
        }
    }

    private boolean isPlayer() {
        return (Object) this instanceof Player;
    }
}
