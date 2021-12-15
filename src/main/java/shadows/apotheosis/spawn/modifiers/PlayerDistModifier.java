package shadows.apotheosis.spawn.modifiers;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class PlayerDistModifier extends SpawnerModifier {

	public PlayerDistModifier() {
		super(2, 0, 50);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return super.canModify(spawner, stack, inverting) && (inverting ? spawner.spawner.requiredPlayerRange > this.min : spawner.spawner.requiredPlayerRange < this.max);
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		int modify = inverting ? -this.value : this.value;
		spawner.spawner.requiredPlayerRange = Mth.clamp(spawner.spawner.requiredPlayerRange + modify, this.min, this.max);
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