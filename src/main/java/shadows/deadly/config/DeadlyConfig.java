package shadows.deadly.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
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

	public static final IntList DIM_WHITELIST = new IntArrayList();
	public static final List<PotionEffect> BRUTAL_POTIONS = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> BRUTAL_MOBS = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> BOSS_MOBS = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> SWARM_MOBS = new ArrayList<>();
	public static List<ResourceLocation> BLACKLISTED_POTIONS = new ArrayList<>();

	public static Configuration config;

	//Boss Stats
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

	//Dungeons
	public static float dungeonBrutalChance = .05F;
	public static float dungeonSwarmChance = .10F;

	public static void init() {
		Configuration c = config;
		c.load();

		DeadlyConstants.BRUTAL_SPAWNER_STATS.load(c);
		DeadlyConstants.SWARM_SPAWNER_STATS.load(c);

		String[] dims = c.getStringList("Generation Dimension Whitelist", DeadlyConstants.GENERAL, new String[] { "0" }, "The dimensions that the deadly module will generate in.");
		for (String s : dims) {
			try {
				DIM_WHITELIST.add(Integer.parseInt(s.trim()));
			} catch (NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid dim whitelist entry: " + s + ".  It will be ignored!  (Not a number)");
			}
		}

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
		String[] blacklistPotions = c.getStringList("Blacklisted Potions", DeadlyConstants.BOSSES, new String[0], "A list of potions (registry names) that bosses cannot generate with.");
		for (String s : blacklistPotions)
			BLACKLISTED_POTIONS.add(new ResourceLocation(s));

		brutalSpawnerChance = c.getFloat("Brutal Spawner Chance", DeadlyConstants.FREQUENCY, brutalSpawnerChance, 0, 1, "The chance (per chunk) for a brutal spawner to try spawning.");
		swarmSpawnerChance = c.getFloat("Swarm Spawner Chance", DeadlyConstants.FREQUENCY, swarmSpawnerChance, 0, 1, "The chance (per chunk) for a swarm spawner to try spawning.");
		bossChance = c.getFloat("Boss Chance", DeadlyConstants.FREQUENCY, bossChance, 0, 1, "The chance (per chunk) for a boss to try spawning.");

		dungeonBrutalChance = c.getFloat("Dungeon Brutal Chance", DeadlyConstants.DUNGEONS, dungeonBrutalChance, 0, 1, "The chance for a dungeon to have a brutal spawner.");
		dungeonSwarmChance = c.getFloat("Dungeon Swarm Chance", DeadlyConstants.DUNGEONS, dungeonSwarmChance, 0, 1, "The chance for a dungeon to have a swarm spawner.");

		String[] brutalFromCfg = c.getStringList("Brutal Spawner Mobs", DeadlyConstants.BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_DEFAULT_MOBS, "The possible spawn entries for brutal spawners.  Format is weight@entity, entity is a registry name.  apotheosis:random is a special name, used to generate a spawner that spawns any mob.");

		for (String s : brutalFromCfg) {
			String[] split = s.split("@");
			try {
				int weight = Integer.parseInt(split[0]);
				ResourceLocation name = new ResourceLocation(split[1]);
				EntityEntry e = ForgeRegistries.ENTITIES.getValue(name);
				if (weight > 0 && (e != null || name.equals(DeadlyConstants.RANDOM))) BRUTAL_MOBS.add(Pair.of(weight, name));
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
				if (weight > 0 && e != null) BOSS_MOBS.add(Pair.of(weight, name));
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
				if (weight > 0 && (e != null || name.equals(DeadlyConstants.RANDOM))) SWARM_MOBS.add(Pair.of(weight, name));
				else DeadlyModule.LOGGER.error("Invalid swarm spawner entry: " + s + ".  It will be ignored! (Weight <= 0 or Entity does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid swarm spawner entry: " + s + ".  It will be ignored!  (Invalid format)");
			}
		}

		String[] brutalPotions = c.getStringList("Brutal Potion Effects", DeadlyConstants.BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_POTIONS, "The potion effects applied to all brutal mobs.  Format is potion@level, potion is a registry name.");

		for (String s : brutalPotions) {
			String[] split = s.split("@");
			try {
				int level = Math.max(1, Integer.parseInt(split[1]));
				ResourceLocation name = new ResourceLocation(split[0]);
				Potion p = ForgeRegistries.POTIONS.getValue(name);
				if (p != null) BRUTAL_POTIONS.add(new PotionEffect(p, Integer.MAX_VALUE, level - 1));
				else DeadlyModule.LOGGER.error("Invalid brutal potion entry: " + s + ".  It will be ignored! (Potion does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid brutal potion entry: " + s + ".  It will be ignored! (Invalid format)");
			}
		}

		for (EntityEntry e : ForgeRegistries.ENTITIES)
			if (IMob.class.isAssignableFrom(e.getEntityClass())) config.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");

		if (c.hasChanged()) c.save();
	}

	public static int getWeightForEntry(EntityEntry e) {
		return config.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");
	}

}
