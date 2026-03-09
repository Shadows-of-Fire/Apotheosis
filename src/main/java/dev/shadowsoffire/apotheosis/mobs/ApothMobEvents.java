package dev.shadowsoffire.apotheosis.mobs;

import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apoth.DataMaps;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.AugmentRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Augmentation;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCooldownSavedData;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * This file contains Apotheosis's mob processing events.
 * <p>
 * These events execute the following steps in sequential order:
 * <ol>
 * <li>Checks if the mob spawn can be consumed to spawn an {@link Invader}, aborting the mob spawn.</li>
 * <li>Checks if a mob should be {@linkplain Augmentation augmented}, and attempts to run the augments.</li>
 * <li>Attempts to transform the entity into an {@link Elite}, cancelling future events if successful.</li>
 * <li>Attmpts to grant the entity a random affix item.</li>
 * </ol>
 */
public class ApothMobEvents {

    public static final String APOTH_MINIBOSS = "apoth.miniboss";
    public static final String APOTH_MINIBOSS_PLAYER = APOTH_MINIBOSS + ".player";

    protected SpawnCooldownSavedData cooldownData = new SpawnCooldownSavedData();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void finalizeMobSpawns(FinalizeSpawnEvent e) {
        debugLog("Finalizing spawn for: {}", e.getEntity().getName().getString());
        if (e.isCanceled() || e.isSpawnCancelled()) {
            debugLog("Discarding due to cancellation.");
            return;
        }

        Player player = e.getLevel().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
        if (player == null) {
            debugLog("Discarding due to lack of player context.");
            return; // Spawns require player context
        }

        Mob mob = e.getEntity();
        RandomSource rand = e.getLevel().getRandom();
        GenContext ctx = GenContext.forPlayerAtPos(rand, player, mob.blockPosition());

        if (this.trySpawnInvader(e, mob, ctx, player)) {
            debugLog("Successfully spawned an invader. Skipping Augmentations and Elites.");
            return;
        }

        this.tryAugmentations(e.getLevel(), mob, e.getSpawnType(), ctx);

        if (this.trySpawnElite(e, mob, ctx, player)) {
            return;
        }
    }

    private boolean trySpawnInvader(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
        // Invaders can only trigger off of natural spawns (chunk generation is considered "natural")
        if ((e.getSpawnType() != MobSpawnType.NATURAL && e.getSpawnType() != MobSpawnType.CHUNK_GENERATION) || !(mob instanceof Monster)) {
            debugLog("[Invaders]: Failed invader preconditions.");
            return false;
        }

        if (this.cooldownData.isOnCooldown(mob.level())) {
            debugLog("[Invaders]: Cooldown is active for " + mob.level().dimension().location());
            return false;
        }

        ServerLevelAccessor sLevel = e.getLevel();
        ResourceKey<DimensionType> dimId = sLevel.getLevel().dimensionTypeRegistration().getKey();

        InvaderSpawnRules rules = sLevel.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getData(DataMaps.INVADER_SPAWN_RULES, dimId);
        if (rules == null) {
            debugLog("[Invaders]: No invader spawn rules present for dimension {}", dimId);
            return false;
        }

        float chance = rules.spawnChances().get(ctx.tier());
        SurfaceType surface = rules.surfaceType();

        if (ctx.rand().nextFloat() > chance) {
            debugLog("[Invaders]: Failed random chance roll.");
            return false;
        }

        if (surface.test(sLevel, BlockPos.containing(e.getX(), e.getY(), e.getZ()))) {
            debugLog("[Invaders]: Succeeded at random chance roll and surface test.");
            Invader item = InvaderRegistry.INSTANCE.getRandomItem(ctx);
            if (item == null) {
                Apotheosis.LOGGER.error("Attempted to spawn an Invader in dimension {} using configured spawn rules {} but no bosses were made available.", dimId, rules);
                return false;
            }

            if (!item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
                debugLog("[Invaders]: Failed invader spawn conditions.");
                return false;
            }

            Mob boss = item.createBoss(sLevel, BlockPos.containing(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), ctx);
            if (AdventureConfig.bossAutoAggro && !player.isCreative()) {
                boss.setTarget(player);
            }

            if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
                sLevel.addFreshEntityWithPassengers(boss);
                e.setCanceled(true);
                e.setSpawnCancelled(true);

                sendInvaderSpawnNotification((ServerLevel) sLevel, boss);

                this.cooldownData.startCooldown(mob.level(), rules.cooldown().orElse(AdventureConfig.bossSpawnCooldown));
                debugLog("[Invaders]: Successfully spawned an invader {} at {}", boss.getName().getString(), boss.blockPosition());
                return true;
            }
            else {
                debugLog("Failed entity spawn checks.");
            }
        }
        else {
            debugLog("[Invaders]: Failed surface test " + surface);
        }

