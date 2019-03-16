package shadows.spawn.modifiers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import shadows.spawn.TileSpawnerExt;

public class MaxDelayModifier extends SpawnerModifier {

	public MaxDelayModifier() {
		super(new ItemStack(Items.CLOCK), -5);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.maxSpawnDelay < Integer.MAX_VALUE : spawner.spawnerLogic.maxSpawnDelay > 0);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.maxSpawnDelay = MathHelper.clamp(spawner.spawnerLogic.maxSpawnDelay + modify, 0, Integer.MAX_VALUE);
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
