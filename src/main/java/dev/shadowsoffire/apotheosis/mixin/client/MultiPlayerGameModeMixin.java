package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.client.RadialProgressTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;

@Mixin(value = MultiPlayerGameMode.class, remap = false)
public class MultiPlayerGameModeMixin {

    /**
     * WAR for the fact that BlockEvent.BreakEvent is not called on the client.
     * <p>
     * Instead, we tell the progress tracker to break all client blocks when the target block is broken.
     * 
     * @param pos
     * @param cir
     */
    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void apoth_onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            RadialProgressTracker.breakClientBlocks((MultiPlayerGameMode) (Object) this, pos);
        }
    }

}
