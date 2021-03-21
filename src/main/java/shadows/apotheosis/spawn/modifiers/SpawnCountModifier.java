package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class SpawnCountModifier extends SpawnerModifier {

	public SpawnCountModifier() {
		super(1, 1, 20);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.spawnCount > this.min : spawner.spawnerLogic.spawnCount < this.max);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -this.value : this.value;
		spawner.spawnerLogic.spawnCount = MathHelper.clamp(spawner.spawnerLogic.spawnCount + modify, this.min, this.max);
		return true;
	}

	@Override
	public String getId() {
		return "spawn_count";
	}

	@Override
	public String getDefaultItem() {
		return Items.FERMENTED_SPIDER_EYE.getRegistryName().toString();
	}

}