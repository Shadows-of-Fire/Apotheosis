package shadows.apotheosis.ench.table;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.objects.IEnchantingBlock;

public class EnchantmentStatRegistry {

	private static final Map<IRegistryDelegate<Block>, Stats> STATS = new HashMap<>();

	public static void init() {
		register(Blocks.JACK_O_LANTERN, 0, -1 / 3F, 0.25F, 0);
		register(Blocks.GLOWSTONE, 0, -1 / 3F, 0, 0.25F);
		register(ApotheosisObjects.BLAZING_HELLSHELF, 30, 2, 0.3F, 0);
		register(ApotheosisObjects.GLOWING_HELLSHELF, 30, 1.5F, 0, 0.5F);
		register(ApotheosisObjects.CRYSTAL_SEASHELF, 30, 2, 0.3F, 0.3F);
		register(ApotheosisObjects.HEART_SEASHELF, 30, 1.75F, 0, 1F);
		register(ApotheosisObjects.ENDSHELF, 40, 3, 0.35F, 0.35F);
		register(ApotheosisObjects.PEARL_ENDSHELF, 40, 3, 0.5F, 0.75F);
		register(ApotheosisObjects.DRACONIC_ENDSHELF, 50, 4, 0, 0);
		register(ApotheosisObjects.BEESHELF, 0, -15, 10, 0);
		register(ApotheosisObjects.MELONSHELF, 0, -1, -1, 0);
	}

	private static void register(Block block, float maxEterna, float eterna, float quanta, float arcana) {
		STATS.put(block.delegate, new Stats(maxEterna, eterna, quanta, arcana));
	}

	public static class Stats {
		final float maxEterna, eterna, quanta, arcana;

		public Stats(float maxEterna, float eterna, float quanta, float arcana) {
			this.maxEterna = maxEterna;
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

	public static float getMaxEterna(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (STATS.containsKey(block.delegate)) return STATS.get(block.delegate).maxEterna;
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
