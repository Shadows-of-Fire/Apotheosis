package dev.shadowsoffire.apotheosis.util;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.compat.spawners.ApothicSpawnersCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

/**
 * A preset collection of spawner stats, stored as raw values keyed by stat id.
 * <p>
 * When Apothic Spawners is installed, stats are applied through its spawner stat registry. Otherwise, only the
 * stats corresponding to vanilla {@link BaseSpawner} fields are applied, and any others are skipped.
 */
public record PresetSpawnerStats(Map<ResourceLocation, Dynamic<?>> stats) {

    public static final Codec<PresetSpawnerStats> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.PASSTHROUGH)
        .xmap(PresetSpawnerStats::new, PresetSpawnerStats::stats);

    public static final ResourceLocation MIN_DELAY = as("min_delay");
    public static final ResourceLocation MAX_DELAY = as("max_delay");
    public static final ResourceLocation SPAWN_COUNT = as("spawn_count");
    public static final ResourceLocation MAX_NEARBY_ENTITIES = as("max_nearby_entities");
    public static final ResourceLocation REQ_PLAYER_RANGE = as("req_player_range");
    public static final ResourceLocation SPAWN_RANGE = as("spawn_range");
    public static final ResourceLocation YOUTHFUL = as("youthful");

    private static final Map<ResourceLocation, BiConsumer<BaseSpawner, Integer>> VANILLA_STATS = Map.of(
        MIN_DELAY, (s, v) -> s.minSpawnDelay = v,
        MAX_DELAY, (s, v) -> s.maxSpawnDelay = v,
        SPAWN_COUNT, (s, v) -> s.spawnCount = v,
        MAX_NEARBY_ENTITIES, (s, v) -> s.maxNearbyEntities = v,
        REQ_PLAYER_RANGE, (s, v) -> s.requiredPlayerRange = v,
        SPAWN_RANGE, (s, v) -> s.spawnRange = v);

    private static final Map<ResourceLocation, Dynamic<?>> DEFAULT_STATS = builder()
        .stat(MIN_DELAY, 200)
        .stat(MAX_DELAY, 800)
        .stat(SPAWN_COUNT, 4)
        .stat(MAX_NEARBY_ENTITIES, 6)
        .stat(SPAWN_RANGE, 4)
        .stat(REQ_PLAYER_RANGE, 16)
        .build().stats();

    public PresetSpawnerStats() {
        this(DEFAULT_STATS);
    }

    public void apply(SpawnerBlockEntity entity) {
        if (ApothicSpawnersCompat.isLoaded()) {
            ApothicSpawnersCompat.applyStats(this, entity);
        }
        else {
            this.applyVanilla(entity.getSpawner());
        }
    }

    private void applyVanilla(BaseSpawner spawner) {
        for (Map.Entry<ResourceLocation, Dynamic<?>> entry : this.stats.entrySet()) {
            BiConsumer<BaseSpawner, Integer> setter = VANILLA_STATS.get(entry.getKey());
            if (setter == null) {
                Apotheosis.LOGGER.trace("Skipping spawner stat {} - Apothic Spawners is not installed.", entry.getKey());
                continue;
            }
            entry.getValue().asNumber().result().ifPresentOrElse(
                n -> setter.accept(spawner, n.intValue()),
                () -> Apotheosis.LOGGER.error("Ignoring non-numeric value for spawner stat {}.", entry.getKey()));
        }
    }

    private static ResourceLocation as(String path) {
        return ResourceLocation.fromNamespaceAndPath(ApothicSpawnersCompat.MODID, path);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected final ImmutableMap.Builder<ResourceLocation, Dynamic<?>> stats = ImmutableMap.builder();

        public Builder stat(ResourceLocation stat, int value) {
            return this.statInternal(stat, value);
        }

        public Builder stat(ResourceLocation stat, float value) {
            return this.statInternal(stat, value);
        }

        public Builder stat(ResourceLocation stat, boolean value) {
            return this.statInternal(stat, value);
        }

        private Builder statInternal(ResourceLocation stat, Object value) {
            this.stats.put(stat, new Dynamic<>(JavaOps.INSTANCE, value));
            return this;
        }

        public PresetSpawnerStats build() {
            return new PresetSpawnerStats(this.stats.build());
        }
    }

}
