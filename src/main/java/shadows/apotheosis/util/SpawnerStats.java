package shadows.apotheosis.util;

import shadows.placebo.config.Configuration;
import shadows.placebo.util.SpawnerBuilder;
import shadows.placebo.util.SpawnerEditor;

public class SpawnerStats {

	private int spawnDelay = 20;
	private int minDelay = 200;
	private int maxDelay = 800;
	private int spawnCount = 4;
	private int maxNearbyEntities = 6;
	private int spawnRange = 4;
	private int playerRange = 16;

	private final String category;

	public SpawnerStats(String name) {
		this(name, 20, 200, 800, 4, 6, 4, 16);
	}

	public SpawnerStats(String name, int delay, int min, int max, int count, int nearby, int range, int playerRange) {
		spawnDelay = delay;
		minDelay = min;
		maxDelay = max;
		spawnCount = count;
		maxNearbyEntities = nearby;
		spawnRange = range;
		this.playerRange = playerRange;
		category = "Spawner Stats: " + name;
	}

	public void load(Configuration cfg) {
		spawnDelay = cfg.getInt("Spawn Delay", category, spawnDelay, 1, Short.MAX_VALUE, "The delay before first spawn on this spawner.");
		minDelay = cfg.getInt("Min Delay", category, minDelay, 1, Short.MAX_VALUE, "The minimum delay between spawns");
		maxDelay = cfg.getInt("Max Delay", category, maxDelay, 1, Short.MAX_VALUE, "The maximum delay between spawns");
		spawnCount = cfg.getInt("Spawn Count", category, spawnCount, 1, Short.MAX_VALUE, "The number of mobs that will spawn.");
		maxNearbyEntities = cfg.getInt("Max Nearby Entities", category, maxNearbyEntities, 1, Short.MAX_VALUE, "The maximum number of nearby entities (when hit, the spawner turns off).");
		spawnRange = cfg.getInt("Spawn Range", category, spawnRange, 1, Short.MAX_VALUE, "The spawn range.");
		playerRange = cfg.getInt("Player Range", category, playerRange, 1, Short.MAX_VALUE, "The required distance a player must be within for this spawner to work.");
	}

	public SpawnerBuilder apply(SpawnerBuilder builder) {
		builder.setDelay(spawnDelay).setMinAndMaxDelay(minDelay, maxDelay);
		builder.setSpawnCount(spawnCount).setMaxNearbyEntities(maxNearbyEntities);
		builder.setSpawnRange(spawnRange).setPlayerRange(playerRange);
		return builder;
	}

	public SpawnerEditor apply(SpawnerEditor editor) {
		editor.setDelay(spawnDelay).setMinAndMaxDelay(minDelay, maxDelay);
		editor.setSpawnCount(spawnCount).setMaxNearbyEntities(maxNearbyEntities);
		editor.setSpawnRange(spawnRange).setPlayerRange(playerRange);
		return editor;
	}

}