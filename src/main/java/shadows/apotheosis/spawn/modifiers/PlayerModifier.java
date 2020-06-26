package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.spawner.TileSpawnerExt;
import shadows.placebo.config.Configuration;

public class PlayerModifier extends SpawnerModifier {

	public PlayerModifier() {
		super(new ItemStack(Items.NETHER_STAR), -1, -1, -1);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && spawner.ignoresPlayers == inverting;
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		spawner.ignoresPlayers = !inverting;
		return true;
	}

	public void load(Configuration cfg) {
		String s = cfg.getString(ITEM, getCategory(), getDefaultItem(), "The item that applies this modifier.");
		item = SpawnerModifiers.readStackCfg(s);
	}

	@Override
	public String getCategory() {
		return "require_players";
	}

	@Override
	public String getDefaultItem() {
		return Items.NETHER_STAR.getRegistryName().toString();
	}

}
