package shadows.deadly.feature;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.deadly.config.SpawnerStats;
import shadows.deadly.util.DeadlyConstants;
import shadows.deadly.util.TagBuilder;
import shadows.placebo.util.SpawnerBuilder;

public class SpawnerItem extends WorldFeatureItem {

	protected final SpawnerBuilder spawner;

	public SpawnerItem(SpawnerBuilder spawner, int weight) {
		super(weight);
		this.spawner = spawner;
	}

	public static void addItems(List<SpawnerItem> items, SpawnerStats stats, List<Pair<Integer, ResourceLocation>> weightMobPairs) {
		for (Pair<Integer, ResourceLocation> pair : weightMobPairs) {
			SpawnerBuilder builder = new SpawnerBuilder();
			builder.setType(pair.getRight());
			if (pair.getRight().equals(DeadlyConstants.RANDOM)) builder = TagBuilder.createMobSpawnerRandom();
			stats.apply(builder);
			TagBuilder.checkForSkeleton(builder.getSpawnData());
			items.add(new SpawnerItem(builder, pair.getLeft()));
		}
	}

	/*
	    /// Creates and returns an appropriate spawner item.
	public static SpawnerItem buildSpawner(String name, int index) {
	    if (name == "") {
	        // Do nothing
	    }
	    else if (name == "dungeon")
	        return SpawnerItem.buildMobSpawner(name, index, Properties.DUNGEON_SPAWNERS, _DeadlyWorld.DUNGEON_MOBS);
	    else if (name == "spawner_brutal")
	        return SpawnerItem.buildMobSpawner(name, index, Properties.BRUTAL_SPAWNERS, _DeadlyWorld.SPAWNERS);
	    else if (name == "spawner_rogue")
	        return SpawnerItem.buildMobSpawner(name, index, Properties.SPAWNERS, _DeadlyWorld.SPAWNERS);
	    else if (name == "spawner_swarm")
	        return SpawnerItem.buildMobSpawner(name, index, Properties.SPAWNER_SWARMS, _DeadlyWorld.MOBS);
	    else if (name == "spawner_vein")
	        return SpawnerItem.buildMobSpawner(name, index, Properties.SPAWNER_VEINS, _DeadlyWorld.SPAWNERS);
	
	    else if (name == "spawner_trap") {
	        NBTTagCompound tag = TagBuilder.createMobSpawnerTrap(_DeadlyWorld.SPAWNERS[index]);
	        int weight = Properties.getInt(Properties.SPAWNER_TRAPS, _DeadlyWorld.SPAWNERS[index].toLowerCase());
	        if (_DeadlyWorld.SPAWNERS[index].equalsIgnoreCase("RANDOM"))
	            return new SpawnerItemRandom(tag, weight, true, false);
	        else if (_DeadlyWorld.SPAWNERS[index].equalsIgnoreCase("CREEPER"))
	            return new SpawnerItemCreeper(tag, weight, true, false);
	        else
	            return new SpawnerItem(tag, weight, true, false);
	    }
	    else if (name == "potion")
	        return new SpawnerItem(TagBuilder.createPotionSpawner(_DeadlyWorld.POTIONS[index]), Properties.getInt(Properties.POTION_TRAPS, _DeadlyWorld.POTIONS[index].toLowerCase()), true, false);
	    else if (name == "tnt")
	        return new SpawnerItem(TagBuilder.createTNTSpawner(), 0, true, false);
	    else if (name == "fire")
	        return new SpawnerItem(TagBuilder.createFireSpawner(), 0, true, false);
	
	    else if (name == "arrow")
	        return new SpawnerItem(TagBuilder.createArrowSpawner(index == 1), 0, true, false);
	    else if (name == "silver_nest") {
	        if (index == 2) {
	            TagBuilder tag = new TagBuilder(TagBuilder.createTNTSpawner());
	            tag.setPlayerRange(3);
	            return new SpawnerItem(tag.spawnerTag, 0, true, false);
	        }
	        TagBuilder tag = new TagBuilder(new NBTTagCompound());
	        tag.setType("Silverfish");
	        tag.setMinAndMaxDelay(Properties.getInt(Properties.NESTS, "_min_delay"), Properties.getInt(Properties.NESTS, "_max_delay"));
	        tag.setSpawnCount(Properties.getInt(Properties.NESTS, "_spawn_count"));
	        tag.setMaxNearbyEntities(Properties.getInt(Properties.NESTS, "_nearby_entity_cap"));
	        tag.setSpawnRange(Properties.getInt(Properties.NESTS, "_spawn_range"));
	        tag.setPlayerRange(Properties.getInt(Properties.NESTS, "_player_range"));
	        if (index == 1) {
	            NBTTagCompound data = new NBTTagCompound();
	            TagBuilder.addPotionEffect(data, Potion.poison, 0);
	            TagBuilder.addPotionEffect(data, Potion.regeneration, 1);
	            TagBuilder.addPotionEffect(data, Potion.damageBoost, 0);
	            TagBuilder.addPotionEffect(data, Potion.resistance, 2);
	            tag.setSpawnData(data);
	        }
	        return new SpawnerItem(tag.spawnerTag, 0, false, false);
	    }
	    return null;
	}
	*/

	@Override
	public void place(World world, BlockPos pos) {
		world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
		world.setTileEntity(pos, spawner.build(world, pos));
	}

	public SpawnerBuilder getSpawner() {
		return spawner;
	}
}