package shadows.spawn.modifiers;

import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import shadows.placebo.util.SpawnerBuilder;
import shadows.spawn.TileSpawnerExt;

/**
 * Special case modifier to allow for spawn eggs to work properly.
 * @author Shadows
 *
 */
public class EggModifier extends SpawnerModifier {

	public EggModifier(ItemStack item) {
		super(item, -1);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return stack.getItem() instanceof ItemMonsterPlacer && !ItemMonsterPlacer.getNamedIdFrom(stack).toString().equals(spawner.spawnerLogic.spawnData.getNbt().getString(SpawnerBuilder.ID));
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		spawner.spawnerLogic.potentialSpawns.clear();
		return false;
	}

	@Override
	public void load(Configuration cfg) {
	}

	@Override
	public String getCategory() {
		return "";
	}

	@Override
	public String getDefaultItem() {
		return "";
	}

}
