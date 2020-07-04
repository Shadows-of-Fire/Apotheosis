package shadows.apotheosis.spawn.modifiers;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.spawner.TileSpawnerExt;
import shadows.placebo.config.Configuration;

public class ConditionModifier extends SpawnerModifier {

	public ConditionModifier() {
		super(new ItemStack(Items.CHORUS_FRUIT), -1, -1, -1);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && spawner.ignoresConditions == inverting;
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		spawner.ignoresConditions = !inverting;
		return true;
	}

	public void load(Configuration cfg) {
		String s = cfg.getString(ITEM, getCategory(), getDefaultItem(), "The item that applies this modifier.");
		item = SpawnerModifiers.readStackCfg(s);
	}

	@Override
	public String getCategory() {
		return "ignore_spawn_conditions";
	}

	@Override
	public String getDefaultItem() {
		return Blocks.DRAGON_EGG.getRegistryName().toString();
	}

}
