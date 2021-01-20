package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.recipe.VanillaPacketDispatcher;

public class RedstoneModifier extends SpawnerModifier {

	public RedstoneModifier() {
		super(new ItemStack(Items.COMPARATOR), -1, -1, -1);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && spawner.redstoneEnabled == inverting;
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
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