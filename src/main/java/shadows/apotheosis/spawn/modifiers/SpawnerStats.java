package shadows.apotheosis.spawn.modifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.gson.JsonElement;

import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class SpawnerStats {

	public static final Map<String, SpawnerStat<?>> REGISTRY = new HashMap<>();

	public static final SpawnerStat<Integer> MIN_DELAY = register(new IntStat("min_delay", s -> s.spawner.minSpawnDelay, (s, v) -> s.spawner.minSpawnDelay = v));

	public static final SpawnerStat<Integer> MAX_DELAY = register(new IntStat("max_delay", s -> s.spawner.maxSpawnDelay, (s, v) -> s.spawner.maxSpawnDelay = v));

	public static final SpawnerStat<Integer> SPAWN_COUNT = register(new IntStat("spawn_count", s -> s.spawner.spawnCount, (s, v) -> s.spawner.spawnCount = v));

	public static final SpawnerStat<Integer> MAX_NEARBY_ENTITIES = register(new IntStat("max_nearby_entities", s -> s.spawner.maxNearbyEntities, (s, v) -> s.spawner.maxNearbyEntities = v));

	public static final SpawnerStat<Integer> REQ_PLAYER_RANGE = register(new IntStat("req_player_range", s -> s.spawner.requiredPlayerRange, (s, v) -> s.spawner.requiredPlayerRange = v));

	public static final SpawnerStat<Integer> SPAWN_RANGE = register(new IntStat("spawn_range", s -> s.spawner.spawnRange, (s, v) -> s.spawner.spawnRange = v));

	public static final SpawnerStat<Boolean> IGNORE_PLAYERS = register(new BoolStat("ignore_players", s -> s.ignoresPlayers, (s, v) -> s.ignoresPlayers = v));

	public static final SpawnerStat<Boolean> IGNORE_CONDITIONS = register(new BoolStat("ignore_conditions", s -> s.ignoresConditions, (s, v) -> s.ignoresConditions = v));

	public static final SpawnerStat<Boolean> REDSTONE_CONTROL = register(new BoolStat("redstone_control", s -> s.redstoneControl, (s, v) -> s.redstoneControl = v));

	public static final SpawnerStat<Boolean> IGNORE_LIGHT = register(new BoolStat("ignore_light", s -> s.ignoresLight, (s, v) -> s.ignoresLight = v));

	public static final SpawnerStat<Boolean> NO_AI = register(new BoolStat("no_ai", s -> s.hasNoAI, (s, v) -> s.hasNoAI = v));
	
	public static final SpawnerStat<Boolean> SILENT = register(new BoolStat("silent", s -> s.silent, (s, v) -> s.silent = v));

	private static <T extends SpawnerStat<?>> T register(T t) {
		REGISTRY.put(t.getId(), t);
		return t;
	}

	private static abstract class Base<T> implements SpawnerStat<T> {

		protected final String id;
		protected final Function<ApothSpawnerTile, T> getter;
		protected final BiConsumer<ApothSpawnerTile, T> setter;

		private Base(String id, Function<ApothSpawnerTile, T> getter, BiConsumer<ApothSpawnerTile, T> setter) {
			this.id = id;
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		public String getId() {
			return this.id;
		}

	}

	private static class BoolStat extends Base<Boolean> {

		private BoolStat(String id, Function<ApothSpawnerTile, Boolean> getter, BiConsumer<ApothSpawnerTile, Boolean> setter) {
			super(id, getter, setter);
		}

		@Override
		public Boolean parseValue(JsonElement value) {
			return value == null ? false : value.getAsBoolean();
		}

		@Override
		public boolean apply(Boolean value, Boolean min, Boolean max, ApothSpawnerTile spawner) {
			boolean old = getter.apply(spawner);
			setter.accept(spawner, value);
			return old != getter.apply(spawner);
		}

		@Override
		public Class<Boolean> getTypeClass() {
			return Boolean.class;
		}
	}

	private static class IntStat extends Base<Integer> {

		private IntStat(String id, Function<ApothSpawnerTile, Integer> getter, BiConsumer<ApothSpawnerTile, Integer> setter) {
			super(id, getter, setter);
		}

		@Override
		public Integer parseValue(JsonElement value) {
			return value == null ? 0 : value.getAsInt();
		}

		@Override
		public boolean apply(Integer value, Integer min, Integer max, ApothSpawnerTile spawner) {
			int old = getter.apply(spawner);
			setter.accept(spawner, MathHelper.clamp(old + value, min, max));
			return old != getter.apply(spawner);
		}

		@Override
		public Class<Integer> getTypeClass() {
			return Integer.class;
		}
	}

}
