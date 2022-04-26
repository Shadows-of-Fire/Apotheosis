package shadows.apotheosis.deadly.config;

import java.util.*;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.placebo.config.Configuration;

public class DeadlyConfig {

	public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
	public static final List<ResourceLocation> BIOME_BLACKLIST = new ArrayList<>();
//	public static final Map<ResourceLocation, EquipmentType> TYPE_OVERRIDES = new HashMap<>();

	public static Configuration config;

	//Boss Stats
	public static int surfaceBossChance = 85;
	public static int randomAffixItem = 125;
	public static boolean surfaceBossLightning = true;
	public static boolean curseBossItems = false;

	//Generation Chances
	public static int bossDungeonAttempts = 8;
	public static int bossDungeon2Attempts = 8;
	public static int rogueSpawnerAttempts = 4;
	public static int troveAttempts = 8;
	public static int tomeTowerChance = 125;

	public static boolean affixTrades = true;
	public static boolean mythicUnbreakable = true;

	public static int spawnerValueChance = 9;

	public static int[] rarityThresholds = new int[] { 400, 700, 880, 950, 995 };

	//pc3k: now configurable via worldgen/biome tag HAS_STRUCTURE/XXX
//	public static boolean canGenerateIn(WorldGenLevel world) {
//		ResourceKey<Level> key = world.getLevel().dimension();
//		return DIM_WHITELIST.contains(key.location());
//	}
	public static void load(Configuration c) {

		String[] dims = c.getStringList("Generation Dimension Whitelist", "general", new String[] { "overworld" }, "The dimensions that the deadly module will generate in.");

		DIM_WHITELIST.clear();
		for (String s : dims) {
			try {
				DIM_WHITELIST.add(new ResourceLocation(s.trim()));
			} catch (ResourceLocationException e) {
				DeadlyModule.LOGGER.error("Invalid dim whitelist entry: " + s + " will be ignored");
			}
		}

		BIOME_BLACKLIST.clear();
		String[] biomes = c.getStringList("Generation Biome Blacklist", "general", new String[] { "minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:cold_ocean", "minecraft:frozen_ocean", "minecraft:deep_warm_ocean", "minecraft:deep_frozen_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:deep_cold_ocean", "minecraft:ocean", "minecraft:deep_ocean" }, "The biomes that the deadly module will not generate in.");
		for (String s : biomes) {
			try {
				BIOME_BLACKLIST.add(new ResourceLocation(s.trim()));
			} catch (ResourceLocationException e) {
				DeadlyModule.LOGGER.error("Invalid biome blacklist entry: " + s + " will be ignored!");
			}
		}

		surfaceBossChance = c.getInt("Surface Boss Chance", "bosses", surfaceBossChance, 0, 500000, "The 1/n chance that a naturally spawned mob that can see the sky is transformed into a boss.  0 to disable.");
		randomAffixItem = c.getInt("Random Affix Chance", "affixes", randomAffixItem, 0, 500000, "The 1/n chance that a naturally spawned mob will be granted an affix item. 0 to disable.");
		surfaceBossLightning = c.getBoolean("Surface Boss Lightning", "bosses", true, "If a lightning bolt strikes when a surface boss spawn occurs.");
		curseBossItems = c.getBoolean("Curse Boss Items", "bosses", false, "If boss items are always cursed.  Enable this if you want bosses to be less overpowered by always giving them a negative effect.");

		bossDungeonAttempts = c.getInt("Boss Dungeon", "frequency", bossDungeonAttempts, 0, 50000, "The number of generation attempts (per chunk) for boss dungeons.");
		bossDungeon2Attempts = c.getInt("Boss Dungeon Variant 2", "frequency", bossDungeon2Attempts, 0, 50000, "The number of generation attempts (per chunk) for boss dungeon variant 2.");
		rogueSpawnerAttempts = c.getInt("Rogue Spawners", "frequency", rogueSpawnerAttempts, 0, 50000, "The number of generation attempts (per chunk) for rogue spawners.");
		troveAttempts = c.getInt("Ore Troves", "frequency", troveAttempts, 0, 50000, "The number of generation attempts (per chunk) for ore troves.");
		tomeTowerChance = c.getInt("Tome Tower", "frequency", tomeTowerChance, 0, 50000, "The 1/n chance (per chunk) that a tome tower may attempt generation. 0 = disabled, lower = more chances.");

		affixTrades = c.getBoolean("Affix Trades", "wanderer", true, "If the wandering trader may sell affix loot items as a rare trade.");
		mythicUnbreakable = c.getBoolean("Mythic Unbreakable", "affixes", mythicUnbreakable, "If mythic items are unbreakable.");

		spawnerValueChance = c.getInt("Spawner Rare Loot Chance", "general", spawnerValueChance, 0, 80000, "The 1/n chance that a rogue spawner will generate with a CHEST_VALUABLE instead of it's default chest.  0 to disable.");

		int i = 0;
		for (LootRarity r : LootRarity.values()) {
			if (r != LootRarity.ANCIENT) {
				int threshold = c.getInt(r.name().toLowerCase(Locale.ROOT), "rarity", rarityThresholds[i], 0, 1000, "The threshold for this rarity.  The percentage chance of this rarity appearing is equal to (previous threshold - this threshold) / 10.");
				rarityThresholds[i++] = threshold;
			}
		}
	}

}