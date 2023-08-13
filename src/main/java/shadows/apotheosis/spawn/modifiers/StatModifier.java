package shadows.apotheosis.spawn.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class StatModifier<T> {

    public final SpawnerStat<T> stat;
    public final T value, min, max;

    @SuppressWarnings("unchecked")
    protected StatModifier(SpawnerStat<T> stat, T value, T min, T max) {
        this.stat = stat;
        this.value = value;
        this.min = min.equals(-1) ? (T) Integer.valueOf(0) : min;
        this.max = max.equals(-1) ? (T) Integer.valueOf(Integer.MAX_VALUE) : max;
    }

    public boolean apply(ApothSpawnerTile tile) {
        return this.stat.apply(this.value, this.min, this.max, tile);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static StatModifier<?> parse(JsonObject obj) {
        SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(obj.get("id").getAsString());
        if (stat == null) throw new JsonParseException("Failed to parse a stat modifier - missing or invalid ID");
        return new StatModifier(stat, stat.parseValue(obj.get("value")), stat.parseValue(obj.get("min")), stat.parseValue(obj.get("max")));
    }

}
