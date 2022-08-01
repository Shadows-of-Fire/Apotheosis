package shadows.apotheosis.adventure;

import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.config.Configuration;

public class AdventureConfig {

	//public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
	//public static final List<ResourceLocation> BIOME_BLACKLIST = new ArrayList<>();
	//public static final Map<ResourceLocation, EquipmentType> TYPE_OVERRIDES = new HashMap<>();

	//Boss Stats
	//public static int surfaceBossChance = 85;
	//public static int randomAffixItem = 125;
	//public static boolean surfaceBossLightning = true;
	//public static boolean curseBossItems = false;

	//Generation Chances
	//public static int bossDungeonAttempts = 8;
	//public static int bossDungeon2Attempts = 8;
	//public static int rogueSpawnerAttempts = 4;
	//public static int troveAttempts = 8;
	//public static int tomeTowerChance = 125;

	//public static boolean affixTrades = true;
	//public static boolean mythicUnbreakable = true;

	//public static int spawnerValueChance = 9;

	public static int[] rarityThresholds = new int[] { 400, 700, 880, 950, 995 };

	public static void load(Configuration c) {
		int i = 0;
		for (LootRarity r : LootRarity.values()) {
			if (r != LootRarity.ANCIENT) {
				int threshold = c.getInt(r.id(), "rarity", rarityThresholds[i], 0, 1000, "The threshold for this rarity.  The percentage chance of this rarity appearing is equal to (previous threshold - this threshold) / 10.");
				rarityThresholds[i++] = threshold;
			}
		}
	}

}