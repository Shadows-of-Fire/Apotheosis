package shadows.apotheosis.deadly.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.placebo.config.Configuration;

public class DeadlyConfig {

	public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
	public static final List<ResourceLocation> BIOME_BLACKLIST = new ArrayList<>();

	public static Configuration config;

	//Boss Stats
	public static int surfaceBossChance = 600;
	public static int randomAffixItem = 250;
	public static boolean surfaceBossLightning = true;
	public static int bossRarityOffset = 475;

	//Generation Chances
	public static int bossDungeonAttempts = 8;
	public static int bossDungeon2Attempts = 8;
	public static int rogueSpawnerAttempts = 4;
	public static int troveAttempts = 8;
	public static int tomeTowerChance = 50;

	public static boolean affixTrades = true;

	public static int spawnerValueChance = 9;

	public static void loadConfigs() {
		Configuration c = config;
		String[] dims = c.getStringList("Generation Dimension Whitelist", "general", new String[] { "overworld" }, "The dimensions that the deadly module will generate in.");

		DIM_WHITELIST.clear();
		for (String s : dims) {
			try {
				DIM_WHITELIST.add(new ResourceLocation(s.trim()));
			} catch (ResourceLocationException e) {
				DeadlyModule.LOGGER.error("Invalid dim whitelist entry: " + s + " will be ignored");
			}
		}

		//NOT RELOADABLE (why?)
		String[] biomes = c.getStringList("Generation Biome Blacklist", "general", new String[] { "minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:cold_ocean", "minecraft:frozen_ocean", "minecraft:deep_warm_ocean", "minecraft:deep_frozen_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:deep_cold_ocean", "minecraft:ocean", "minecraft:deep_ocean" }, "The biomes that the deadly module will not generate in.");
		for (String s : biomes) {
			try {
				BIOME_BLACKLIST.add(new ResourceLocation(s.trim()));
			} catch (ResourceLocationException e) {
				DeadlyModule.LOGGER.error("Invalid biome blacklist entry: " + s + " will be ignored!");
			}
		}
		surfaceBossChance = c.getInt("Surface Boss Chance", "bosses", surfaceBossChance, 1, 500000, "The 1/n chance that a naturally spawned mob that can see the sky is transformed into a boss.");
		randomAffixItem = c.getInt("Random Affix Chance", "affixes", randomAffixItem, 1, 500000, "The 1/n chance that a naturally spawned mob will be granted an affix item.");
		surfaceBossLightning = c.getBoolean("Surface Boss Lightning", "bosses", true, "If a lightning bolt strikes when a surface boss spawn occurs.");
		bossRarityOffset = c.getInt("Boss Rarity Offset", "bosses", bossRarityOffset, 0, 999, "The rarity offset for boss item generation.  400 guarantees uncommon, 700 guarantees rare, 800 guarantees epic, 950 guarantees mythic.");

		bossDungeonAttempts = c.getInt("Boss Dungeon", "frequency", bossDungeonAttempts, 0, 50000, "The number of generation attempts (per chunk) for boss dungeons.");
		bossDungeon2Attempts = c.getInt("Boss Dungeon Variant 2", "frequency", bossDungeon2Attempts, 0, 50000, "The number of generation attempts (per chunk) for boss dungeon variant 2.");
		rogueSpawnerAttempts = c.getInt("Rogue Spawners", "frequency", rogueSpawnerAttempts, 0, 50000, "The number of generation attempts (per chunk) for rogue spawners.");
		troveAttempts = c.getInt("Ore Troves", "frequency", troveAttempts, 0, 50000, "The number of generation attempts (per chunk) for ore troves.");
		tomeTowerChance = c.getInt("Tome Tower", "frequency", tomeTowerChance, 0, 50000, "The 1/n chance (per chunk) that a tome tower may attempt generation. 0 = disabled, lower = more chances.");

		affixTrades = c.getBoolean("Affix Trades", "wanderer", true, "If the wandering trader may sell affix loot items as a rare trade.");

		spawnerValueChance = c.getInt("Spawner Rare Loot Chance", "general", spawnerValueChance, 0, 80000, "The 1/n chance that a rogue spawner will generate with a CHEST_VALUABLE instead of it's default chest.  0 to disable.");
	}

	public static boolean canGenerateIn(ISeedReader world) {
		RegistryKey<World> key = world.getWorld().getDimensionKey();
		return DIM_WHITELIST.contains(key.getLocation());
	}
}