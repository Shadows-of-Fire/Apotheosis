package shadows.deadly.config;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import shadows.Apotheosis;

/**
 * Class of various constants, as this mod relies heavily on strings.
 * @author Shadows
 *
 */
public class DeadlyConstants {

	public static final String GENERAL = Configuration.CATEGORY_GENERAL;
	public static final String FREQUENCY = "Frequencies";
	public static final String VEINS = "Veins";
	public static final String BOSSES = "Bosses";
	public static final String BRUTAL_MOBS = "Brutal Mobs";
	public static final String BRUTAL_SPAWNERS = "Brutal Spawners";
	public static final String CHESTS = "Chests";
	public static final String DUNGEON_SPAWNERS = "Dungeon Spawners";
	public static final String DUNGEON_TYPES = "Dungeon Types";
	public static final String DUNGEONS = "Dungeons";
	public static final String NESTS = "Silverfish Nests";
	public static final String POTION_TRAPS = "Potion Traps";
	public static final String RANDOM_SPAWNERS = "Random Spawners";
	public static final String SPAWNER_TRAPS = "Spawner Traps";
	public static final String SPAWNERS = "Spawners";
	public static final String SWARM_SPAWNERS = "Swarm Spawners";
	public static final String TOWERS = "Towers";
	public static final SpawnerStats BRUTAL_SPAWNER_STATS = new SpawnerStats(BRUTAL_SPAWNERS, 20, 200, 400, 6, 6, 4, 16);
	public static final String[] BRUTAL_DEFAULT_MOBS = { "4@minecraft:zombie", "1@minecraft:skeleton", "1@minecraft:spider", "1@minecraft:cave_spider", "1@minecraft:creeper", "1@deadlyworld:random" };
	public static final ResourceLocation RANDOM = new ResourceLocation(Apotheosis.MODID, "random");
	public static final String[] BOSS_DEFAULT_MOBS = { "4@minecraft:zombie", "3@minecraft:skeleton", "2@minecraft:spider", "1@minecraft:cave_spider", "1@minecraft:creeper", "1@minecraft:wither_skeleton" };
	public static final String[] DUNGEON_DEFAULT_MOBS = { "4@minecraft:zombie", "4@minecraft:skeleton", "2@minecraft:spider", "1@minecraft:cave_spider", "2@minecraft:creeper", "1@minecraft:silverfish", "1@deadlyworld:random" };
	public static final String[] SWARM_DEFAULT_MOBS = { "4@minecraft:zombie", "2@minecraft:skeleton", "5@minecraft:spider", "8@minecraft:cave_spider", "1@minecraft:creeper" };
	public static final SpawnerStats DUNGEON_SPAWNER_STATS = new SpawnerStats(DUNGEONS, 20, 200, 400, 4, 6, 4, 16);
	public static final SpawnerStats SWARM_SPAWNER_STATS = new SpawnerStats(SWARM_SPAWNERS, 20, 75, 300, 8, 32, 6, 8);
	public static final SpawnerStats NEST_SPAWNER_STATS = new SpawnerStats(NESTS, 20, 100, 300, 6, 16, 4, 5);
	public static final String[] ROGUE_DEFAULT_MOBS = { "4@minecraft:zombie", "3@minecraft:skeleton", "3@minecraft:spider", "2@minecraft:cave_spider", "2@minecraft:creeper", "1@deadlyworld:random" };

	//These are outdated
	public static final String[] CHEST_TYPES = { "normal", "trapped", "mine", "indie", "valuable" };
	public static final String[] TOWER_TYPES = { "arrow", "arrow_fire", "double", "double_fire", "spawner", "spawner_fire", "chest", "chest_fire" };

	public static enum TrapType {
		CREEPER,
		RANDOM,
		NORMAL;
	}

	public static enum PotionTrapType {
		DAMAGE(MobEffects.INSTANT_DAMAGE),
		POISON(MobEffects.POISON),
		SLOW(MobEffects.SLOWNESS),
		NAUSEA(MobEffects.NAUSEA),
		BLINDNESS(MobEffects.BLINDNESS),
		HUNGER(MobEffects.HUNGER),
		FATIGUE(MobEffects.MINING_FATIGUE);

		public final Potion potion;

		PotionTrapType(Potion potion) {
			this.potion = potion;
		}
	}
}
