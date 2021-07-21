package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class MinDelayModifier extends SpawnerModifier {

	public MinDelayModifier() {
		super(-5, 5, 10000);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawner.minSpawnDelay < this.max : spawner.spawner.minSpawnDelay > this.min);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -this.value : this.value;
		spawner.spawner.minSpawnDelay = MathHelper.clamp(spawner.spawner.minSpawnDelay + modify, this.min, this.max);
		return true;
	}

	@Override
	public String getId() {
		return "min_delay";
	}

	@Override
	public String getDefaultItem() {
		return Items.SUGAR.getRegistryName().toString();
	}

}