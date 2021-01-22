package shadows.apotheosis.spawn;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.spawn.modifiers.CapModifier;
import shadows.apotheosis.spawn.modifiers.ConditionModifier;
import shadows.apotheosis.spawn.modifiers.EggModifier;
import shadows.apotheosis.spawn.modifiers.InverseModifier;
import shadows.apotheosis.spawn.modifiers.MaxDelayModifier;
import shadows.apotheosis.spawn.modifiers.MinDelayModifier;
import shadows.apotheosis.spawn.modifiers.ModifierSync.ModifierRecipe;
import shadows.apotheosis.spawn.modifiers.NearbyEntityModifier;
import shadows.apotheosis.spawn.modifiers.PlayerDistModifier;
import shadows.apotheosis.spawn.modifiers.PlayerModifier;
import shadows.apotheosis.spawn.modifiers.RedstoneModifier;
import shadows.apotheosis.spawn.modifiers.SpawnCountModifier;
import shadows.apotheosis.spawn.modifiers.SpawnRangeModifier;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.placebo.config.Configuration;
import shadows.placebo.recipe.RecipeHelper;

public class SpawnerModifiers {

	public static final Map<String, SpawnerModifier> MODIFIERS = new HashMap<>();
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
	public static final SpawnerModifier INVERSE = new InverseModifier();

	public static void registerModifiers() {
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
		register(INVERSE);
		for (SpawnerModifier modif : MODIFIERS.values()) {
			RecipeHelper.addRecipe(new ModifierRecipe(modif));
		}
	}

	public static void reload(Configuration config) {
		for (SpawnerModifier modif : MODIFIERS.values())
			modif.load(config);
	}

	public static LazyValue<Ingredient> readIngredient(String s) {
		if (s.startsWith("#")) {
			String tag = s.substring(1);
			return new LazyValue<>(() -> Ingredient.fromTag(TagCollectionManager.getManager().getItemTags().get(new ResourceLocation(tag))));
		} else {
			String[] split = s.split(":");
			Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
			ItemStack stack = new ItemStack(i);
			if (i == Items.BARRIER) stack.setDisplayName(new TranslationTextComponent("info.apoth.modifier_disabled"));
			return new LazyValue<>(() -> Ingredient.fromStacks(stack));
		}
	}

	public static void register(SpawnerModifier modif) {
		if (!MODIFIERS.containsKey(modif.getId())) {
			MODIFIERS.put(modif.getId(), modif);
		} else throw new RuntimeException("Tried to register a spawner modifier, but it is already registered!");
	}

}