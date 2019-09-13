package shadows.apotheosis.spawn.modifiers;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.TileSpawnerExt;
import shadows.placebo.config.Configuration;

/**
 * Parent class for all spawner modifiers.
 * @author Shadows
 *
 */
public abstract class SpawnerModifier {

	public static final String ITEM = "item";
	public static final String VALUE = "value";
	public static final String MIN = "min_value";
	public static final String MAX = "max_value";

	/**
	 * The matching item for this modifier.
	 */
	protected Ingredient item;

	/**
	 * The amount this modifier changes it's respective stat.
	 */
	protected int value;

	/**
	 * The in int value of this modifier value.
	 */
	protected int min;

	/**
	 * The max int value of this modifier value.
	 */
	protected int max;

	public SpawnerModifier(Ingredient item, int value) {
		this.item = item;
		this.value = value;
	}

	public SpawnerModifier(ItemStack item, int value) {
		this(Ingredient.fromStacks(item), value);
	}

	/**
	 * Checks if this modifier can be applied.  Should check matching item and if the spawner is not at capacity.
	 * @param spawner The spawner you are modifying.
	 * @param stack The mainhand stack of the player.
	 * @param inverting If the player is holding the inverse item in their offhand.
	 * @return If this modifier can act, given the conditions.
	 */
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return item.test(stack);
	}

	/**
	 * Applies this modifier.
	 * @param spawner The spawner you are modifying.
	 * @param stack The mainhand stack of the player.
	 * @param inverting If the player is holding the inverse item in their offhand.
	 * @return The value to be returned to {@link Block#onBlockActivated}
	 */
	public abstract boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting);

	/**
	 * Reads this modifier from config.  Should update all relevant values.
	 */
	public void load(Configuration cfg) {
		String s = cfg.getString(ITEM, getCategory(), getDefaultItem(), "The item that applies this modifier.");
		item = SpawnerModifiers.readStackCfg(s);
		value = cfg.getInt(VALUE, getCategory(), value, Integer.MIN_VALUE, Integer.MAX_VALUE, "The amount each item changes this stat.");
		min = cfg.getInt(MIN, getCategory(), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "The min value of this stat.");
		max = cfg.getInt(MAX, getCategory(), Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, "The max value of this stat.");
	}

	public Ingredient getIngredient() {
		return item;
	}

	public int getValue() {
		return value;
	}

	public void setIngredient(Ingredient item) {
		this.item = item;
	}

	public abstract String getCategory();

	public abstract String getDefaultItem();
}
