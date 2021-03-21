package shadows.apotheosis.spawn.modifiers;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
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
	protected LazyValue<Ingredient> item;

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

	/**
	 * @param item
	 * @param value
	 * @param min
	 * @param max
	 */
	public SpawnerModifier(int value, int min, int max) {
		this.value = value;
		this.min = min;
		this.max = max;
	}

	public SpawnerModifier() {
		this(-1, -1, -1);
	}

	/**
	 * Checks if this modifier can be applied.  Should check matching item and if the spawner is not at capacity.
	 * @param spawner The spawner you are modifying.
	 * @param stack The mainhand stack of the player.
	 * @param inverting If the player is holding the inverse item in their offhand.
	 * @return If this modifier can act, given the conditions.
	 */
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return this.item.getValue().test(stack);
	}

	/**
	 * Applies this modifier.
	 * @param spawner The spawner you are modifying.
	 * @param stack The mainhand stack of the player.
	 * @param inverting If the player is holding the inverse item in their offhand.
	 * @return The value to be returned to {@link Block#onBlockActivated}
	 */
	public abstract boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting);

	/**
	 * Reads this modifier from config.  Should update all relevant values.
	 */
	public void load(Configuration cfg) {
		String s = cfg.getString(ITEM, this.getId(), this.getDefaultItem(), "The item that applies this modifier.");
		this.item = SpawnerModifiers.readIngredient(s);
		if (this.value != -1) this.value = cfg.getInt(VALUE, this.getId(), this.value, Integer.MIN_VALUE, Integer.MAX_VALUE, "The amount each item changes this stat.");
		if (this.min != -1) this.min = cfg.getInt(MIN, this.getId(), this.min, Integer.MIN_VALUE, Integer.MAX_VALUE, "The min value of this stat.");
		if (this.max != -1) this.max = cfg.getInt(MAX, this.getId(), this.max, Integer.MIN_VALUE, Integer.MAX_VALUE, "The max value of this stat.");
	}

	public Ingredient getIngredient() {
		return this.item.getValue();
	}

	public int getValue() {
		return this.value;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	public abstract String getId();

	public abstract String getDefaultItem();

	/**
	 * Updates modifier data.
	 * Used on the client during the receipt of modifiers from the server.
	 */
	public void sync(Ingredient ing, int value, int min, int max) {
		this.item = new LazyValue<>(() -> ing);
		this.item.getValue();
		this.value = value;
		this.min = min;
		this.max = max;
	}
}