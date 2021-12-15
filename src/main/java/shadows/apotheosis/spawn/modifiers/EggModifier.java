package shadows.apotheosis.spawn.modifiers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.SpawnData;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.SpawnerBuilder;

/**
 * Special case modifier to allow for spawn eggs to work properly.
 * @author Shadows
 *
 */
public class EggModifier extends SpawnerModifier {

	public final List<String> bannedMobs = new ArrayList<>();

	public EggModifier() {
		this.item = new LazyLoadedValue<>(() -> Ingredient.of(Items.WITCH_SPAWN_EGG));
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return stack.getItem() instanceof SpawnEggItem;
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		String name = ((SpawnEggItem) stack.getItem()).getType(stack.getTag()).getRegistryName().toString();
		if (!this.bannedMobs.contains(name) && !name.equals(spawner.spawner.nextSpawnData.getTag().getString(SpawnerBuilder.ID))) {
			spawner.spawner.spawnPotentials.clear();
			spawner.spawner.nextSpawnData = new SpawnData();
			return false;
		}
		return true;
	}

	@Override
	public void load(Configuration cfg) {
		String[] bans = cfg.getStringList("Banned Mobs", this.getId(), new String[0], "A list of entity registry names that cannot be applied to spawners via egg.");
		for (String s : bans)
			this.bannedMobs.add(s);
	}

	@Override
	public String getId() {
		return "spawn_eggs";
	}

	@Override
	public String getDefaultItem() {
		return "";
	}

}