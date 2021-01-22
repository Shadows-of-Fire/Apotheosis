package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class NearbyEntityModifier extends SpawnerModifier {

	public NearbyEntityModifier() {
		super(2, 0, 40);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.maxNearbyEntities > min : spawner.spawnerLogic.maxNearbyEntities < max);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.maxNearbyEntities = MathHelper.clamp(spawner.spawnerLogic.maxNearbyEntities + modify, min, max);
		return true;
	}

	@Override
	public String getId() {
		return "max_nearby_entities";
	}

	@Override
	public String getDefaultItem() {
		return Items.GHAST_TEAR.getRegistryName().toString();
	}

}