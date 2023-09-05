package dev.shadowsoffire.apotheosis.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public record SpawnerStats(int spawnDelay, int minDelay, int maxDelay, int spawnCount, int maxNearbyEntities, int spawnRange, int playerRange) {

    public static final Codec<SpawnerStats> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.INT.optionalFieldOf("spawn_delay", 20).forGetter(SpawnerStats::spawnDelay),
            Codec.INT.optionalFieldOf("min_delay", 200).forGetter(SpawnerStats::minDelay),
            Codec.INT.optionalFieldOf("max_delay", 800).forGetter(SpawnerStats::maxDelay),
            Codec.INT.optionalFieldOf("spawn_count", 4).forGetter(SpawnerStats::spawnCount),
            Codec.INT.optionalFieldOf("max_nearby_entities", 6).forGetter(SpawnerStats::maxNearbyEntities),
            Codec.INT.optionalFieldOf("spawn_range", 4).forGetter(SpawnerStats::spawnRange),
            Codec.INT.optionalFieldOf("player_activation_range", 16).forGetter(SpawnerStats::playerRange))
        .apply(inst, SpawnerStats::new));

    public SpawnerStats() {
        this(20, 200, 800, 4, 6, 4, 16);
    }

    public void apply(SpawnerBlockEntity entity) {
        BaseSpawner base = entity.spawner;
        base.spawnDelay = this.spawnDelay;
        base.minSpawnDelay = this.minDelay;
        base.maxSpawnDelay = this.maxDelay;
        base.spawnCount = this.spawnCount;
        base.maxNearbyEntities = this.maxNearbyEntities;
        base.spawnRange = this.spawnRange;
        base.requiredPlayerRange = this.playerRange;
    }

}
