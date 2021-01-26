package shadows.apotheosis.deadly.config;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.util.RandomIntRange;
import shadows.placebo.config.Configuration;

public class DeadlyConfig {

	public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
	public static final List<ResourceLocation> BIOME_BLACKLIST = new ArrayList<>();
	public static final List<EffectInstance> BRUTAL_POTIONS = new ArrayList<>();
	public static Object2IntMap<ResourceLocation> BRUTAL_MOBS = new Object2IntOpenHashMap<>();
	public static Object2IntMap<ResourceLocation> BOSS_MOBS = new Object2IntOpenHashMap<>();
	public static Object2IntMap<ResourceLocation> SWARM_MOBS = new Object2IntOpenHashMap<>();
	public static List<ResourceLocation> BLACKLISTED_POTIONS = new ArrayList<>();

	public static Configuration config;

	//Boss Stats
	public static RandomIntRange bossRegenLevel = new RandomIntRange(0, 2);
	public static RandomIntRange bossResistLevel = new RandomIntRange(0, 3);
	public static float bossFireRes = 1.0F;
	public static float bossWaterBreathing = 1.0F;
	public static RandomValueRange bossHealthMultiplier = new RandomValueRange(4F, 8F);
	public static RandomValueRange bossKnockbackResist = new RandomValueRange(0.65F, 1F);
	public static RandomValueRange bossSpeedMultiplier = new RandomValueRange(1.10F, 1.4F);
	public static RandomValueRange bossDamageMult = new RandomValueRange(2F, 4.5F);
	public static float bossEnchantChance = .45F;
	public static float bossPotionChance = .65F;
	public static int surfaceBossChance = 600;
	public static int randomAffixItem = 250;
	public static boolean surfaceBossLightning = true;
	public static int bossRarityOffset = 475;

	//Generation Chances
	public static float brutalSpawnerChance = .18F;
	public static float swarmSpawnerChance = .20F;
	public static float bossChance = .07F;

	public static Block bossFillerBlock = Blocks.RED_SANDSTONE;

	public static boolean affixTrades = true;

