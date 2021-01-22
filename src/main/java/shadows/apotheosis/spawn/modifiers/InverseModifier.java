package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.config.Configuration;

public class InverseModifier extends SpawnerModifier {

	@Override
	public void load(Configuration cfg) {
		String s = cfg.getString("Inverse Item", "general", "minecraft:quartz", "When held in the off-hand, this item makes modifiers change stats in the opposite direction.");
		this.item = SpawnerModifiers.readIngredient(s);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return false;
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return false;
	}

	@Override
	public String getId() {
		return "inverse";
	}

	@Override
	public String getDefaultItem() {
		return "";
	}

}
