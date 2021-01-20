package shadows.apotheosis.spawn.modifiers;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class CapModifier extends SpawnerModifier {

	public CapModifier() {
		super(new ItemStack(Blocks.DRAGON_EGG), -1, -1, -1);
	}

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
	public String getCategory() {
		return "ignore_spawn_cap";
	}

	@Override
	public String getDefaultItem() {
		return Items.CHORUS_FRUIT.getRegistryName().toString();
	}

}