package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.TileSpawnerExt;

public class SpawnCountModifier extends SpawnerModifier {

	public SpawnCountModifier() {
		super(new ItemStack(Items.FERMENTED_SPIDER_EYE), 1, 1, 20);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.spawnCount > min : spawner.spawnerLogic.spawnCount < max);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.spawnCount = MathHelper.clamp(spawner.spawnerLogic.spawnCount + modify, min, max);
		return true;
	}

	@Override
	public String getCategory() {
		return "spawn_count";
	}

	@Override
	public String getDefaultItem() {
		return Items.FERMENTED_SPIDER_EYE.getRegistryName().toString();
	}

}
