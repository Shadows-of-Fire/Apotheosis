package shadows.apotheosis.util;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.level.BaseSpawner;

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

	public void apply(BaseSpawner spawner){
		spawner.spawnDelay = this.spawnDelay;
		spawner.minSpawnDelay = this.minDelay;
		spawner.maxSpawnDelay = this.maxDelay;
		spawner.spawnCount = this.spawnCount;
		spawner.maxNearbyEntities = this.maxNearbyEntities;
		spawner.spawnRange = this.spawnRange;
		spawner.requiredPlayerRange = this.playerRange;
	}

}