	public static void loadConfigs() {
		Configuration c = config;

		DeadlyConstants.BRUTAL_SPAWNER_STATS.load(c);
		DeadlyConstants.SWARM_SPAWNER_STATS.load(c);

		String[] dims = c.getStringList("Generation Dimension Whitelist", DeadlyConstants.GENERAL, new String[] { "overworld" }, "The dimensions that the deadly module will generate in.");

		DIM_WHITELIST.clear();
		for (String s : dims) {
			try {
				DIM_WHITELIST.add(new ResourceLocation(s.trim()));
			} catch (ResourceLocationException e) {
				DeadlyModule.LOGGER.error("Invalid dim whitelist entry: " + s + " will be ignored");
			}
		}

		//NOT RELOADABLE
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
		bossEnchantChance = c.getFloat("Random Enchantment Chance", DeadlyConstants.BOSSES, bossEnchantChance, 0, Integer.MAX_VALUE, "The chance a gear piece will be randomly enchanted.");
		bossPotionChance = c.getFloat("Random Potion Chance", DeadlyConstants.BOSSES, bossPotionChance, 0, Integer.MAX_VALUE, "The chance a boss will have extra random potion effects.");
		String[] blacklistPotions = c.getStringList("Blacklisted Potions", DeadlyConstants.BOSSES, new String[] { "forbidden_arcanus:spectral_vision" }, "A list of potions (registry names) that bosses cannot generate with.");
		BLACKLISTED_POTIONS.clear();
		for (String s : blacklistPotions)
			BLACKLISTED_POTIONS.add(new ResourceLocation(s));
		surfaceBossChance = c.getInt("Surface Boss Chance", DeadlyConstants.BOSSES, surfaceBossChance, 1, 500000, "The 1/n chance that a naturally spawned mob that can see the sky is transformed into a boss.");
		randomAffixItem = c.getInt("Random Affix Chance", DeadlyConstants.AFFIXES, randomAffixItem, 1, 500000, "The 1/n chance that a naturally spawned mob will be granted an affix item.");
		surfaceBossLightning = c.getBoolean("Surface Boss Lightning", DeadlyConstants.BOSSES, true, "If a lightning bolt strikes when a surface boss spawn occurs.");
		bossRarityOffset = c.getInt("Boss Rarity Offset", DeadlyConstants.BOSSES, bossRarityOffset, 0, 999, "The rarity offset for boss item generation.  400 guarantees uncommon, 700 guarantees rare, 800 guarantees epic, 950 guarantees mythic.");

		brutalSpawnerChance = c.getFloat("Brutal Spawner Chance", DeadlyConstants.FREQUENCY, brutalSpawnerChance, 0, 1, "The chance (per chunk) for a brutal spawner to try spawning.");
		swarmSpawnerChance = c.getFloat("Swarm Spawner Chance", DeadlyConstants.FREQUENCY, swarmSpawnerChance, 0, 1, "The chance (per chunk) for a swarm spawner to try spawning.");
		bossChance = c.getFloat("Boss Chance", DeadlyConstants.FREQUENCY, bossChance, 0, 1, "The chance (per chunk) for a boss to try spawning.");

		ResourceLocation blockId = new ResourceLocation(c.getString("Boss Filler Block", DeadlyConstants.BOSSES, bossFillerBlock.getRegistryName().toString(), "The block that spawns in a 5x5 underneath world-generated bosses."));
		bossFillerBlock = ForgeRegistries.BLOCKS.getValue(blockId);
		if (bossFillerBlock == Blocks.AIR) {
			DeadlyModule.LOGGER.error("Boss Filler Block {} was mapped to air, it will be reverted to red sandstone.", blockId);
			bossFillerBlock = Blocks.RED_SANDSTONE;
		}

		String[] brutalFromCfg = c.getStringList("Brutal Spawner Mobs", DeadlyConstants.BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_DEFAULT_MOBS, "The possible spawn entries for brutal spawners.  Format is weight@entity, entity is a registry name.  apotheosis:random is a special name, used to generate a spawner that spawns any mob.");
		loadEntitiesFromConfig(brutalFromCfg, BRUTAL_MOBS, "Brutal Spawner");

		String[] bossFromCfg = c.getStringList("Boss Spawner Mobs", DeadlyConstants.BOSSES, DeadlyConstants.BOSS_DEFAULT_MOBS, "The possible mob types for bosses.  Format is weight@entity, entity is a registry name.");
		loadEntitiesFromConfig(bossFromCfg, BOSS_MOBS, "Boss");

		String[] swarmFromCfg = c.getStringList("Swarm Spawner Mobs", DeadlyConstants.SWARM_SPAWNERS, DeadlyConstants.SWARM_DEFAULT_MOBS, "The possible spawn entries for swarm spawners.  Format is weight@entity, entity is a registry name.");
		loadEntitiesFromConfig(swarmFromCfg, SWARM_MOBS, "Swarm Spawner");

		String[] brutalPotions = c.getStringList("Brutal Potion Effects", DeadlyConstants.BRUTAL_SPAWNERS, DeadlyConstants.BRUTAL_POTIONS, "The potion effects applied to all brutal mobs.  Format is potion@level, potion is a registry name.");

		BRUTAL_POTIONS.clear();
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
			if (e.getClassification() == EntityClassification.MONSTER) c.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");

		affixTrades = c.getBoolean("Affix Trades", "wanderer", true, "If the wandering trader may sell affix loot items as a rare trade.");
	}

	public static int getWeightForEntry(EntityType<?> e) {
		return config.getInt(e.getRegistryName().toString(), DeadlyConstants.RANDOM_SPAWNERS, e.getRegistryName().getNamespace().equals("minecraft") ? 8 : 1, 0, 50, "");
	}

	public static RandomValueRange getRange(Configuration c, String name, String group, RandomValueRange range, float min, float max, String comment) {
		float rMin = c.getFloat("Min " + name, group, range.getMin(), min, max, String.format(comment, "min"));
		float rMax = c.getFloat("Max " + name, group, range.getMax(), min, max, String.format(comment, "max"));
		return RandomValueRange.of(rMin, rMax);
	}

	public static RandomIntRange getRange(Configuration c, String name, String group, RandomIntRange range, int min, int max, String comment) {
		int rMin = c.getInt("Min " + name, group, range.getMin(), min, max, String.format(comment, "min"));
		int rMax = c.getInt("Max " + name, group, range.getMax(), min, max, String.format(comment, "max"));
		return new RandomIntRange(rMin, rMax);
	}

	private static void loadEntitiesFromConfig(String[] entities, Object2IntMap<ResourceLocation> map, String type) {
		map.clear();
		for (String s : entities) {
			String[] split = s.split("@");
			try {
				int weight = Integer.parseInt(split[0]);
				ResourceLocation name = new ResourceLocation(split[1]);
				EntityType<?> e = ForgeRegistries.ENTITIES.getValue(name);
				if (weight > 0 && (e != null || name.equals(DeadlyConstants.RANDOM))) map.put(name, weight);
				else DeadlyModule.LOGGER.error("Invalid {} entry: {}.  It will be ignored! (Weight <= 0 or Entity does not exist)", type, s);
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				DeadlyModule.LOGGER.error("Invalid {} entry: {}.  It will be ignored!  (Invalid format)", type, s);
			}
		}
	}

}