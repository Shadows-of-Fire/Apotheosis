package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.Map;
import java.util.TreeMap;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class SpawnCooldownSavedData extends SavedData {

    private static final Codec<SpawnCooldownSavedData> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.INT)
        .xmap(SpawnCooldownSavedData::unpack, SpawnCooldownSavedData::pack);

    public static final SavedDataType<SpawnCooldownSavedData> TYPE = new SavedDataType<>(
        Apotheosis.loc("boss_cooldowns"), SpawnCooldownSavedData::new, CODEC);

    public Object2IntMap<Identifier> bossCooldowns = new Object2IntOpenHashMap<>();

    private static SpawnCooldownSavedData unpack(Map<Identifier, Integer> source) {
        SpawnCooldownSavedData data = new SpawnCooldownSavedData();
        source.forEach((id, value) -> data.bossCooldowns.put(id, value.intValue()));
        return data;
    }

    private Map<Identifier, Integer> pack() {
        Map<Identifier, Integer> out = new TreeMap<>();
        for (Object2IntMap.Entry<Identifier> e : this.bossCooldowns.object2IntEntrySet()) {
            out.put(e.getKey(), e.getIntValue());
        }
        return out;
    }

    public void tick(Identifier level) {
        this.bossCooldowns.computeIntIfPresent(level, (key, value) -> Math.max(0, value - 1));
    }

    public boolean isOnCooldown(Level level) {
        return this.isOnCooldown(level.dimension().identifier());
    }

    public boolean isOnCooldown(Identifier level) {
        return this.bossCooldowns.getInt(level) > 0;
    }

    public void startCooldown(Level level, int timer) {
        this.startCooldown(level.dimension().identifier(), timer);
    }

    public void startCooldown(Identifier level, int timer) {
        this.bossCooldowns.put(level, timer);
    }

}
