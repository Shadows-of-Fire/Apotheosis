package shadows.apotheosis.ench.table;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.apotheosis.ench.objects.IEnchantingBlock;

public class EnchantmentStatRegistry {

	private static final Map<IRegistryDelegate<Block>, Stats> STATS = new HashMap<>();

	public static void init() {
		register(Blocks.JACK_O_LANTERN, 0, 0.25F, 0);
	}

	private static void register(Block block, float eterna, float quanta, float arcana) {
		STATS.put(block.delegate, new Stats(eterna, quanta, arcana));
	}

	public static class Stats {
		final float eterna, quanta, arcana;

		public Stats(float eterna, float quanta, float arcana) {
			this.eterna = eterna;
			this.quanta = quanta;
			this.arcana = arcana;
		}
	}

	public static float getEterna(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (STATS.containsKey(block.delegate)) return STATS.get(block.delegate).eterna;
		return state.getEnchantPowerBonus(world, pos);
	}

	public static int getMaxEterna(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getMaxEnchantingPower(state, world, pos);
		return 15;
	}

	public static float getQuanta(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (STATS.containsKey(block.delegate)) return STATS.get(block.delegate).quanta;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getQuantaBonus(state, world, pos);
		return 0;
	}

	public static float getArcana(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (STATS.containsKey(block.delegate)) return STATS.get(block.delegate).arcana;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getArcanaBonus(state, world, pos);
		return 0;
	}

}
