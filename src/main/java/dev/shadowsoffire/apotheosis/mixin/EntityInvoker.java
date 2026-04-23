package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;

@Mixin(value = Entity.class, remap = false)
public interface EntityInvoker {

    @Invoker
    void callReadAdditionalSaveData(ValueInput input);
}
