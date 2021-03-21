package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class SpawnRangeModifier extends SpawnerModifier {

	public SpawnRangeModifier() {
		super(1, 1, 32);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.spawnRange > this.min : spawner.spawnerLogic.spawnRange < this.max);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -this.value : this.value;
		spawner.spawnerLogic.spawnRange = MathHelper.clamp(spawner.spawnerLogic.spawnRange + modify, this.min, this.max);
		return true;
	}

	@Override
	public String getId() {
		return "spawn_range";
	}

	@Override
	public String getDefaultItem() {
		return Items.BLAZE_ROD.getRegistryName().toString();
	}

}