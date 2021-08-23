package shadows.apotheosis.deadly.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.LootRarity;
import shadows.placebo.config.Configuration;

public class DeadlyConfig {

	public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
	public static final List<ResourceLocation> BIOME_BLACKLIST = new ArrayList<>();
	public static final Map<ResourceLocation, EquipmentType> TYPE_OVERRIDES = new HashMap<>();

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

	public static int spawnerValueChance = 9;

	public static int[] rarityThresholds = new int[] { 400, 700, 880, 950, 1000 };

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

		spawnerValueChance = c.getInt("Spawner Rare Loot Chance", "general", spawnerValueChance, 0, 80000, "The 1/n chance that a rogue spawner will generate with a CHEST_VALUABLE instead of it's default chest.  0 to disable.");

		TYPE_OVERRIDES.clear();
		String[] overrides = c.getStringList("Equipment Type Overrides", "affixes", new String[] { "minecraft:stick|SWORD" }, "A list of type overrides for the affix loot system.  Format is <itemname>|<type>.  Types are SWORD, RANGED, PICKAXE, SHOVEL, AXE, SHIELD");
		for (String s : overrides) {
			String[] split = s.split("\\|");
			try {
				EquipmentType type = EquipmentType.valueOf(split[1]);
				if (type == EquipmentType.ARMOR) throw new UnsupportedOperationException("Cannot override an item to type ARMOR!");
				TYPE_OVERRIDES.put(new ResourceLocation(split[0]), type);
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Invalid type override entry: " + s + " will be ignored!");
				e.printStackTrace();
			}
		}

		int i = 0;
		for (LootRarity r : LootRarity.values()) {
			if (r != LootRarity.ANCIENT) {
				int threshold = c.getInt(r.name().toLowerCase(Locale.ROOT), "rarity", rarityThresholds[i], 0, 1000, "The threshold for this rarity.  The percentage chance of this rarity appearing is equal to (previous threshold - this threshold) / 10.");
				rarityThresholds[i++] = threshold;
			}
		}
	}

	public static boolean canGenerateIn(ISeedReader world) {
		RegistryKey<World> key = world.getLevel().dimension();
		return DIM_WHITELIST.contains(key.location());
	}
}