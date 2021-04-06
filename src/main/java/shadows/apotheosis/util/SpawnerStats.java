package shadows.apotheosis.util;

import com.google.gson.annotations.SerializedName;

import shadows.placebo.util.SpawnerBuilder;
import shadows.placebo.util.SpawnerEditor;

public class SpawnerStats {

	@SerializedName("spawn_delay")
	protected final int spawnDelay;
	@SerializedName("min_delay")
	protected final int minDelay;
	@SerializedName("max_delay")
	protected final int maxDelay;
	@SerializedName("spawn_count")
	protected final int spawnCount;
	@SerializedName("max_nearby_entities")
	protected final int maxNearbyEntities;
	@SerializedName("spawn_range")
	protected final int spawnRange;
	@SerializedName("player_activation_range")
	protected final int playerRange;

	public SpawnerStats() {
		this(20, 200, 800, 4, 6, 4, 16);
	}

	public SpawnerStats(int delay, int min, int max, int count, int nearby, int range, int playerRange) {
		this.spawnDelay = delay;
		this.minDelay = min;
		this.maxDelay = max;
		this.spawnCount = count;
		this.maxNearbyEntities = nearby;
		this.spawnRange = range;
		this.playerRange = playerRange;
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