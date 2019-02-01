package shadows.deadly.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.deadly.DeadlyModule;
import shadows.deadly.util.DeadlyConstants;

/**
 * This class is functional.
 * @author Shadows
 *
 */
public class DeadlyConfig {

	public static Configuration config;

	//General Values
	public static int chargedCreeperChance = 2;
	public static float towerArrowDamage = 8F;
	public static boolean removeDungeons = true;

	//Brutal Mobs
	public static boolean brutalFireRes = true;
	public static int brutalRegenLevel = 1;
	public static int brutalResistLevel = 3;
	public static int brutalStrengthLevel = 0;
	public static int brutalSpeedLevel = 1;
	public static boolean brutalWaterBreathing = true;

	//Boss Stats
	public static int bossMaxLevel = 3;
	public static int bossRegenLevel = 2;
	public static int bossResistLevel = 1;
	public static boolean bossFireRes = true;
	public static boolean bossWaterBreathing = true;
	public static float bossHealthMultiplier = 4;
	public static float bossKnockbackResist = .85F;
	public static float bossSpeedMultiplier = .45F;
	public static float bossDamageBonus = 4;
	public static float bossLevelUpChance = .25F;
	public static float bossEnchantChance = .25F;
	public static float bossPotionChance = .45F;

	//Generation Chances
	public static float brutalSpawnerChance = .08F;
	public static float swarmSpawnerChance = 0;
	public static float bossChance = .05F;
	public static float chestChance = .1F;
	public static float fireTrapChance = .35F;
	public static float mineChance = .07F;
	public static float potionTrapChance = .2F;
	public static float silverfishNestChance = .1F;
	public static float spawnerChance = .2F;
	public static float spwanerTrapChance = .15F;
	public static float towerChance = .35F;

	//Weighted entries
	public static List<Pair<Integer, ResourceLocation>> brutalWeightedMobs = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> bossWeightedMobs = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> dungeonWeightedMobs = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> swarmWeightedMobs = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> rogueSpawnerWeightedMobs = new ArrayList<>();

	//Dungeons
	public static float dungeonArmorChance = .05F;
	public static float dungeonPlaceAttempts = 8;
	public static float dungeonSilverfishChance = .2F;
	public static int dungeonDefaultChance = 4;
	public static int dungeonBrutalChance = 1;
	public static int dungeonTowerChance = 1;
	public static int dungeonSwarmChance = 1;

	//Nests
	public static float nestAngerChance = .2F;

	//Rogue Spawners
	public static float spawnerArmorChance = .05F;
	public static float spawnerChestChance = .1F;
	public static float spawnerTrickChance = .05F;

	//Traps
	public static float coverChance = .8F;
	public static float carpetChance = .4F;

