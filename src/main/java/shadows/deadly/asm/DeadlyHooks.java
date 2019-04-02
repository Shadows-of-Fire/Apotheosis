package shadows.deadly.asm;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.gen.WorldGenerator;

/**
 * ASM methods for the deadly module.
 * @author Shadows
 *
 */
public class DeadlyHooks {

	/**
	 * Injects a custom spawner into a dungeon.
	 * Called from {@link WorldGenDungeons#generate(World, Random, BlockPos)}
	 * Injected by {@link DeadlyTransformer}
	 */
	public static void setDungeonMobSpawner(World world, BlockPos pos, Random rand) {
		if (rand.nextFloat() <= DeadlyConfig.dungeonBrutalChance) {
			WorldGenerator.BRUTAL_SPAWNER.place(world, pos, rand);
		} else if (rand.nextFloat() <= DeadlyConfig.dungeonSwarmChance) {
			WorldGenerator.SWARM_SPAWNER.place(world, pos, rand);
		}
	}

}
