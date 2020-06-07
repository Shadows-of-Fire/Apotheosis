package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.TileSpawnerExt;

public class PlayerDistModifier extends SpawnerModifier {

	public PlayerDistModifier() {
		super(new ItemStack(Items.PRISMARINE_CRYSTALS), 2);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.activatingRangeFromPlayer > min : spawner.spawnerLogic.activatingRangeFromPlayer < max);
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -value : value;
		spawner.spawnerLogic.activatingRangeFromPlayer = MathHelper.clamp(spawner.spawnerLogic.activatingRangeFromPlayer + modify, min, max);
		return true;
	}

	@Override
	public String getCategory() {
		return "player_activation_range";
	}

	@Override
	public String getDefaultItem() {
		return Items.PRISMARINE_CRYSTALS.getRegistryName().toString();
	}

}
