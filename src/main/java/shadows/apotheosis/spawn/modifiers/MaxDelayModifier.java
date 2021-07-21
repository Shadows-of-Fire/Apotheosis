package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class MaxDelayModifier extends SpawnerModifier {

	public MaxDelayModifier() {
		super(-10, 10, 10000);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawner.maxSpawnDelay < this.max : spawner.spawner.maxSpawnDelay > this.min);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -this.value : this.value;
		spawner.spawner.maxSpawnDelay = MathHelper.clamp(spawner.spawner.maxSpawnDelay + modify, this.min, this.max);
		return true;
	}

	@Override
	public String getId() {
		return "max_delay";
	}

	@Override
	public String getDefaultItem() {
		return Items.CLOCK.getRegistryName().toString();
	}

}