package shadows.spawn;

import java.util.function.BiConsumer;

import org.apache.logging.log4j.util.TriConsumer;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.MobSpawnerBaseLogic;

public class SpawnerModifier {

	protected final Ingredient item;
	protected final TriConsumer<TileSpawnerExt, MobSpawnerBaseLogic, Boolean> action;

	public SpawnerModifier(Ingredient item, TriConsumer<TileSpawnerExt, MobSpawnerBaseLogic, Boolean> action) {
		init();
		this.item = item;
		this.action = action;
	}

	public SpawnerModifier(Ingredient item, BiConsumer<MobSpawnerBaseLogic, Boolean> action) {
		this(item, (a, b, c) -> action.accept(b, c));
	}

	public SpawnerModifier(ItemStack item, TriConsumer<TileSpawnerExt, MobSpawnerBaseLogic, Boolean> action) {
		this(Ingredient.fromStacks(item), action);
	}

	public SpawnerModifier(ItemStack item, BiConsumer<MobSpawnerBaseLogic, Boolean> action) {
		this(item, (a, b, c) -> action.accept(b, c));
	}

	protected void init() {
		SpawnerModifiers.MODIFIERS.add(this);
	}

	public boolean matches(ItemStack stack) {
		return item.apply(stack);
	}

	public Ingredient getIngredient() {
		return item;
	}

	public void modify(TileSpawnerExt tile, boolean inverting) {
		action.accept(tile, tile.spawnerLogic, inverting);
	}

	public boolean returnVal() {
		return true;
	}
}
