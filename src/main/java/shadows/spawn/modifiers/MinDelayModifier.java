package shadows.spawn.modifiers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import shadows.spawn.TileSpawnerExt;

public class MinDelayModifier extends SpawnerModifier {

	public MinDelayModifier() {
		super(new ItemStack(Items.SUGAR), -5);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.minSpawnDelay < Integer.MAX_VALUE : spawner.spawnerLogic.minSpawnDelay > 0);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.minSpawnDelay = MathHelper.clamp(spawner.spawnerLogic.minSpawnDelay + modify, 0, Integer.MAX_VALUE);
		return true;
	}

	@Override
	public String getCategory() {
		return "min_delay";
	}

	@Override
	public String getDefaultItem() {
		return Items.SUGAR.getRegistryName().toString();
	}

}
