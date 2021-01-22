package shadows.apotheosis.spawn.modifiers;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class ConditionModifier extends SpawnerModifier {

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && spawner.ignoresConditions == inverting;
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		spawner.ignoresConditions = !inverting;
		return true;
	}

	@Override
	public String getId() {
		return "ignore_spawn_conditions";
	}

	@Override
	public String getDefaultItem() {
		return Blocks.DRAGON_EGG.getRegistryName().toString();
	}

}