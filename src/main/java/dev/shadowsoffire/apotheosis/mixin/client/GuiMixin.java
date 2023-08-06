package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.gui.Gui;

@Mixin(Gui.class)
public class GuiMixin {

    /**
     * @reason Extends the time the action bar message is set on the screen from 3 seconds to 8 seconds.
     */
    @ModifyConstant(method = "setOverlayMessage")
    public int apoth_extendTime(int old) {
        return 160;
    }

}
