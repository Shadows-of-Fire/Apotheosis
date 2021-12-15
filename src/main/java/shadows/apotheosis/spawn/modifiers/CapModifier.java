package shadows.apotheosis.spawn.modifiers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class CapModifier extends SpawnerModifier {

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && spawner.ignoresCap == inverting;
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		spawner.ignoresCap = !inverting;
		return true;
	}

	@Override
	public String getId() {
		return "ignore_spawn_cap";
	}

	@Override
	public String getDefaultItem() {
		return Items.CHORUS_FRUIT.getRegistryName().toString();
	}

}