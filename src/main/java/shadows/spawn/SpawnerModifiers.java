package shadows.spawn;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SpawnerModifiers {

	public static ItemStack minDelay;
	public static ItemStack maxDelay;
	public static ItemStack spawnCount;
	public static ItemStack nearbyEntities;
	public static ItemStack playerDist;
	public static ItemStack spawnRange;
	public static ItemStack spawnConditions;
	public static ItemStack checkPlayers;
	public static ItemStack inverseItem;

	public static void init(Configuration config) {
		minDelay = readStackCfg(config.getString("Min Delay Modifier", "general", "minecraft:sugar", "The item that decreases the min delay of spawners.  5 ticks per item."));
		maxDelay = readStackCfg(config.getString("Max Delay Modifier", "general", "minecraft:clock", "The item that decreases the max delay of spawners.  5 ticks per item."));
		spawnCount = readStackCfg(config.getString("Spawn Count Modifier", "general", "minecraft:fermented_spider_eye", "The item that increases the spawn count of spawners.  1 per item."));
		nearbyEntities = readStackCfg(config.getString("Nearby Entity Modifier", "general", "minecraft:ghast_tear", "The item that increases the max nearby entities of spawners.  1 per item."));
		playerDist = readStackCfg(config.getString("Player Distance Modifier", "general", "minecraft:prismarine_crystals", "The item that increases the player activation range of spawners.  2 block radius per item."));
		spawnRange = readStackCfg(config.getString("Spawn Range Modifier", "general", "minecraft:blaze_rod", "The item that increases the spawn range of spawners.  1 block radius per item."));
		spawnConditions = readStackCfg(config.getString("Spawn Condition Modifier", "general", "minecraft:dragon_egg", "The item that disables spawn conditon checking (like light)."));
		checkPlayers = readStackCfg(config.getString("Player Check Modifier", "general", "minecraft:nether_star", "The item that disables the requirement of a nearby player."));
		inverseItem = readStackCfg(config.getString("Inverse Item", "general", "minecraft:fish:3", "When held in the off-hand, this item makes the others change stats in the opposite direction."));
		if (config.hasChanged()) config.save();
	}

	static ItemStack readStackCfg(String s) {
		String[] split = s.split(":");
		Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
		return new ItemStack(i, 1, split.length == 3 ? Integer.parseInt(split[2]) : 0);
	}

}