        return false;
    }

    public static void sendInvaderSpawnNotification(ServerLevel sLevel, Mob invader) {
        Component name = getName(invader);
        DynamicHolder<LootRarity> rarity = getRarity(invader);

        if (name == null || !rarity.isBound()) {
            Apotheosis.LOGGER.warn("An Invader {} ({}) has spawned without a name ({}) or rarity ({})!", invader.getName().getString(), EntityType.getKey(invader.getType()), name, rarity);
        }
        else {
            sLevel.players().forEach(p -> {
                Vec3 tPos = new Vec3(invader.getX(), p.getY(), invader.getZ());
                if (p.distanceToSqr(tPos) <= AdventureConfig.bossAnnounceRange * AdventureConfig.bossAnnounceRange) {
                    ((ServerPlayer) p).connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("info.apotheosis.boss_spawn", name, (int) invader.getX(), (int) invader.getY())));
                    PacketDistributor.sendToPlayer((ServerPlayer) p, new BossSpawnPayload(invader.blockPosition(), rarity));
                }
            });
        }
    }

    /**
     * Applies all active {@link TierAugment}s to the mob, then rolls the {@link AdventureConfig#augmentedMobChance} to apply {@link Augmentation}s.
     */
    private void tryAugmentations(ServerLevelAccessor level, Mob mob, MobSpawnType type, GenContext ctx) {
        float healthPct = mob.getHealth() / mob.getMaxHealth();

        for (TierAugment aug : TierAugmentRegistry.getAugments(ctx.tier(), Target.MONSTERS)) {
            aug.apply(level, mob);
        }
        mob.setData(Attachments.TIER_AUGMENTS_APPLIED, true);

        for (Augmentation aug : AugmentRegistry.getAll()) {
            if (aug.canApply(level, mob, type, ctx)) {
                if (ctx.rand().nextFloat() <= aug.chance()) {
                    debugLog("Applying augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
                    aug.apply(mob, ctx);
                }
                else {
                    debugLog("Roll failed for augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
                }
            }
            else {
                debugLog("Skipped augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
            }
        }

        // Since Tier Augments or Augmentations may apply max health, we need to update the mob's current HP.
        mob.setHealth(healthPct * mob.getMaxHealth());
    }

    private boolean trySpawnElite(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
        ServerLevelAccessor sLevel = e.getLevel();

        Elite item = EliteRegistry.INSTANCE.getRandomItem(ctx, mob);
        if (item == null) {
            debugLog("No Elites were available for {} and {}", ctx, mob);
            return false;
        }

        if (!item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
            debugLog("The elite {} was selected but could not spawn based on spawn conditions.", EliteRegistry.INSTANCE.getKey(item));
            return false;
        }

        if (ctx.rand().nextFloat() <= item.getChance()) {
            mob.getPersistentData().putString(Elite.MINIBOSS_KEY, EliteRegistry.INSTANCE.getKey(item).toString());
            mob.getPersistentData().putString(Elite.PLAYER_KEY, player.getUUID().toString());
            if (!item.basicData().finalizeSpawn()) {
                e.setCanceled(true);
            }
            debugLog("Successfully spawned the elite {} at {}", EliteRegistry.INSTANCE.getKey(item), mob.blockPosition());
            return true;
        }

        return false;
    }

    /**
     * The {@link FinalizeSpawnEvent} happens when the entity spawns, but is still before the mob is added to the world.
     * <p>
     * Since applying an {@link Elite} can have side effects that spawn mobs into the world, we need to delay the application
     * until the target entity joins the world.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void delayedEliteMobs(EntityJoinLevelEvent e) {
        if (!e.getLevel().isClientSide && e.getEntity() instanceof Mob mob) {
            CompoundTag data = mob.getPersistentData();
            if (data.contains(Elite.MINIBOSS_KEY) && data.contains(Elite.PLAYER_KEY)) {
                String key = data.getString(Elite.MINIBOSS_KEY);
                try {
                    UUID playerId = UUID.fromString(data.getString(Elite.PLAYER_KEY));
                    Player player = e.getLevel().getPlayerByUUID(playerId);
                    if (player == null) {
                        player = e.getLevel().getNearestPlayer(mob, -1);
                    }

                    if (player != null) {
                        GenContext ctx = GenContext.forPlayerAtPos(e.getLevel().random, player, mob.blockPosition());
                        Elite item = EliteRegistry.INSTANCE.getValue(ResourceLocation.tryParse(key));
                        if (item != null) {
                            item.transformMiniboss((ServerLevel) e.getLevel(), mob, ctx);
                        }
                    }
                }
                catch (Exception ex) {
                    Apotheosis.LOGGER.error("Failure while initializing the Apothic Elite " + key, ex);
                }
            }
        }
    }

    @SubscribeEvent
    public void tick(LevelTickEvent.Post e) {
        this.cooldownData.tick(e.getLevel().dimension().location());
    }

    @SubscribeEvent
    public void load(ServerStartedEvent e) {
        this.cooldownData = e.getServer().getLevel(Level.OVERWORLD).getDataStorage()
            .computeIfAbsent(new SavedData.Factory<>(SpawnCooldownSavedData::new, SpawnCooldownSavedData::loadTimes, null), "apotheosis_boss_times");
    }

    private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
        if (playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance() && entity.removeWhenFarAway(playerDist)) {
            return false;
        }
        else {
            return entity.checkSpawnRules(world, MobSpawnType.NATURAL) && entity.checkSpawnObstruction(world);
        }
    }

    @Nullable
    private static Component getName(Mob boss) {
        return boss.getSelfAndPassengers().filter(e -> e.getPersistentData().contains(Invader.BOSS_KEY)).findFirst().map(Entity::getCustomName).orElse(null);
    }

    @Nullable
    private static DynamicHolder<LootRarity> getRarity(Mob boss) {
        return boss.getSelfAndPassengers().filter(e -> e.getPersistentData().contains(Invader.BOSS_KEY)).findFirst().map(ent -> {
            return RarityRegistry.INSTANCE.holder(ResourceLocation.tryParse(ent.getPersistentData().getString(Invader.RARITY_KEY)));
        }).orElse(RarityRegistry.INSTANCE.emptyHolder());
    }

    private static final Marker MARKER = MarkerManager.getMarker(ApothMobEvents.class.getSimpleName());

    private static void debugLog(String msg, Object... args) {
        if (Apotheosis.DEBUG_MOBS) {
            Apotheosis.LOGGER.debug(MARKER, msg, args);
        }
    }

}
