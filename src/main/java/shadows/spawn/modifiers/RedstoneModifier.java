package shadows.spawn.modifiers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import shadows.placebo.util.VanillaPacketDispatcher;
import shadows.spawn.TileSpawnerExt;

public class RedstoneModifier extends SpawnerModifier {

	public RedstoneModifier() {
		super(new ItemStack(Items.COMPARATOR), 0);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && spawner.redstoneEnabled == inverting;
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		spawner.redstoneEnabled = !inverting;
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(spawner);
		return true;
	}

	@Override
	public String getCategory() {
		return "redstone_control";
	}

	@Override
	public String getDefaultItem() {
		return Items.COMPARATOR.getRegistryName().toString();
	}

}
