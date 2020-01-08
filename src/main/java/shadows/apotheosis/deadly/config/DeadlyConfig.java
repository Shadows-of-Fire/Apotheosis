package shadows.apotheosis.deadly.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.placebo.config.Configuration;

public class DeadlyConfig {

	public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
	public static final List<ResourceLocation> BIOME_BLACKLIST = new ArrayList<>();
	public static final List<EffectInstance> BRUTAL_POTIONS = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> BRUTAL_MOBS = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> BOSS_MOBS = new ArrayList<>();
	public static List<Pair<Integer, ResourceLocation>> SWARM_MOBS = new ArrayList<>();
	public static List<ResourceLocation> BLACKLISTED_POTIONS = new ArrayList<>();

	public static Configuration config;

	//Boss Stats
	public static RandomValueRange bossRegenLevel = RandomValueRange.of(0, 2);
	public static RandomValueRange bossResistLevel = RandomValueRange.of(0, 2);
	public static float bossFireRes = 0.5F;
	public static float bossWaterBreathing = 1.0F;
	public static RandomValueRange bossHealthMultiplier = RandomValueRange.of(2.5F, 10F);
	public static RandomValueRange bossKnockbackResist = RandomValueRange.of(0.5F, 1F);
	public static RandomValueRange bossSpeedMultiplier = RandomValueRange.of(1F, 1.5F);
	public static RandomValueRange bossDamageMult = RandomValueRange.of(1.2F, 3F);;
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

		String[] dims = c.getStringList("Generation Dimension Whitelist", DeadlyConstants.GENERAL, new String[] { "overworld" }, "The dimensions that the deadly module will generate in.");
		for (String s : dims) {
			try {
				DIM_WHITELIST.add(new ResourceLocation(s.trim()));
			} catch (ResourceLocationException e) {
				DeadlyModule.LOGGER.error("Invalid dim whitelist entry: " + s + " will be ignored");
			}
		}

		String[] biomes = c.getStringList("Generation Biome Blacklist", DeadlyConstants.GENERAL, new String[] { "minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:cold_ocean", "minecraft:frozen_ocean", "minecraft:deep_warm_ocean", "minecraft:deep_frozen_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:deep_cold_ocean", "minecraft:ocean", "minecraft:deep_ocean" }, "The biomes that the deadly module will not generate in.");
		for (String s : biomes) {
			try {
				BIOME_BLACKLIST.add(new ResourceLocation(s.trim()));
			} catch (ResourceLocationException e) {
				DeadlyModule.LOGGER.error("Invalid biome blacklist entry: " + s + " will be ignored!");
			}
		}

		bossRegenLevel = getRange(c, "Regen Level", DeadlyConstants.BOSSES, bossRegenLevel, 0, Integer.MAX_VALUE, "The %s regeneration level of bosses.");
		bossResistLevel = getRange(c, "Resistance Level", DeadlyConstants.BOSSES, bossResistLevel, 0, Integer.MAX_VALUE, "The %s resistance level of bosses.");
		bossFireRes = c.getFloat("Fire Resistance", DeadlyConstants.BOSSES, bossFireRes, 0, Float.MAX_VALUE, "The percent chance a boss has fire resistance.");
		bossWaterBreathing = c.getFloat("Water Breathing", DeadlyConstants.BOSSES, bossWaterBreathing, 0, Float.MAX_VALUE, "The percent chance a boss has water breathing.");
		bossHealthMultiplier = getRange(c, "Health Multiplier", DeadlyConstants.BOSSES, bossHealthMultiplier, 0, Integer.MAX_VALUE, "The %s amount boss health is multiplied by.  Base hp * factor = final hp.");
		bossKnockbackResist = getRange(c, "Knockback Resist", DeadlyConstants.BOSSES, bossKnockbackResist, 0, Integer.MAX_VALUE, "The %s amount of knockback resist bosses have.");
		bossSpeedMultiplier = getRange(c, "Speed Multiplier", DeadlyConstants.BOSSES, bossSpeedMultiplier, 0, Integer.MAX_VALUE, "The %s amount boss speed is multiplied by.  Base speed * factor = final speed.");
		bossDamageMult = getRange(c, "Damage Bonus", DeadlyConstants.BOSSES, bossDamageMult, 0, Integer.MAX_VALUE, "The %s amount of extra damage bosses do, in half hearts.");
		bossLevelUpChance = c.getFloat("Level Up Chance", DeadlyConstants.BOSSES, bossLevelUpChance, 0, Integer.MAX_VALUE, "The level up chance, this is rolled once per number of levels.  Levels determine gear.");
		bossEnchantChance = c.getFloat("Random Enchantment Chance", DeadlyConstants.BOSSES, bossEnchantChance, 0, Integer.MAX_VALUE, "The chance a gear piece will be randomly enchanted.");
		bossPotionChance = c.getFloat("Random Potion Chance", DeadlyConstants.BOSSES, bossPotionChance, 0, Integer.MAX_VALUE, "The chance a boss will have extra random potion effects.");
		String[] blacklistPotions = c.getStringList("Blacklisted Potions", DeadlyConstants.BOSSES, new String[] { "forbidden_arcanus:spectral_vision" }, "A list of potions (registry names) that bosses cannot generate with.");
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
				EntityType<?> e = ForgeRegistries.ENTITIES.getValue(name);
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
				EntityType<?> e = ForgeRegistries.ENTITIES.getValue(name);
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
				EntityType<?> e = ForgeRegistries.ENTITIES.getValue(name);
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
				Effect p = ForgeRegistries.POTIONS.getValue(name);
				if (p != null) BRUTAL_POTIONS.add(new EffectInstance(p, Integer.MAX_VALUE, level - 1));
				else DeadlyModule.LOGGER.error("Invalid brutal potion entry: " + s + ".  It will be ignored! (Potion does not exist)");
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid brutal potion entry: " + s + ".  It will be ignored! (Invalid format)");
			}
		}

		for (EntityType<?> e : ForgeRegistries.ENTITIES)
			if (e.getClassification() == EntityClassification.MONSTER) config.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");

		if (c.hasChanged()) c.save();
	}

	public static int getWeightForEntry(EntityType<?> e) {
		return config.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");
	}

	public static RandomValueRange getRange(Configuration c, String name, String group, RandomValueRange range, float min, float max, String comment) {
		float rMin = c.getFloat("Min " + name, group, range.getMin(), min, max, String.format(comment, "min"));
		float rMax = c.getFloat("Max " + name, group, range.getMax(), min, max, String.format(comment, "max"));
		return RandomValueRange.of(rMin, rMax);
	}

}
