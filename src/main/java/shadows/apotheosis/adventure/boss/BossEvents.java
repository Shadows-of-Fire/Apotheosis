package shadows.apotheosis.adventure.boss;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.client.BossSpawnMessage;
import shadows.placebo.network.PacketDistro;

public class BossEvents {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void surfaceBosses(LivingSpawnEvent.CheckSpawn e) {
		if (e.getSpawnReason() == MobSpawnType.NATURAL || e.getSpawnReason() == MobSpawnType.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isClientSide() && entity instanceof Monster && e.getResult() == Result.DEFAULT) {
				if (rand.nextFloat() <= AdventureConfig.surfaceBossChance && isValidBossPos(e.getWorld(), new BlockPos(e.getX(), e.getY(), e.getZ()))) {
					BossItem item = BossItemManager.INSTANCE.getRandomItem(rand, (ServerLevelAccessor) e.getWorld());
					if (item == null) return;
					Player player = e.getWorld().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
					if (player == null) return; //Should never be null, but we check anyway since nothing makes sense around here.
					Mob boss = item.createBoss((ServerLevelAccessor) e.getWorld(), new BlockPos(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), rand);
					if (canSpawn(e.getWorld(), boss, player.distanceToSqr(boss))) {
						e.getWorld().addFreshEntity(boss);
						e.setResult(Result.DENY);
						AdventureModule.debugLog(boss.blockPosition(), "Surface Boss - " + boss.getName().getString());
						e.getWorld().players().forEach(p -> {
							if (p.distanceToSqr(boss) <= 64 * 64) {
								((ServerPlayer) p).connection.send(new ClientboundSetActionBarTextPacket(new TranslatableComponent("info.apotheosis.boss_spawn", boss.getCustomName(), (int) boss.getX(), (int) boss.getY())));
								PacketDistro.sendTo(Apotheosis.CHANNEL, new BossSpawnMessage(boss.blockPosition(), boss.getCustomName().getStyle().getColor().getValue()), player);
							}
						});
						e.getWorld().playSound(null, boss.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 4, 1.25F);
					}
				}
			}
		}
	}

	private static boolean isValidBossPos(LevelAccessor level, BlockPos pos) {
		return level.canSeeSky(pos);
	}

	private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
		if (playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance() && entity.removeWhenFarAway(playerDist)) {
			return false;
		} else {
			return entity.checkSpawnRules(world, MobSpawnType.NATURAL) && entity.checkSpawnObstruction(world);
		}
	}

}
