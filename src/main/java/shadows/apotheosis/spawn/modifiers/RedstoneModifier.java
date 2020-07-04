package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.spawner.TileSpawnerExt;
import shadows.placebo.config.Configuration;
import shadows.placebo.recipe.VanillaPacketDispatcher;

public class RedstoneModifier extends SpawnerModifier {

	public RedstoneModifier() {
		super(new ItemStack(Items.COMPARATOR), -1, -1, -1);
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

	public void load(Configuration cfg) {
		String s = cfg.getString(ITEM, getCategory(), getDefaultItem(), "The item that applies this modifier.");
		item = SpawnerModifiers.readStackCfg(s);
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
