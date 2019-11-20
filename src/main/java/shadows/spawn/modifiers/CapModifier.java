package shadows.spawn.modifiers;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import shadows.spawn.TileSpawnerExt;

public class CapModifier extends SpawnerModifier {

	public CapModifier() {
		super(new ItemStack(Blocks.DRAGON_EGG), 0);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && spawner.ignoresCap == inverting;
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
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
