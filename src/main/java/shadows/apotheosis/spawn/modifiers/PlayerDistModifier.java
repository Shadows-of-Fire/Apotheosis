package shadows.apotheosis.spawn.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class PlayerDistModifier extends SpawnerModifier {

	public PlayerDistModifier() {
		super(2, 0, 50);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawnerLogic.activatingRangeFromPlayer > this.min : spawner.spawnerLogic.activatingRangeFromPlayer < this.max);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -this.value : this.value;
		spawner.spawnerLogic.activatingRangeFromPlayer = MathHelper.clamp(spawner.spawnerLogic.activatingRangeFromPlayer + modify, this.min, this.max);
		return true;
	}

	@Override
	public String getId() {
		return "player_activation_range";
	}

	@Override
	public String getDefaultItem() {
		return Items.PRISMARINE_CRYSTALS.getRegistryName().toString();
	}

}