	public static void init() {
		Configuration c = config;
		c.load();

		chargedCreeperChance = c.getInt("Charged Creeper Chance", DeadlyConstants.GENERAL, 2, 0, 100, "Percent chance for creeper spawners to spawn charged creepers.");
		towerArrowDamage = c.getFloat("Arrow Damage", DeadlyConstants.TOWERS, 8F, 2F, Float.MAX_VALUE, "Damage arrows from towers deal. Translates roughly into half hearts of damage.");
		removeDungeons = c.getBoolean("Remove Vanilla Dungeons", DeadlyConstants.DUNGEONS, true, "If vanilla dungeons are blocked from generating in favor of Deadly World dungeons.");

		DeadlyConstants.BRUTAL_SPAWNER_STATS.load(c);
		DeadlyConstants.DUNGEON_SPAWNER_STATS.load(c);
		DeadlyConstants.SWARM_SPAWNER_STATS.load(c);
		DeadlyConstants.NEST_SPAWNER_STATS.load(c);

		brutalFireRes = c.getBoolean("Fire Resist", DeadlyConstants.BRUTAL_MOBS, true, "If brutal mobs will be immune to fire damage.");
		brutalRegenLevel = c.getInt("Regeneration", DeadlyConstants.BRUTAL_MOBS, 1, 0, 5, "If brutal mobs regen hp (0 heals 1 health every 2.5 sec, each rank halves the time between heals.)");
		brutalResistLevel = c.getInt("Resistance", DeadlyConstants.BRUTAL_MOBS, 3, 0, 5, "Increases damage resistance. (0 is -20% damage, each rank grants -20% damage.)");
		brutalStrengthLevel = c.getInt("Strength", DeadlyConstants.BRUTAL_MOBS, 0, 0, 5, "Increases melee damage. (0 is +130% damage, each rank grants +130% damage.)");
		brutalSpeedLevel = c.getInt("Swiftness", DeadlyConstants.BRUTAL_MOBS, 1, 0, 5, "Increases speed. (0 is +30% speed, each rank grants +30% speed.)");
		brutalWaterBreathing = c.getBoolean("Water Breathing", DeadlyConstants.BRUTAL_MOBS, true, "If true, brutal mobs will not drown.");

		bossMaxLevel = c.getInt("Max Level", DeadlyConstants.BOSSES, bossMaxLevel, 0, Integer.MAX_VALUE, "The max level of bosses.  Should not be higher than the max level Armor Set");
		bossRegenLevel = c.getInt("Regen Level", DeadlyConstants.BOSSES, bossRegenLevel, 0, Integer.MAX_VALUE, "The regeneration level of bosses.  Set to 0 to disable.");
		bossResistLevel = c.getInt("Resistance Level", DeadlyConstants.BOSSES, bossResistLevel, 0, Integer.MAX_VALUE, "The resistance level of bosses.  Set to 0 to disable.");
		bossFireRes = c.getBoolean("Fire Resistance", DeadlyConstants.BOSSES, true, "If bosses have fire resistance.");
		bossWaterBreathing = c.getBoolean("Water Breathing", DeadlyConstants.BOSSES, true, "If bosses have water breathing.");
		bossHealthMultiplier = c.getFloat("Health Multiplier", DeadlyConstants.BOSSES, bossHealthMultiplier, 0, Integer.MAX_VALUE, "The amount boss health is multiplied by.  Base hp * factor = final hp.");
		bossKnockbackResist = c.getFloat("Knockback Resist", DeadlyConstants.BOSSES, bossKnockbackResist, 0, Integer.MAX_VALUE, "The amount of knockback resist bosses have.");
		bossSpeedMultiplier = c.getFloat("Speed Multiplier", DeadlyConstants.BOSSES, bossSpeedMultiplier, 0, Integer.MAX_VALUE, "The amount boss speed is multiplied by.  Base speed * factor = final speed.");
		bossDamageBonus = c.getFloat("Damage Bonus", DeadlyConstants.BOSSES, bossDamageBonus, 0, Integer.MAX_VALUE, "The amount of extra damage bosses do, in half hearts.");
		bossLevelUpChance = c.getFloat("Level Up Chance", DeadlyConstants.BOSSES, bossLevelUpChance, 0, Integer.MAX_VALUE, "The level up chance, this is rolled once per number of levels.  Levels determine gear.");
		bossEnchantChance = c.getFloat("Random Enchantment Chance", DeadlyConstants.BOSSES, bossEnchantChance, 0, Integer.MAX_VALUE, "The chance a gear piece will be randomly enchanted.");
		bossPotionChance = c.getFloat("Random Potion Chance", DeadlyConstants.BOSSES, bossPotionChance, 0, Integer.MAX_VALUE, "The chance a boss will have extra random potion effects.");

		brutalSpawnerChance = c.getFloat("Brutal Spawner Chance", DeadlyConstants.FREQUENCY, brutalSpawnerChance, 0, 1, "The chance (per chunk) for a brutal spawner to try spawning.");
		swarmSpawnerChance = c.getFloat("Swarm Spawner Chance", DeadlyConstants.FREQUENCY, swarmSpawnerChance, 0, 1, "The chance (per chunk) for a swarm spawner to try spawning.");
		bossChance = c.getFloat("Boss Chance", DeadlyConstants.FREQUENCY, bossChance, 0, 1, "The chance (per chunk) for a boss to try spawning.");
		chestChance = c.getFloat("Chest Chance", DeadlyConstants.FREQUENCY, chestChance, 0, 1, "The chance (per chunk) for a chest to try spawning.");
		fireTrapChance = c.getFloat("Fire Trap Chance", DeadlyConstants.FREQUENCY, fireTrapChance, 0, 1, "The chance (per chunk) for a fire trap to try spawning.");
		mineChance = c.getFloat("Mine Chance", DeadlyConstants.FREQUENCY, mineChance, 0, 1, "The chance (per chunk) for a mine to try spawning.");
		potionTrapChance = c.getFloat("Potion Trap Chance", DeadlyConstants.FREQUENCY, potionTrapChance, 0, 1, "The chance (per chunk) for a potion trap to try spawning.");
		silverfishNestChance = c.getFloat("Silverfish Nest Chance", DeadlyConstants.FREQUENCY, silverfishNestChance, 0, 1, "The chance (per chunk) for a silverfish nest to try spawning.");
		spawnerChance = c.getFloat("Spawner Chance", DeadlyConstants.FREQUENCY, spawnerChance, 0, 1, "The chance (per chunk) for a spawner to try spawning.");
		spwanerTrapChance = c.getFloat("Spawner Trap Chance", DeadlyConstants.FREQUENCY, spwanerTrapChance, 0, 1, "The chance (per chunk) for a spawner trap to try spawning.");
		towerChance = c.getFloat("Tower Chance", DeadlyConstants.FREQUENCY, towerChance, 0, 1, "The chance (per chunk) for an arrow tower to try spawning.");

		String[] fromCfg = c.getStringList("Brutal Spawner Mobs", DeadlyConstants.BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_DEFAULT_MOBS, "The possible spawn entries for brutal spawners.  Format is weight@entity, entity is a registry name.  deadlyworld:random is a special name, used to generate a spawner that spawns any mob.");

		for (String s : fromCfg) {
			String[] split = s.split("@");
			try {
				int weight = Integer.parseInt(split[0]);
				ResourceLocation name = new ResourceLocation(split[1]);
				EntityEntry e = ForgeRegistries.ENTITIES.getValue(name);
				if (weight > 0 && (e != null || name.equals(DeadlyConstants.RANDOM))) brutalWeightedMobs.add(Pair.of(weight, name));
				else DeadlyModule.LOGGER.error("Invalid brutal spawner entry: " + s + ".  It will be ignored! (Weight <= 0 or Entity does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid brutal spawner entry: " + s + ".  It will be ignored!  (Invalid format)");
			}
		}

		String[] dungeonFromCfg = c.getStringList("Dungeon Spawner Mobs", DeadlyConstants.DUNGEONS, DeadlyConstants.DUNGEON_DEFAULT_MOBS, "The possible spawn entries for dungeon spawners.  Format is weight@entity, entity is a registry name.  deadlyworld:random is a special name, used to generate a spawner that spawns any mob.");

		for (String s : dungeonFromCfg) {
			String[] split = s.split("@");
			try {
				int weight = Integer.parseInt(split[0]);
				ResourceLocation name = new ResourceLocation(split[1]);
				EntityEntry e = ForgeRegistries.ENTITIES.getValue(name);
				if (weight > 0 && (e != null || name.equals(DeadlyConstants.RANDOM))) dungeonWeightedMobs.add(Pair.of(weight, name));
				else DeadlyModule.LOGGER.error("Invalid dungeon spawner entry: " + s + ".  It will be ignored! (Weight <= 0 or Entity does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid dungeon spawner entry: " + s + ".  It will be ignored!  (Invalid format)");
			}
		}

		String[] bossFromCfg = c.getStringList("Boss Spawner Mobs", DeadlyConstants.BOSSES, DeadlyConstants.BOSS_DEFAULT_MOBS, "The possible mob types for bosses.  Format is weight@entity, entity is a registry name.");

		for (String s : bossFromCfg) {
			String[] split = s.split("@");
			try {
				int weight = Integer.parseInt(split[0]);
				ResourceLocation name = new ResourceLocation(split[1]);
				EntityEntry e = ForgeRegistries.ENTITIES.getValue(name);
				if (weight > 0 && e != null) bossWeightedMobs.add(Pair.of(weight, name));
				else DeadlyModule.LOGGER.error("Invalid boss entry: " + s + ".  It will be ignored! (Weight <= 0 or Entity does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid boss entry: " + s + ".  It will be ignored!  (Invalid format)");
			}
		}

		String[] swarmFromCfg = c.getStringList("Swarm Spawner Mobs", DeadlyConstants.SWARM_SPAWNERS, DeadlyConstants.SWARM_DEFAULT_MOBS, "The possible spawn entries for swarm spawners.  Format is weight@entity, entity is a registry name.");

		for (String s : swarmFromCfg) {
			String[] split = s.split("@");
			try {
				int weight = Integer.parseInt(split[0]);
				ResourceLocation name = new ResourceLocation(split[1]);
				EntityEntry e = ForgeRegistries.ENTITIES.getValue(name);
				if (weight > 0 && (e != null || name.equals(DeadlyConstants.RANDOM))) swarmWeightedMobs.add(Pair.of(weight, name));
				else DeadlyModule.LOGGER.error("Invalid swarm spawner entry: " + s + ".  It will be ignored! (Weight <= 0 or Entity does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid swarm spawner entry: " + s + ".  It will be ignored!  (Invalid format)");
			}
		}

		dungeonArmorChance = c.getFloat("Armor Chance", DeadlyConstants.DUNGEONS, dungeonArmorChance, 0, 1, "The chance for a spawner to be covered in obsidian (if it's a normal spawner).");
		dungeonPlaceAttempts = c.getFloat("Place Attempts", DeadlyConstants.DUNGEONS, dungeonPlaceAttempts, 0, 1, "The number of dungeon generation attempts per chunk. Be careful; increasing this far beyond 8.0 (vanilla) could cause lag.  Example: 9.25 is 9 attempts with a 25% chance of a fourth attempt.");
		dungeonSilverfishChance = c.getFloat("Silverfish Chance", DeadlyConstants.DUNGEONS, dungeonSilverfishChance, 0, 1, "The chance for any cobblestone block in a dungeon to instead be a silverfish block.");
		dungeonDefaultChance = c.getInt("Default Spawner Chance", DeadlyConstants.DUNGEONS, dungeonDefaultChance, 0, 50, "The chance for a normal spawner to be used in dungeons.");
		dungeonBrutalChance = c.getInt("Brutal Spawner Chance", DeadlyConstants.DUNGEONS, dungeonBrutalChance, 0, 50, "The chance for a brutal spawner to be used in dungeons.");
		dungeonTowerChance = c.getInt("Tower Spawner Chance", DeadlyConstants.DUNGEONS, dungeonTowerChance, 0, 50, "The chance for an arrow tower to be used in dungeons.");
		dungeonSwarmChance = c.getInt("Swarm Spawner Chance", DeadlyConstants.DUNGEONS, dungeonSwarmChance, 0, 50, "The chance for a swarm spawner to be used in dungeons.");

		nestAngerChance = c.getFloat("Nest Anger Chance", DeadlyConstants.NESTS, nestAngerChance, 0, 1, "Chance for a silverfish nest to be abnormally aggressive.");

		spawnerArmorChance = c.getFloat("Spawner Armor Chance", DeadlyConstants.SPAWNERS, spawnerArmorChance, 0, 1, "Chance for a mob spawner to be covered in obsidian.");
		spawnerChestChance = c.getFloat("Spawner Chest Chance", DeadlyConstants.SPAWNERS, spawnerChestChance, 0, 1, "Chance for a mob spawner to have a chest below it. If the spawner is armored, its chest will also be armored and have better loot.");
		spawnerTrickChance = c.getFloat("Spawner Trick Chance", DeadlyConstants.SPAWNERS, spawnerTrickChance, 0, 1, "Chance for an armored mob spawner to be a chest instead, if it doesn't already have a chest below it.");

		String[] rogueFromCfg = c.getStringList("Rogue Spawner Mobs", DeadlyConstants.SPAWNERS, DeadlyConstants.ROGUE_DEFAULT_MOBS, "The possible spawn entries for rogue spawners.  Format is weight@entity, entity is a registry name. deadlyworld:random is a special name, used to generate a spawner that spawns any mob.");

		for (String s : rogueFromCfg) {
			String[] split = s.split("@");
			try {
				int weight = Integer.parseInt(split[0]);
				ResourceLocation name = new ResourceLocation(split[1]);
				EntityEntry e = ForgeRegistries.ENTITIES.getValue(name);
				if (weight > 0 && (e != null || name.equals(DeadlyConstants.RANDOM))) rogueSpawnerWeightedMobs.add(Pair.of(weight, name));
				else DeadlyModule.LOGGER.error("Invalid swarm spawner entry: " + s + ".  It will be ignored! (Weight <= 0 or Entity does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid swarm spawner entry: " + s + ".  It will be ignored!  (Invalid format)");
			}
		}

		coverChance = c.getFloat("Trap Cover Chance", DeadlyConstants.GENERAL, coverChance, 0, 1, "Chance for traps to spawn with a cover block over them.");
		carpetChance = c.getFloat("Covered Trap Carpet Chance", DeadlyConstants.GENERAL, carpetChance, 0, 1, "Chance for covered traps to have carpet instead of a pressure plate.");

		/*
				c.get("Covered Trap Carpet Chance", DeadlyConstants.GENERAL, 0.4, "Chance (from 0 to 1) for covered traps to have carpet instead of a pressure plate.");
				c.get(General.COVERED_TRAP_CHANCE, DeadlyConstants.GENERAL, 0.8, "Chance (from 0 to 1) for traps to spawn with a cover block over them.");
				c.get(Veins.LAVA_COUNT, 4.0, "Lava vein stats. Defaults: count=4, size=10, height=0-32.");
				c.get(Veins.LAVA_MAX_HEIGHT, 32);
				c.get(Veins.LAVA_MIN_HEIGHT, 0);
				c.get(Veins.LAVA_SIZE, 10);
				c.get(Veins.SAND_COUNT, 0.3, "Sand vein stats. Defaults: count=0.3, size=64, height=0-62.");
				c.get(Veins.SAND_MAX_HEIGHT, 62);
				c.get(Veins.SAND_MIN_HEIGHT, 0);
				c.get(Veins.SAND_SIZE, 48);
				c.get(Veins.SILVERFISH_COUNT, 10.0, "Silverfish vein stats. Defaults: count=10, size=24, height=0-128.");
				c.get(Veins.SILVERFISH_MAX_HEIGHT, 128);
				c.get(Veins.SILVERFISH_MIN_HEIGHT, 0);
				c.get(Veins.SILVERFISH_SIZE, 24);
				c.get(Veins.WATER_COUNT, 8.0, "Water vein stats. Defaults: count=8, size=10, height=0-62.");
				c.get(Veins.WATER_MAX_HEIGHT, 62);
				c.get(Veins.WATER_MIN_HEIGHT, 0);
				c.get(Veins.WATER_SIZE, 10);
				c.get(Bosses.DAMAGE_BONUS, 4.0, "How much more damage (in half-hearts) bosses deal than normal mobs.");
				c.get(Bosses.EFFECT_CHANCE, 0.3, "Chance (from 0 to 1) for a boss to have a random, permanent potion effect. (Technically, the effect only lasts about 3.4 years.)");
				c.get(Bosses.ENCHANTMENT_CHANCE, 0.25, "Chance (from 0 to 1) for any equipment a boss wears to be enchanted. The unique item will always be enchanted.");
				c.get(Bosses.HEALTH_MULTIPLIER, 4.0, "How much health a boss has. (max health * health multipler = boss max health)");
				c.get(Bosses.KNOCKBACK_RESISTANCE, 0.85, "How resistant (from 0 to 1) bosses are to being knocked back.");
				c.get(Bosses.LEVEL_UP_CHANCE, 0.25, "Chance (from 0 to 1) to increase the boss material level. Rolled three times. (Chainmail -> Gold -> Iron -> Diamond)");
				c.get(Bosses.REGENERATION, 2, "Regeneration potion level. 0 disables the effect.");
				c.get(Bosses.RESISTANCE, 1, "Resistance potion level. 0 disables the effect.");
				c.get(Bosses.FIRE_RESISTANCE, true, "If true, bosses will be immune to fire damage.");
				c.get(Bosses.WATER_BREATHING, true, "If true, bosses will not drown.");
				c.get(Bosses.SPEED_MULTIPLIER, 0.85, "How fast bosses are compared to normal mobs.");
				c.get(BrutalSpawners.MAX_DELAY, 400, "Brutal spawner stats. Defaults: delay=200-400, nearby=6, playerrange=16, spawncount=6, spawnrange=4.");
				c.get(BrutalSpawners.MIN_DELAY, SpawnerEditor.MIN_SPAWN_DELAY);
				c.get(BrutalSpawners.NEARBY_ENTITY_CAP, SpawnerEditor.MAX_NEARBY_ENTITIES);
				c.get(BrutalSpawners.PLAYER_RANGE, SpawnerEditor.PLAYER_RANGE);
				c.get(BrutalSpawners.SPAWN_COUNT, 6);
				c.get(BrutalSpawners.SPAWN_RANGE, SpawnerEditor.SPAWN_RANGE);
				c.get(DungeonSpawners.MAX_DELAY, 400, "Spawner stats. Defaults: delay=200-400, nearby=6, playerrange=16, spawncount=4, spawnrange=4.");
				c.get(DungeonSpawners.MIN_DELAY, SpawnerEditor.MIN_SPAWN_DELAY);
				c.get(DungeonSpawners.NEARBY_ENTITY_CAP, SpawnerEditor.MAX_NEARBY_ENTITIES);
				c.get(DungeonSpawners.PLAYER_RANGE, SpawnerEditor.PLAYER_RANGE);
				c.get(DungeonSpawners.SPAWN_COUNT, SpawnerEditor.SPAWN_COUNT);
				c.get(DungeonSpawners.SPAWN_RANGE, SpawnerEditor.SPAWN_RANGE);
				c.get(Nests.ANGERED_CHANCE, 0.2, "Chance (from 0 to 1) for a silverfish nest to be abnormally aggressive.");
				c.get(Nests.MAX_DELAY, 300, "Silverfish nest spawner stats. Defaults: delay=100-300, nearby=16, playerrange=5, spawncount=6, spawnrange=4.");
				c.get(Nests.MIN_DELAY, 100);
				c.get(Nests.NEARBY_ENTITY_CAP, 16);
				c.get(Nests.PLAYER_RANGE, 5);
				c.get(Nests.SPAWN_COUNT, 6);
				c.get(Nests.SPAWN_RANGE, SpawnerEditor.SPAWN_RANGE);
				c.get(PotionTraps.DAZE_DURATION, 1600, "The duration of potions shot by daze potion traps. (Affected by proximity to the splash.)");
				c.get(PotionTraps.DAZE_POTENCY, 0, "The strength of potions shot by daze potion traps.");
				c.get(PotionTraps.HARM_POTENCY, 2, "The strength of potions shot by harm potion traps. (Affected by proximity to the splash.)");
				c.get(PotionTraps.POISON_DURATION, 2000, "The duration of potions shot by poison potion traps. (Affected by proximity to the splash.)");
				c.get(PotionTraps.POISON_POTENCY, 0, "The strength of potions shot by poison potion traps.");
				c.get(SpawnerTraps.CHEST_CHANCE, 0.1, "Chance (from 0 to 1) for a spawner trap to have a chest below it.");
				c.get(Spawners.ARMOR_CHANCE, 0.05, "Chance (from 0 to 1) for a mob spawner to be covered in obsidian.");
				c.get(Spawners.CHEST_CHANCE, 0.1, "Chance (from 0 to 1) for a mob spawner to have a chest below it. If the spawner is armored, its chest will also be armored and have better loot.");
				c.get(Spawners.MAX_DELAY, 600, "Spawner stats. Defaults: delay=200-600, nearby=6, playerrange=16, spawncount=4, spawnrange=4.");
				c.get(Spawners.MIN_DELAY, SpawnerEditor.MIN_SPAWN_DELAY);
				c.get(Spawners.NEARBY_ENTITY_CAP, SpawnerEditor.MAX_NEARBY_ENTITIES);
				c.get(Spawners.PLAYER_RANGE, SpawnerEditor.PLAYER_RANGE);
				c.get(Spawners.SPAWN_COUNT, SpawnerEditor.SPAWN_COUNT);
				c.get(Spawners.SPAWN_RANGE, SpawnerEditor.SPAWN_RANGE);
				c.get(Spawners.TRICK_CHANCE, 0.05, "Chance (from 0 to 1) for an armored mob spawner to be a chest instead, if it doesn't already have a chest below it.");
				c.get(SpawnerSwarms.MAX_DELAY, 600, "Swarm spawner stats. Defaults: delay=200-600, nearby=8, playerrange=8, spawncount=127, spawnrange=6.");
				c.get(SpawnerSwarms.MIN_DELAY, SpawnerEditor.MIN_SPAWN_DELAY);
				c.get(SpawnerSwarms.NEARBY_ENTITY_CAP, 8);
				c.get(SpawnerSwarms.PLAYER_RANGE, 8);
				c.get(SpawnerSwarms.SPAWN_COUNT, 127);
				c.get(SpawnerSwarms.SPAWN_RANGE, 6);
		
				setupWeightedCategory(DeadlyConstants.NESTS, DeadlyConstants.NEST_TYPES, 4, 1);
				setupWeightedCategory(DeadlyConstants.POTION_TRAPS, DeadlyConstants.POTION_TYPES, 2, 1);
				setupWeightedCategory(DeadlyConstants.RANDOM_SPAWNERS, DeadlyConstants.MOB_TYPES, 2, 1);
				setupWeightedCategory(DeadlyConstants.SPAWNER_TRAPS, DeadlyConstants.SPAWNER_TYPES, 4, 1);
				setupWeightedCategory(DeadlyConstants.SPAWNERS, DeadlyConstants.SPAWNER_TYPES, 4, 1);
				setupWeightedCategory(DeadlyConstants.SPAWNER_SWARMS, DeadlyConstants.MOB_TYPES, 4, 1);
				setupWeightedCategory(DeadlyConstants.BRUTAL_SPAWNERS, DeadlyConstants.SPAWNER_TYPES, 4, 1);
				setupWeightedCategory(DeadlyConstants.CHESTS, DeadlyConstants.CHEST_TYPES, 2, 1);
				setupWeightedCategory(DeadlyConstants.TOWERS, DeadlyConstants.TOWER_TYPES, 4, 1);
				setupWeightedCategory(DeadlyConstants.DUNGEON_SPAWNERS, DeadlyConstants.DUNGEON_MOB_TYPES, 4, 1);
				setupWeightedCategory(DeadlyConstants.DUNGEON_TYPES, DeadlyConstants.DUNGEON_FEATURES, 4, 1);
				setupWeightedCategory(DeadlyConstants.BOSSES_ROGUE, DeadlyConstants.MOB_TYPES, 4, 1);
		
				c.addCustomCategoryComment(DeadlyConstants.FREQUENCY, "The frequencies for all features added by this mod. (from 0 to 1)");
				c.addCustomCategoryComment(DeadlyConstants.GENERAL, "General and/or miscellaneous options.");
				c.addCustomCategoryComment(DeadlyConstants.VEINS, "The number of vein generation attempts per chunk and the generation DeadlyConstants for those veins. Example: 3.25 is 3 attempts with a 25% chance of a fourth attempt.");
				c.addCustomCategoryComment(DeadlyConstants.BOSSES, "The stats and loot table for each boss mob.");
				c.addCustomCategoryComment(DeadlyConstants.BOSSES_ROGUE, "The relative weights for each wandering boss mob type.");
				c.addCustomCategoryComment(DeadlyConstants.BRUTAL_MOBS, "The potion strengths for mobs spawned by brutal spawners (aka brutal mobs). Potions can be disabled be setting their amplifiers to -1.");
				c.addCustomCategoryComment(DeadlyConstants.BRUTAL_SPAWNERS, "The stats for brutal spawners and the relative weights for each brutal spawner type.");
				c.addCustomCategoryComment(DeadlyConstants.CHESTS, "The relative weights for each chest type.");
				c.addCustomCategoryComment(DeadlyConstants.DUNGEON_SPAWNERS, "The stats for dungeon spawners and the relative weights for each dungeon spawner type.");
				c.addCustomCategoryComment(DeadlyConstants.DUNGEON_TYPES, "The relative weights for each dungeon type.");
				c.addCustomCategoryComment(DeadlyConstants.DUNGEONS, "General options for dungeons and dungeon generation.");
				c.addCustomCategoryComment(DeadlyConstants.NESTS, "The stats for silverfish nest spawners and the relative weights for each silverfish nest type.");
				c.addCustomCategoryComment(DeadlyConstants.RANDOM_SPAWNERS, "The relative weights for each mob type to spawn from a random spawner.");
				c.addCustomCategoryComment(DeadlyConstants.SPAWNER_TRAPS, "The relative weights for each mob spawner trap type. Only applies to spawner traps.");
				c.addCustomCategoryComment(DeadlyConstants.SPAWNERS, "The stats for mob spawners and the relative weights for each mob spawner type. Applies only to rogue spawners and tower mob spawners.");
				c.addCustomCategoryComment(DeadlyConstants.SPAWNER_SWARMS, "The stats for swarm spawners and the relative weights for each swarm spawner type.");
				c.addCustomCategoryComment(DeadlyConstants.TOWERS, "The damage towers deal and relative weights for each tower type.");
		*/
		if (c.hasChanged()) c.save();
	}

	public static int getWeightForEntry(EntityEntry e) {
		return config.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");
	}

	public static int getPotencyForType(Potion p) {
		return config.getInt("Potency: " + p.getRegistryName().toString(), DeadlyConstants.POTION_TRAPS, 1, 0, 5, "The level of this potion.  If 0, this potion will not be used.");
	}

	public static int getDurationForType(Potion p) {
		return config.getInt("Duration: " + p.getRegistryName().toString(), DeadlyConstants.POTION_TRAPS, 200, 200, Integer.MAX_VALUE, "The duration (in ticks) of this potion.");
	}

}
