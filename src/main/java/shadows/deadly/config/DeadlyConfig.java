package shadows.deadly.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.deadly.DeadlyModule;

/**
 * This class is functional.
 * @author Shadows
 *
 */
public class DeadlyConfig {

	public static Configuration config;

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
	public static float bossSpeedMultiplier = 1.15F;
	public static float bossDamageBonus = 4;
	public static float bossLevelUpChance = .25F;
	public static float bossEnchantChance = .25F;
	public static float bossPotionChance = .45F;

	//Generation Chances
	public static float brutalSpawnerChance = .12F;
	public static float swarmSpawnerChance = .15F;
	public static float bossChance = .08F;

	//Weighted entries
	public static List<Pair<Integer, ResourceLocation>> brutalWeightedMobs = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> bossWeightedMobs = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> swarmWeightedMobs = new ArrayList<>();

	public static void init() {
		Configuration c = config;
		c.load();

		DeadlyConstants.BRUTAL_SPAWNER_STATS.load(c);
		DeadlyConstants.SWARM_SPAWNER_STATS.load(c);

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

		String[] brutalFromCfg = c.getStringList("Brutal Spawner Mobs", DeadlyConstants.BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_DEFAULT_MOBS, "The possible spawn entries for brutal spawners.  Format is weight@entity, entity is a registry name.  apotheosis:random is a special name, used to generate a spawner that spawns any mob.");

		for (String s : brutalFromCfg) {
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
		if (c.hasChanged()) c.save();
	}

	public static int getWeightForEntry(EntityEntry e) {
		return config.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");
	}

}
