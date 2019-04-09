package shadows.spawn.modifiers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import shadows.placebo.util.SpawnerBuilder;
import shadows.spawn.TileSpawnerExt;

/**
 * Special case modifier to allow for spawn eggs to work properly.
 * @author Shadows
 *
 */
public class EggModifier extends SpawnerModifier {

	List<String> bannedMobs = new ArrayList<>();

	public EggModifier(ItemStack item) {
		super(item, -1);
	}

	public EggModifier() {
		this(ItemStack.EMPTY);
	}

	@Override
	public boolean canModify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		return stack.getItem() instanceof ItemMonsterPlacer;
	}

	@Override
	public boolean modify(TileSpawnerExt spawner, ItemStack stack, boolean inverting) {
		String name = ItemMonsterPlacer.getNamedIdFrom(stack).toString();
		if (!bannedMobs.contains(name) && !name.equals(spawner.spawnerLogic.spawnData.getNbt().getString(SpawnerBuilder.ID))) {
			spawner.spawnerLogic.potentialSpawns.clear();
			return false;
		}
		return true;
	}

	@Override
	public void load(Configuration cfg) {
		String[] bans = cfg.getStringList("Banned Mobs", getCategory(), new String[0], "A list of entity registry names that cannot be applied to spawners via egg.");
		for (String s : bans)
			bannedMobs.add(s);
	}

	@Override
	public String getCategory() {
		return "spawn_eggs";
	}

	@Override
	public String getDefaultItem() {
		return "";
	}

}
