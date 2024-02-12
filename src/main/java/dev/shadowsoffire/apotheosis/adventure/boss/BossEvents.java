package dev.shadowsoffire.apotheosis.adventure.boss;

import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.boss.MinibossRegistry.IEntityMatch;
import dev.shadowsoffire.apotheosis.adventure.client.BossSpawnMessage;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.network.PacketDistro;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
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
import net.minecraft.util.Mth;
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
import net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BossEvents {

    public Object2IntMap<ResourceLocation> bossCooldowns = new Object2IntOpenHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void naturalBosses(FinalizeSpawn e) {
        if (e.getSpawnType() == MobSpawnType.NATURAL || e.getSpawnType() == MobSpawnType.CHUNK_GENERATION) {
            LivingEntity entity = e.getEntity();
            RandomSource rand = e.getLevel().getRandom();
            if (this.bossCooldowns.getInt(entity.level().dimension().location()) <= 0 && !e.getLevel().isClientSide() && entity instanceof Monster && e.getResult() != Result.DENY) {
                ServerLevelAccessor sLevel = e.getLevel();
                ResourceLocation dimId = sLevel.getLevel().dimension().location();
                Pair<Float, BossSpawnRules> rules = AdventureConfig.BOSS_SPAWN_RULES.get(dimId);
                if (rules == null) return;
                if (rand.nextFloat() <= rules.getLeft() && rules.getRight().test(sLevel, BlockPos.containing(e.getX(), e.getY(), e.getZ()))) {
                    Player player = sLevel.getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
                    if (player == null) return; // Spawns require player context
                    ApothBoss item = BossRegistry.INSTANCE.getRandomItem(rand, player.getLuck(), IDimensional.matches(sLevel.getLevel()), IStaged.matches(player));
                    if (item == null) {
                        AdventureModule.LOGGER.error("Attempted to spawn a boss in dimension {} using configured boss spawn rule {}/{} but no bosses were made available.", dimId, rules.getRight(), rules.getLeft());
                        return;
                    }
                    Mob boss = item.createBoss(sLevel, BlockPos.containing(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), rand, player.getLuck());
                    if (AdventureConfig.bossAutoAggro && !player.isCreative()) {
                        boss.setTarget(player);
                    }
                    if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
                        sLevel.addFreshEntityWithPassengers(boss);
                        e.setResult(Result.DENY);
                        AdventureModule.debugLog(boss.blockPosition(), "Surface Boss - " + boss.getName().getString());
                        Component name = this.getName(boss);
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
                        this.bossCooldowns.put(entity.level().dimension().location(), AdventureConfig.bossSpawnCooldown);
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
    public void minibosses(FinalizeSpawn e) {
        LivingEntity entity = e.getEntity();
        RandomSource rand = e.getLevel().getRandom();
        if (!e.getLevel().isClientSide() && entity instanceof Mob mob && e.getResult() != Result.DENY) {
            ServerLevelAccessor sLevel = e.getLevel();
            Player player = sLevel.getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
            if (player == null) return; // Spawns require player context
            ApothMiniboss item = MinibossRegistry.INSTANCE.getRandomItem(rand, player.getLuck(), IDimensional.matches(sLevel.getLevel()), IStaged.matches(player), IEntityMatch.matches(entity));
            if (item != null && !item.isExcluded(mob, sLevel, e.getSpawnType()) && sLevel.getRandom().nextFloat() <= item.getChance()) {
                mob.getPersistentData().putString("apoth.miniboss", MinibossRegistry.INSTANCE.getKey(item).toString());
                mob.getPersistentData().putFloat("apoth.miniboss.luck", player.getLuck());
                if (!item.shouldFinalize()) e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void delayedMinibosses(EntityJoinLevelEvent e) {
        if (!e.getLevel().isClientSide && e.getEntity() instanceof Mob mob) {
            String key = mob.getPersistentData().getString("apoth.miniboss");
            if (key != null) {
                ApothMiniboss item = MinibossRegistry.INSTANCE.getValue(new ResourceLocation(key));
                if (item != null) {
                    item.transformMiniboss((ServerLevel) e.getLevel(), mob, e.getLevel().getRandom(), mob.getPersistentData().getFloat("apoth.miniboss.luck"));
                }
            }
        }
    }

    @SubscribeEvent
    public void tick(LevelTickEvent e) {
        if (e.phase == Phase.END) {
            this.bossCooldowns.computeIntIfPresent(e.level.dimension().location(), (key, value) -> Math.max(0, value - 1));
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
        }
        else {
            return entity.checkSpawnRules(world, MobSpawnType.NATURAL) && entity.checkSpawnObstruction(world);
        }
    }

    public static enum BossSpawnRules implements BiPredicate<ServerLevelAccessor, BlockPos> {
        NEEDS_SKY(ServerLevelAccessor::canSeeSky),
        NEEDS_SURFACE(
            (level, pos) -> pos.getY() >= level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())),
        BELOW_SURFACE(
            (level, pos) -> pos.getY() < level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())),
        CANNOT_SEE_SKY((level, pos) -> !level.canSeeSky(pos)),
        SURFACE_OUTER_END(
            (level, pos) -> NEEDS_SURFACE.test(level, pos) && (Mth.abs(pos.getX()) > 1024 || Mth.abs(pos.getZ()) > 1024)),
        ANY((level, pos) -> true);

        public static final Codec<BossSpawnRules> CODEC = PlaceboCodecs.enumCodec(BossSpawnRules.class);

        BiPredicate<ServerLevelAccessor, BlockPos> pred;

        private BossSpawnRules(BiPredicate<ServerLevelAccessor, BlockPos> pred) {
            this.pred = pred;
        }

        @Override
        public boolean test(ServerLevelAccessor t, BlockPos u) {
            return this.pred.test(t, u);
        }
    }

}
