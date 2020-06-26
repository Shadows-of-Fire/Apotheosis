package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.TileSpawnerExt;

public class SpawnRangeModifier extends SpawnerModifier {

	public SpawnRangeModifier() {
		super(new ItemStack(Items.BLAZE_ROD), 1, 0, 32);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.spawnRange > min : spawner.spawnerLogic.spawnRange < max);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.spawnRange = MathHelper.clamp(spawner.spawnerLogic.spawnRange + modify, min, max);
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
