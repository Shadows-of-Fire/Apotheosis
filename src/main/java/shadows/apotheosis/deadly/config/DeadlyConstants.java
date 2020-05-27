package shadows.apotheosis.deadly.config;

import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.util.SpawnerStats;
import shadows.placebo.config.Configuration;

/**
 * Class of various constants.  Used for configs.
 * @author Shadows
 *
 */
public class DeadlyConstants {

	public static final String GENERAL = Configuration.CATEGORY_GENERAL;
	public static final String FREQUENCY = "Frequencies";
	public static final String BOSSES = "Bosses";
	public static final String BRUTAL_SPAWNERS = "Brutal Spawners";
	public static final String RANDOM_SPAWNERS = "Random Spawners";
	public static final String SWARM_SPAWNERS = "Swarm Spawners";
	public static final String DUNGEONS = "Dungeons";
	public static final SpawnerStats BRUTAL_SPAWNER_STATS = new SpawnerStats(BRUTAL_SPAWNERS, 20, 200, 400, 6, 6, 4, 16);
	public static final String[] BRUTAL_DEFAULT_MOBS = { "3@minecraft:zombie", "3@minecraft:skeleton", "2@minecraft:husk", "2@minecraft:drowned", "2@minecraft:stray", "1@minecraft:spider" };
	public static final ResourceLocation RANDOM = new ResourceLocation(Apotheosis.MODID, "random");
	public static final String[] BOSS_DEFAULT_MOBS = { "3@minecraft:zombie", "3@minecraft:skeleton", "2@minecraft:husk", "2@minecraft:drowned", "2@minecraft:stray", "1@minecraft:wither_skeleton" };
	public static final String[] SWARM_DEFAULT_MOBS = { "4@minecraft:zombie", "2@minecraft:skeleton", "5@minecraft:spider", "8@minecraft:cave_spider", "1@minecraft:creeper" };
	public static final SpawnerStats SWARM_SPAWNER_STATS = new SpawnerStats(SWARM_SPAWNERS, 20, 75, 300, 8, 32, 6, 8);
	public static final String[] BRUTAL_POTIONS = { "minecraft:resistance@2", "minecraft:fire_resistance@1", "minecraft:regeneration@1", "minecraft:speed@2", "minecraft:water_breathing@1", "minecraft:strength@1" };
	public static final String AFFIXES = "Affixes";
}
