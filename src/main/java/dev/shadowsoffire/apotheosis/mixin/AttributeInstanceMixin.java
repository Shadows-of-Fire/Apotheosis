package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.util.AttributeInstanceExt;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

/**
 * Implements {@link AttributeInstanceExt} for {@link AttributeInstance}.
 */
@Mixin(value = AttributeInstance.class, remap = false)
public abstract class AttributeInstanceMixin implements AttributeInstanceExt {

    @Unique
    private int apoth$modCount = 0;

    /**
     * Every mutation of an {@link AttributeInstance} (base value, add/remove modifier, replaceFrom) routes through {@code setDirty()}.
     */
    @Inject(method = "setDirty", at = @At("HEAD"))
    private void apoth_bumpModCount(CallbackInfo ci) {
        this.apoth$modCount++;
    }

    @Override
    public int apoth$getModificationCount() {
        return this.apoth$modCount;
    }
}
