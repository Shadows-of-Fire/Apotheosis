package shadows.spawn.modifiers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import shadows.spawn.TileSpawnerExt;

public class NearbyEntityModifier extends SpawnerModifier {

	public NearbyEntityModifier() {
		super(new ItemStack(Items.GHAST_TEAR), 2);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.maxNearbyEntities > 0 : spawner.spawnerLogic.maxNearbyEntities < Integer.MAX_VALUE);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.maxNearbyEntities = MathHelper.clamp(spawner.spawnerLogic.maxNearbyEntities + modify, 0, Integer.MAX_VALUE);
		return true;
	}

	@Override
	public String getCategory() {
		return "max_nearby_entities";
	}

	@Override
	public String getDefaultItem() {
		return Items.GHAST_TEAR.getRegistryName().toString();
	}

}
