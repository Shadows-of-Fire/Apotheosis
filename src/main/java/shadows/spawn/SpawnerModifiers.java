package shadows.spawn;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.placebo.util.VanillaPacketDispatcher;

public class SpawnerModifiers {

	public static final List<SpawnerModifier> MODIFIERS = new ArrayList<>();
	public static final SpawnerModifier ENTITY = new SpawnerModifier(new ItemStack(Items.SPAWN_EGG), (a, b) -> a.potentialSpawns.clear()) {
		public boolean returnVal() {
			return false;
		}
	};

	public static SpawnerModifier minDelay;
	public static SpawnerModifier maxDelay;
	public static SpawnerModifier spawnCount;
	public static SpawnerModifier nearbyEntities;
	public static SpawnerModifier playerDist;
	public static SpawnerModifier spawnRange;
	public static SpawnerModifier spawnConditions;
	public static SpawnerModifier checkPlayers;
	public static Ingredient inverseItem;
	public static SpawnerModifier ignoreCap;
	public static SpawnerModifier redstone;

	//Formatter::off
	public static void init(Configuration config) {
		minDelay = new SpawnerModifier(
				readStackCfg(config.getString("Min Delay Modifier", "general", "minecraft:sugar", "The item that decreases the min delay of spawners.  5 ticks per item.")),
				(a, inv) -> a.minSpawnDelay = Math.max(0, a.minSpawnDelay + (!inv ? -5 : 5)));
		maxDelay = new SpawnerModifier(
				readStackCfg(config.getString("Max Delay Modifier", "general", "minecraft:clock", "The item that decreases the max delay of spawners.  5 ticks per item.")),
				(a, inv) -> a.maxSpawnDelay = Math.max(Math.max(10, a.minSpawnDelay), a.maxSpawnDelay + (!inv ? -5 : 5)));
		spawnCount = new SpawnerModifier(
				readStackCfg(config.getString("Spawn Count Modifier", "general", "minecraft:fermented_spider_eye", "The item that increases the spawn count of spawners.  1 per item.")),
				(a, inv) -> a.spawnCount = Math.max(0, a.spawnCount + (!inv ? 1 : -1)));
		nearbyEntities = new SpawnerModifier(
				readStackCfg(config.getString("Nearby Entity Modifier", "general", "minecraft:ghast_tear", "The item that increases the max nearby entities of spawners.  3 per item.")),
				(a, inv) -> a.maxNearbyEntities = Math.max(0, a.maxNearbyEntities + (!inv ? 3 : -3)));
		playerDist = new SpawnerModifier(
				readStackCfg(config.getString("Player Distance Modifier", "general", "minecraft:prismarine_crystals", "The item that increases the player activation range of spawners.  2 block radius per item.")),
				(a, inv) -> a.activatingRangeFromPlayer = Math.max(0, a.activatingRangeFromPlayer + (!inv ? 2 : -2)));
		spawnRange = new SpawnerModifier(
				readStackCfg(config.getString("Spawn Range Modifier", "general", "minecraft:blaze_rod", "The item that increases the spawn range of spawners.  1 block radius per item.")),
				(a, inv) -> a.spawnRange = Math.max(0, a.spawnRange + (!inv ? 1 : -1)));
		spawnConditions = new SpawnerModifier(
				readStackCfg(config.getString("Spawn Condition Modifier", "general", "minecraft:dragon_egg", "The item that disables spawn conditon checking (like light).")),
				(a, b, inv) -> a.ignoresConditions = !inv);
		checkPlayers = new SpawnerModifier(
				readStackCfg(config.getString("Player Check Modifier", "general", "minecraft:nether_star", "The item that disables the requirement of a nearby player.")),
				(a, b, inv) -> a.ignoresPlayers = !inv);
		ignoreCap = new SpawnerModifier(
				readStackCfg(config.getString("Entity Cap Modifier", "general", "minecraft:chorus_fruit", "The item that disables the nearby entity cap.")),
				(a, b, inv) -> a.ignoresCap = !inv);
		redstone = new SpawnerModifier(
				readStackCfg(config.getString("Redstone Modifier", "general", "minecraft:comparator", "The item that enables redstone control.  Signal = off")),
				(a, b, inv) -> {a.redstoneEnabled = !inv; VanillaPacketDispatcher.dispatchTEToNearbyPlayers(a);});

		inverseItem = Ingredient.fromStacks(readStackCfg(config.getString("Inverse Item", "general", "minecraft:quartz", "When held in the off-hand, this item makes the others change stats in the opposite direction.")));		
		if (config.hasChanged()) config.save();
	}
	//Formatter::on

	static ItemStack readStackCfg(String s) {
		String[] split = s.split(":");
		Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
		return new ItemStack(i, 1, split.length == 3 ? Integer.parseInt(split[2]) : 0);
	}

}
