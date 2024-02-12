package dev.shadowsoffire.apotheosis.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;

/**
 * This contents of this class are Copyright (c) Darkhax under LGPL v2.1 and used under the terms of that license.
 * The only changes made is the rename of the class, from MixinLivingEntity to MHFMixinLivingEntity.
 * The original license text is available here https://github.com/Darkhax-Minecraft/Max-Health-Fix/blob/1.19.2/License.md
 * MaxHealthFix is marked as a required dependency on CF and should be installed alongside Apotheosis.
 * This is just a "backup" so I stop receiving bug reports about it.
 */
@Mixin(LivingEntity.class)
public abstract class MHFMixinLivingEntity {

    /**
     * This float is used to temporarily hold the actual health of the entity while the entity data is being
     * deserialized. A null value is used to indicate that the health does not need correcting.
     */
    @Unique
    @Nullable
    private Float actualHealth = null;

    /**
     * The vanilla code will reset the entity health when the deserialized value exceeds {@link
     * LivingEntity#getMaxHealth()}. This generally is not an issue, however when entities are initially loaded their
     * max health attribute has not been properly initialized. This is the source of MC-17876.
     * <p>
     * This mixin is used to circumvent this faulty logic by capturing the deserialized value early and storing it in
     * {@link #actualHealth}. This approach is favoured over attempting to initialize attributes early as there is no
     * standard way to do this that would reasonably account for modded attribute sources.
     */
    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void maxhealthfix$readAdditionalSaveData(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("Health", Tag.TAG_ANY_NUMERIC)) {
            final float savedHealth = tag.getFloat("Health");
            if (savedHealth > this.getMaxHealth() && savedHealth > 0) {
                this.actualHealth = savedHealth;
            }
        }
    }

    /**
     * This mixin is used to apply the {@link #actualHealth} value after entity equipment has been deserialized and
     * properly applied to the entity. This approach is favoured over directly setting the health during deserialization
     * as it has less potential for de-syncs. An example scenario of concern would be a player saving their game and
     * removing a mod that added the attribute, resulting in too much health.
     */
    @Inject(method = "detectEquipmentUpdates()V", at = @At("RETURN"))
    private void maxhealthfix$detectEquipmentUpdates(CallbackInfo callback) {
        if (this.actualHealth != null) {
            if (this.actualHealth > 0 && this.actualHealth > this.getHealth()) {
                this.setHealth(this.actualHealth);
            }
            this.actualHealth = null;
        }
    }

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float newHealth);
}
