package shadows.apotheosis.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;

@Pseudo
@Mixin(targets = "mcjty.incontrol.spawner.SpawnerSystem")
public class SpawnerSystemMixin {

	@Redirect(
			at = @At(value = "INVOKE", remap = true,
			target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;"),
			method = "executeRule(ILmcjty/incontrol/spawner/SpawnerRule;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/MobCategory;F)V", 
			remap = false,
			require = 1
			)
	private static SpawnGroupData fireSpecialSpawn(Mob mob, ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
		if(!ForgeEventFactory.doSpecialSpawn(mob, pLevel, (float) mob.getX(), (float) mob.getY(), (float) mob.getZ(), null, pReason)) {
			return mob.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
		}
		return null;
	}

}
