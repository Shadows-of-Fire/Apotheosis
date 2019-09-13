package shadows.apotheosis.spawn.compat;
/*
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import shadows.placebo.Placebo;
import shadows.placebo.interfaces.IPostInitUpdate;
import shadows.spawn.SpawnerModifiers;
import shadows.spawn.modifiers.SpawnerModifier;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.apotheosis.SpawnerModifiers")
@ZenRegister
public class ModifierTweaker {

	@ZenMethod
	public static void setCap(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.CAP, input));
	}

	@ZenMethod
	public static void setConditions(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.CONDITIONS, input));
	}

	@ZenMethod
	public static void setMaxDelay(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.MAX_DELAY, input));
	}

	@ZenMethod
	public static void setMinDelay(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.MIN_DELAY, input));
	}

	@ZenMethod
	public static void setNearbyEntities(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.NEARBY_ENTITIES, input));
	}

	@ZenMethod
	public static void setPlayerDistance(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.PLAYER_DISTANCE, input));
	}

	@ZenMethod
	public static void setPlayers(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.PLAYERS, input));
	}

	@ZenMethod
	public static void setRedstone(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.REDSTONE, input));
	}

	@ZenMethod
	public static void setSpawnCount(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.SPAWN_COUNT, input));
	}

	@ZenMethod
	public static void setSpawnRange(IIngredient input) {
		Placebo.UPDATES.add(getUpdate(SpawnerModifiers.SPAWN_RANGE, input));
	}

	@ZenMethod
	public static void setInverse(IIngredient input) {
		Placebo.UPDATES.add(e -> CraftTweakerAPI.apply(new InverseAction(input)));
	}

	private static IPostInitUpdate getUpdate(SpawnerModifier modifier, IIngredient item) {
		return e -> CraftTweakerAPI.apply(new ModifierAction(modifier, item));
	}

	private static class ModifierAction implements IAction {

		SpawnerModifier modifier;
		IIngredient item;

		ModifierAction(SpawnerModifier modifier, IIngredient item) {
			this.modifier = modifier;
			this.item = item;
		}

		@Override
		public void apply() {
			modifier.setIngredient(CraftTweakerMC.getIngredient(item));
		}

		@Override
		public String describe() {
			return String.format("Set the modifier item for %s to %s", modifier.getCategory(), item.toCommandString());
		}

	}

	private static class InverseAction implements IAction {

		IIngredient item;

		InverseAction(IIngredient item) {
			this.item = item;
		}

		@Override
		public void apply() {
			SpawnerModifiers.inverseItem = CraftTweakerMC.getIngredient(item);
		}

		@Override
		public String describe() {
			return String.format("Set the spawner modification inverse item to %s", item.toCommandString());
		}

	}
}
*/