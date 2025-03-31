package dev.shadowsoffire.apotheosis.mobs;

import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.DataMaps;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;

/**
 * Record holding the per-dimension invader spawn rules for use by {@link DataMaps#INVADER_SPAWN_RULES}.
 *
 * @param spawnChances The per-world-tier spawn chances for invaders in the target dimension.
 * @param cooldown     An optional cooldown override for this dimension. If not set, the global cooldown will be used.
 * @param surfaceType  The surface type used for this dimension.
 */
public record InvaderSpawnRules(Map<WorldTier, Float> spawnChances, Optional<Integer> cooldown, SurfaceType surfaceType) {

    public static final Codec<InvaderSpawnRules> CODEC = RecordCodecBuilder.<InvaderSpawnRules>create(inst -> inst
        .group(
            WorldTier.mapCodec(Codec.floatRange(0, 1)).fieldOf("spawn_chances").forGetter(InvaderSpawnRules::spawnChances),
            Codec.intRange(0, 720000).optionalFieldOf("cooldown").forGetter(InvaderSpawnRules::cooldown),
            SurfaceType.CODEC.fieldOf("surface_type").forGetter(InvaderSpawnRules::surfaceType))
        .apply(inst, InvaderSpawnRules::new))
        .validate(InvaderSpawnRules::validate);

    private static DataResult<InvaderSpawnRules> validate(InvaderSpawnRules rules) {
        if (rules.spawnChances.size() == WorldTier.values().length) {
            return DataResult.success(rules);
        }
        else {
            StringBuilder sb = new StringBuilder("Missing Spawn Chances for the following world tiers: ");
            for (WorldTier tier : WorldTier.values()) {
                if (!rules.spawnChances.containsKey(tier)) {
                    sb.append(tier.getSerializedName()).append(" ");
                }
            }
            return DataResult.error(() -> sb.toString());
        }
    }
}
