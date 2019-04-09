package shadows.spawn;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.spawn.modifiers.CapModifier;
import shadows.spawn.modifiers.ConditionModifier;
import shadows.spawn.modifiers.EggModifier;
import shadows.spawn.modifiers.MaxDelayModifier;
import shadows.spawn.modifiers.MinDelayModifier;
import shadows.spawn.modifiers.NearbyEntityModifier;
import shadows.spawn.modifiers.PlayerDistModifier;
import shadows.spawn.modifiers.PlayerModifier;
import shadows.spawn.modifiers.RedstoneModifier;
import shadows.spawn.modifiers.SpawnCountModifier;
import shadows.spawn.modifiers.SpawnRangeModifier;
import shadows.spawn.modifiers.SpawnerModifier;

public class SpawnerModifiers {

	public static final List<SpawnerModifier> MODIFIERS = new ArrayList<>();
	public static final SpawnerModifier MIN_DELAY = new MinDelayModifier();
	public static final SpawnerModifier MAX_DELAY = new MaxDelayModifier();
	public static final SpawnerModifier SPAWN_COUNT = new SpawnCountModifier();
	public static final SpawnerModifier NEARBY_ENTITIES = new NearbyEntityModifier();
	public static final SpawnerModifier PLAYER_DISTANCE = new PlayerDistModifier();
	public static final SpawnerModifier SPAWN_RANGE = new SpawnRangeModifier();
	public static final SpawnerModifier CONDITIONS = new ConditionModifier();
	public static final SpawnerModifier PLAYERS = new PlayerModifier();
	public static final SpawnerModifier CAP = new CapModifier();
	public static final SpawnerModifier REDSTONE = new RedstoneModifier();
	public static final SpawnerModifier EGG = new EggModifier();

	public static Ingredient inverseItem;

	public static void init() {
		register(MIN_DELAY);
		register(MAX_DELAY);
		register(SPAWN_COUNT);
		register(NEARBY_ENTITIES);
		register(PLAYER_DISTANCE);
		register(SPAWN_RANGE);
		register(CONDITIONS);
		register(PLAYERS);
		register(CAP);
		register(REDSTONE);
		register(EGG);

		inverseItem = readStackCfg(SpawnerModule.config.getString("Inverse Item", "general", "minecraft:quartz", "When held in the off-hand, this item makes modifiers change stats in the opposite direction."));
	}

	public static Ingredient readStackCfg(String s) {
		String[] split = s.split(":");
		Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
		return Ingredient.fromStacks(new ItemStack(i, 1, split.length == 3 ? Integer.parseInt(split[2]) : 0));
	}

	public static void register(SpawnerModifier modif) {
		if (!MODIFIERS.contains(modif)) {
			MODIFIERS.add(modif);
			modif.load(SpawnerModule.config);
		} else throw new RuntimeException("Tried to register a spawner modifier, but it is already registered!");
	}

}
