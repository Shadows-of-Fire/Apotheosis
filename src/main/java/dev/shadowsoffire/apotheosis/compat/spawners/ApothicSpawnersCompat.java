package dev.shadowsoffire.apotheosis.compat.spawners;

import java.util.Map;

import com.mojang.serialization.Dynamic;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.PresetSpawnerStats;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.fml.ModList;

/**
 * Compat handler for Apothic Spawners. All classes that reference Apothic Spawners types live in this package,
 * and may only be classloaded when {@link #isLoaded()} is true. This class itself is always safe to load.
 */
public class ApothicSpawnersCompat {

    public static final String MODID = "apothic_spawners";

    private static final boolean LOADED = ModList.get().isLoaded(MODID);

    public static boolean isLoaded() {
        return LOADED;
    }

    /**
     * Applies preset spawner stats through Apothic Spawners' stat registry. May only be called when {@link #isLoaded()}.
     */
    public static void applyStats(PresetSpawnerStats stats, SpawnerBlockEntity tile) {
        Inner.applyStats(stats, tile);
    }

    private static class Inner {

        static void applyStats(PresetSpawnerStats stats, SpawnerBlockEntity tile) {
            if (!(tile instanceof ApothSpawnerTile spawner)) {
                Apotheosis.LOGGER.error("Expected an ApothSpawnerTile at {} but found {} - preset spawner stats will not be applied.", tile.getBlockPos(), tile.getClass().getName());
                return;
            }
            for (Map.Entry<ResourceLocation, Dynamic<?>> entry : stats.stats().entrySet()) {
                SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(entry.getKey());
                if (stat == null) {
                    Apotheosis.LOGGER.error("Unknown spawner stat {} found in preset spawner stats - it will be skipped.", entry.getKey());
                    continue;
                }
                applyStat(stat, spawner, entry.getValue());
            }
        }

        private static <T> void applyStat(SpawnerStat<T> stat, ApothSpawnerTile tile, Dynamic<?> value) {
            stat.getValueCodec().parse(value)
                .resultOrPartial(msg -> Apotheosis.LOGGER.error("Failed to parse value for spawner stat {}: {}", stat.getId(), msg))
                .ifPresent(v -> stat.setValue(tile, v));
        }

    }

}
