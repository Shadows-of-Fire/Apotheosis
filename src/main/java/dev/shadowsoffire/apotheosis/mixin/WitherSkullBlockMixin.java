package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Fires the {@link FinalizeSpawnEvent} for withers when they are constructed using the usual structure.
 */
@Mixin(value = WitherSkullBlock.class, remap = false)
public class WitherSkullBlockMixin {

    @Inject(method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static void apoth_finalizeWithers(Level level, BlockPos pos, SkullBlockEntity be, CallbackInfo ci, @Local WitherBoss wither) {
        ServerLevelAccessor accessor = (ServerLevelAccessor) level;
        DifficultyInstance difficulty = accessor.getCurrentDifficultyAt(pos);
        EventHooks.finalizeMobSpawn(wither, accessor, difficulty, EntitySpawnReason.EVENT, null);
    }

}
