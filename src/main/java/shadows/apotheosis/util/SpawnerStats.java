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
		this.spawnDelay = delay;
		this.minDelay = min;
		this.maxDelay = max;
		this.spawnCount = count;
		this.maxNearbyEntities = nearby;
		this.spawnRange = range;
		this.playerRange = playerRange;
		this.category = "Spawner Stats: " + name;
	}

	public void load(Configuration cfg) {
		this.spawnDelay = cfg.getInt("Spawn Delay", this.category, this.spawnDelay, 1, Short.MAX_VALUE, "The delay before first spawn on this spawner.");
		this.minDelay = cfg.getInt("Min Delay", this.category, this.minDelay, 1, Short.MAX_VALUE, "The minimum delay between spawns");
		this.maxDelay = cfg.getInt("Max Delay", this.category, this.maxDelay, 1, Short.MAX_VALUE, "The maximum delay between spawns");
		this.spawnCount = cfg.getInt("Spawn Count", this.category, this.spawnCount, 1, Short.MAX_VALUE, "The number of mobs that will spawn.");
		this.maxNearbyEntities = cfg.getInt("Max Nearby Entities", this.category, this.maxNearbyEntities, 1, Short.MAX_VALUE, "The maximum number of nearby entities (when hit, the spawner turns off).");
		this.spawnRange = cfg.getInt("Spawn Range", this.category, this.spawnRange, 1, Short.MAX_VALUE, "The spawn range.");
		this.playerRange = cfg.getInt("Player Range", this.category, this.playerRange, 1, Short.MAX_VALUE, "The required distance a player must be within for this spawner to work.");
	}

	public SpawnerBuilder apply(SpawnerBuilder builder) {
		builder.setDelay(this.spawnDelay).setMinAndMaxDelay(this.minDelay, this.maxDelay);
		builder.setSpawnCount(this.spawnCount).setMaxNearbyEntities(this.maxNearbyEntities);
		builder.setSpawnRange(this.spawnRange).setPlayerRange(this.playerRange);
		return builder;
	}

	public SpawnerEditor apply(SpawnerEditor editor) {
		editor.setDelay(this.spawnDelay).setMinAndMaxDelay(this.minDelay, this.maxDelay);
		editor.setSpawnCount(this.spawnCount).setMaxNearbyEntities(this.maxNearbyEntities);
		editor.setSpawnRange(this.spawnRange).setPlayerRange(this.playerRange);
		return editor;
	}

}