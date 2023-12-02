package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import dev.shadowsoffire.apotheosis.util.DrawsOnLeft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin implements DrawsOnLeft {

}
