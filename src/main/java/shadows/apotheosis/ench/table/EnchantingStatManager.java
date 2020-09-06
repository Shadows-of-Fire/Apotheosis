package shadows.apotheosis.ench.table;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.objects.IEnchantingBlock;

public class EnchantingStatManager extends JsonReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final EnchantingStatManager INSTANCE = new EnchantingStatManager();

	private final Map<IRegistryDelegate<Block>, Stats> stats = new HashMap<>();

	protected EnchantingStatManager() {
		super(GSON, "enchanting_stats");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> objects, IResourceManager mgr, IProfiler profile) {
		stats.clear();
		objects.forEach((key, obj) -> {
			try {
				Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(obj.get("block").getAsString()));
				Stats stats = GSON.fromJson(obj.get("stats"), Stats.class);
				this.stats.put(b.delegate, stats);
			} catch (Exception e) {
				EnchModule.LOGGER.error("Failed to read enchantment stat file {}.", key);
				e.printStackTrace();
			}
		});
	}

	public static float getEterna(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).eterna;
		return state.getEnchantPowerBonus(world, pos);
	}

	public static float getMaxEterna(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).maxEterna;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getMaxEnchantingPower(state, world, pos);
		return 15;
	}

	public static float getQuanta(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).quanta;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getQuantaBonus(state, world, pos);
		return 0;
	}

	public static float getArcana(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).arcana;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getArcanaBonus(state, world, pos);
		return 0;
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

}
