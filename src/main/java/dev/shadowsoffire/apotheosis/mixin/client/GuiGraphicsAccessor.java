package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

@Mixin(value = GuiGraphics.class, remap = false)
public interface GuiGraphicsAccessor {

    @Accessor
    void setTooltipStack(ItemStack stack);

    @Accessor
    ItemStack getTooltipStack();

}
