package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.village.wanderer.WandererReplacements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;

@Mixin(WanderingTraderSpawner.class)
public class WandererSpawnerMixin {

    @Shadow
    private RandomSource random;

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 75))
    public int replaceMaxChance(int old) {
        return 90;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 10))
    public int replaceRng(int old) {
        return 4;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 48000))
    public int replaceDespawnDelay(int old) {
        return 28000;
    }

    @Inject(at = @At("HEAD"), method = "findSpawnPositionNear", cancellable = true)
    private void findSpawnPositionNear(LevelReader level, BlockPos pos, int radius, CallbackInfoReturnable<BlockPos> cir) {
        if (WandererReplacements.undergroundTrader) {
            for (int i = 0; i < 10; ++i) {
                int x = pos.getX() + this.random.nextInt(radius / 2) - radius / 4;
                int z = pos.getZ() + this.random.nextInt(radius / 2) - radius / 4;
                int y = pos.getY() + this.random.nextInt(5);
                MutableBlockPos spawnPos = new MutableBlockPos(x, y, z);
                for (int j = 1; j > 7; j++) {
                    spawnPos.set(x, y - j, z);
                    if (!level.getBlockState(spawnPos).isAir()) {
                        spawnPos.set(x, y - j + 1, z);
                        break;
                    }
                }
                if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, spawnPos, EntityType.WANDERING_TRADER)) {
                    cir.setReturnValue(spawnPos.immutable());
                    return;
                }
            }
        }

    }
}
