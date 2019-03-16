package shadows.spawn.modifiers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import shadows.spawn.TileSpawnerExt;

public class SpawnCountModifier extends SpawnerModifier {

	public SpawnCountModifier() {
		super(new ItemStack(Items.FERMENTED_SPIDER_EYE), 1);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.spawnCount > 0 : spawner.spawnerLogic.spawnCount < Integer.MAX_VALUE);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.spawnCount = MathHelper.clamp(spawner.spawnerLogic.spawnCount + modify, 0, Integer.MAX_VALUE);
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
