package shadows.apotheosis.adventure.boss;

import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.boss.MinibossManager.IEntityMatch;
import shadows.apotheosis.adventure.client.BossSpawnMessage;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;
import shadows.placebo.network.PacketDistro;

public class BossEvents {

	public Object2IntMap<ResourceLocation> bossCooldowns = new Object2IntOpenHashMap<>();

	@SubscribeEvent(priority = EventPriority.LOW)
	public void naturalBosses(LivingSpawnEvent.SpecialSpawn e) {
		if (e.getSpawnReason() == MobSpawnType.NATURAL || e.getSpawnReason() == MobSpawnType.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntity();
			RandomSource rand = e.getLevel().getRandom();
			if (bossCooldowns.getInt(entity.level.dimension().location()) <= 0 && !e.getLevel().isClientSide() && entity instanceof Monster && e.getResult() != Result.DENY) {
				ServerLevelAccessor sLevel = (ServerLevelAccessor) e.getLevel();
				Pair<Float, BossSpawnRules> rules = AdventureConfig.BOSS_SPAWN_RULES.get(sLevel.getLevel().dimension().location());
				if (rules == null) return;
				if (rand.nextFloat() <= rules.getLeft() && rules.getRight().test(sLevel, new BlockPos(e.getX(), e.getY(), e.getZ()))) {
					Player player = sLevel.getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
					if (player == null) return; // Spawns require player context
					BossItem item = BossItemManager.INSTANCE.getRandomItem(rand, player.getLuck(), IDimensional.matches(sLevel.getLevel()), IStaged.matches(player));
					Mob boss = item.createBoss(sLevel, new BlockPos(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), rand, player.getLuck());
					if (AdventureConfig.bossAutoAggro && !player.isCreative()) {
						boss.setTarget(player);
					}
					if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
						sLevel.addFreshEntityWithPassengers(boss);
						e.setResult(Result.DENY);
						AdventureModule.debugLog(boss.blockPosition(), "Surface Boss - " + boss.getName().getString());
						Component name = getName(boss);
						if (name == null || name.getStyle().getColor() == null) AdventureModule.LOGGER.warn("A Boss {} ({}) has spawned without a custom name!", boss.getName().getString(), EntityType.getKey(boss.getType()));
						else {
							sLevel.players().forEach(p -> {
								Vec3 tPos = new Vec3(boss.getX(), AdventureConfig.bossAnnounceIgnoreY ? p.getY() : boss.getY(), boss.getZ());
								if (p.distanceToSqr(tPos) <= AdventureConfig.bossAnnounceRange * AdventureConfig.bossAnnounceRange) {
									((ServerPlayer) p).connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("info.apotheosis.boss_spawn", name, (int) boss.getX(), (int) boss.getY())));
									TextColor color = name.getStyle().getColor();
									PacketDistro.sendTo(Apotheosis.CHANNEL, new BossSpawnMessage(boss.blockPosition(), color == null ? 0xFFFFFF : color.getValue()), player);
								}
							});
						}
						bossCooldowns.put(entity.level.dimension().location(), AdventureConfig.bossSpawnCooldown);
					}
				}
			}
		}
	}

	@Nullable
	private Component getName(Mob boss) {
		return boss.getSelfAndPassengers().filter(e -> e.getPersistentData().contains("apoth.boss")).findFirst().map(Entity::getCustomName).orElse(null);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void minibosses(LivingSpawnEvent.SpecialSpawn e) {
		LivingEntity entity = e.getEntity();
		RandomSource rand = e.getLevel().getRandom();
		if (!e.getLevel().isClientSide() && entity instanceof Mob mob && e.getResult() != Result.DENY) {
			ServerLevelAccessor sLevel = (ServerLevelAccessor) e.getLevel();
			Player player = sLevel.getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
			if (player == null) return; // Spawns require player context
			MinibossItem item = MinibossManager.INSTANCE.getRandomItem(rand, player.getLuck(), IDimensional.matches(sLevel.getLevel()), IStaged.matches(player), IEntityMatch.matches(entity));
			if (item != null && !item.isExcluded(mob, sLevel, e.getSpawnReason()) && sLevel.getRandom().nextFloat() <= item.getChance()) {
				mob.getPersistentData().putString("apoth.miniboss", item.getId().toString());
				mob.getPersistentData().putFloat("apoth.miniboss.luck", player.getLuck());
				e.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void delayedMinibosses(EntityJoinLevelEvent e) {
		if (!e.getLevel().isClientSide && e.getEntity() instanceof Mob mob) {
			String key = mob.getPersistentData().getString("apoth.miniboss");
			if (key != null) {
				MinibossItem item = MinibossManager.INSTANCE.getValue(new ResourceLocation(key));
				if (item != null) {
					item.transformMiniboss((ServerLevel) e.getLevel(), mob, e.getLevel().getRandom(), mob.getPersistentData().getFloat("apoth.miniboss.luck"));
				}
			}
		}
	}

	@SubscribeEvent
	public void tick(LevelTickEvent e) {
		if (e.phase == Phase.END) {
			bossCooldowns.computeIntIfPresent(e.level.dimension().location(), (key, value) -> Math.max(0, value - 1));
		}
	}

	@SubscribeEvent
	public void load(ServerStartedEvent e) {
		e.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(this::loadTimes, TimerPersistData::new, "apotheosis_boss_times");
	}

	private class TimerPersistData extends SavedData {

		@Override
		public CompoundTag save(CompoundTag tag) {
			for (Object2IntMap.Entry<ResourceLocation> e : BossEvents.this.bossCooldowns.object2IntEntrySet()) {
				tag.putInt(e.getKey().toString(), e.getIntValue());
			}
			return tag;
		}

	}

	private TimerPersistData loadTimes(CompoundTag tag) {
		this.bossCooldowns.clear();
		for (String s : tag.getAllKeys()) {
			ResourceLocation id = new ResourceLocation(s);
			int val = tag.getInt(s);
			this.bossCooldowns.put(id, val);
		}
		return new TimerPersistData();
	}

	private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
		if (playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance() && entity.removeWhenFarAway(playerDist)) {
			return false;
		} else {
			return entity.checkSpawnRules(world, MobSpawnType.NATURAL) && entity.checkSpawnObstruction(world);
		}
	}

	public static enum BossSpawnRules implements BiPredicate<ServerLevelAccessor, BlockPos> {
		NEEDS_SKY((level, pos) -> level.canSeeSky(pos)),
		NEEDS_SURFACE(
				(level, pos) -> pos.getY() >= level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())),
		ANY((level, pos) -> true);

		public static final Codec<BossSpawnRules> CODEC = new EnumCodec<>(BossSpawnRules.class);

		BiPredicate<ServerLevelAccessor, BlockPos> pred;

		private BossSpawnRules(BiPredicate<ServerLevelAccessor, BlockPos> pred) {
			this.pred = pred;
		}

		@Override
		public boolean test(ServerLevelAccessor t, BlockPos u) {
			return pred.test(t, u);
		}
	}

}
