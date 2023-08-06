package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraftforge.common.extensions.IForgeItemStack;

@Mixin(IForgeItemStack.class)
public interface IForgeItemStackMixin {

    // @Overwrite(remap = false)
    // public default int getItemEnchantability() {
    // return DeadlyHooks.getEnchantability((ItemStack) (Object) this);
    // }
}
