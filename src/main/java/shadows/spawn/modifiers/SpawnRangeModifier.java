package shadows.spawn.modifiers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import shadows.spawn.TileSpawnerExt;

public class SpawnRangeModifier extends SpawnerModifier {

	public SpawnRangeModifier() {
		super(new ItemStack(Items.BLAZE_ROD), 1);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.spawnRange > 0 : spawner.spawnerLogic.spawnRange < Integer.MAX_VALUE);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.spawnRange = MathHelper.clamp(spawner.spawnerLogic.spawnRange + modify, 0, Integer.MAX_VALUE);
		return true;
	}

	@Override
	public String getCategory() {
		return "spawn_range";
	}

	@Override
	public String getDefaultItem() {
		return Items.BLAZE_ROD.getRegistryName().toString();
	}

}
