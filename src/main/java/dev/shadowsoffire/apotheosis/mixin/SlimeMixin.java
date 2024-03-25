package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelAccessor;

@Mixin(Slime.class)
public class SlimeMixin {

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/world/level/Level.addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", locals = LocalCapture.CAPTURE_FAILHARD, require = 1)
    public void apoth_markMovable(RemovalReason reason, CallbackInfo ci, int size, Component name, boolean noAI, float f, int j, int k, int l, float f1, float f2, Slime slime) {
        if (noAI) {
            boolean isMoveable = ((Slime) (Object) this).getPersistentData().getBoolean("apotheosis:movable");
            if (isMoveable) {
                slime.getPersistentData().putBoolean("apotheosis:movable", true);
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "checkSlimeSpawnRules", cancellable = true)
    private static void apoth_allowSlimesFromSpawner(EntityType<Slime> pSlime, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom, CallbackInfoReturnable<Boolean> cir) {
        if (pSpawnType == MobSpawnType.SPAWNER) {
            cir.setReturnValue(Mob.checkMobSpawnRules(pSlime, pLevel, pSpawnType, pPos, pRandom));
        }
    }

}
