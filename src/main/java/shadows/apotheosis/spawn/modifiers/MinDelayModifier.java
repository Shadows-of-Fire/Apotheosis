package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class MinDelayModifier extends SpawnerModifier {

	public MinDelayModifier() {
		super(new ItemStack(Items.SUGAR), -5, 5, 99999);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.minSpawnDelay < max : spawner.spawnerLogic.minSpawnDelay > min);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.minSpawnDelay = MathHelper.clamp(spawner.spawnerLogic.minSpawnDelay + modify, min, max);
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