package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.TileSpawnerExt;

public class MaxDelayModifier extends SpawnerModifier {

	public MaxDelayModifier() {
		super(new ItemStack(Items.CLOCK), -10, 20, 99999);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.maxSpawnDelay < max : spawner.spawnerLogic.maxSpawnDelay > min);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.maxSpawnDelay = MathHelper.clamp(spawner.spawnerLogic.maxSpawnDelay + modify, min, max);
		return true;
	}

	@Override
	public String getCategory() {
		return "max_delay";
	}

	@Override
	public String getDefaultItem() {
		return Items.CLOCK.getRegistryName().toString();
	}

}
