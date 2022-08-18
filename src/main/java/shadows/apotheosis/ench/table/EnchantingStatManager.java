package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.objects.IEnchantingBlock;
import shadows.apotheosis.ench.table.EnchantingStatManager.BlockStats;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

public class EnchantingStatManager extends PlaceboJsonReloadListener<BlockStats> {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final EnchantingStatManager INSTANCE = new EnchantingStatManager();
	private final Map<Block, Stats> statsPerBlock = new HashMap<>();

	private float absoluteMaxEterna = 50;

	protected EnchantingStatManager() {
		super(EnchModule.LOGGER, "enchanting_stats", true, false);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<BlockStats>("Enchanting Stats").withJsonDeserializer(obj -> {
			Stats stats = GSON.fromJson(obj.get("stats"), Stats.class);
			List<Block> blocks = new ArrayList<>();
			if (obj.has("tag")) {
				TagKey<Block> tag = BlockTags.create(new ResourceLocation(obj.get("tag").getAsString()));
				this.getContext().getTag(tag).getValues().stream().map(Holder::value).forEach(blocks::add);
			} else {
				Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(obj.get("block").getAsString()));
				blocks.add(b);
			}
			return new BlockStats(blocks, stats);
		}).withNetworkSerializer((stats, buf) -> {
			buf.writeInt(stats.blocks.size());
			stats.blocks.forEach(b -> buf.writeInt(Registry.BLOCK.getId(b)));
			stats.stats.write(buf);
		}).withNetworkDeserializer(buf -> {
			int size = buf.readInt();
			List<Block> blocks = new ArrayList<>();
			for (int i = 0; i < size; i++)
				blocks.add(Registry.BLOCK.byId(buf.readInt()));
			return new BlockStats(blocks, Stats.read(buf));
		}));
	}

	@Override
	protected void beginReload() {
		super.beginReload();
		this.statsPerBlock.clear();
	}

	@Override
	protected void onReload() {
		super.onReload();
		for (BlockStats bStats : this.registry.values()) {
			bStats.blocks.forEach(b -> this.statsPerBlock.put(b, bStats.stats));
		}
		this.computeAbsoluteMaxEterna();
	}

	/**
	 * Retrieves the Eterna value for a specific block.
	 * This can be provided by a stat file, or {@link BlockState#getEnchantPowerBonus}
	 * 1F of Eterna = 2 Levels in the enchanting table.
	 */
	public static float getEterna(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).eterna;
		return state.getEnchantPowerBonus(world, pos);
	}

	/**
	 * Retrieves the Max Eterna value for a specific block.
	 * This can be provided by a stat file, or {@link IEnchantingBlock#getMaxEnchantingPower}
	 * 1F of Eterna = 2 Levels in the enchanting table.
	 */
	public static float getMaxEterna(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).maxEterna;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getMaxEnchantingPower(state, world, pos);
		return 15;
	}

	/**
	 * Retrieves the Quanta value for a specific block.
	 * This can be provided by a stat file, or {@link IEnchantingBlock#getQuantaBonus}
	 * 1F of Quanta = 1% of Quanta in the enchanting table.
	 */
	public static float getQuanta(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).quanta;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getQuantaBonus(state, world, pos);
		return 0;
	}

	/**
	 * Retrieves the Arcana value for a specific block.
	 * This can be provided by a stat file, or {@link IEnchantingBlock#getArcanaBonus}
	 * 1F of Arcana = 1% of Arcana in the enchanting table.
	 */
	public static float getArcana(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).arcana;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getArcanaBonus(state, world, pos);
		return 0;
	}

	/**
	 * Retrieves the Quanta Rectification value for a specific block.
	 * See {@link IEnchantingBlock#getQuantaRectification}
	 */
	public static float getQuantaRectification(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).rectification;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getQuantaRectification(state, world, pos);
		return 0;
	}

	/**
	 * Retrieves the number of bonus clues this block provides.
	 * See {@link IEnchantingBlock#getBonusClues}
	 */
	public static int getBonusClues(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).clues;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getBonusClues(state, world, pos);
		return 0;
	}

	/**
	 * This returns the highest possible eterna value, based on the definitions for all stat providers.
	 */
	public static float getAbsoluteMaxEterna() {
		return INSTANCE.absoluteMaxEterna;
	}

	private void computeAbsoluteMaxEterna() {
		this.absoluteMaxEterna = this.registry.values().stream().max(Comparator.comparingDouble(s -> s.stats.maxEterna)).get().stats.maxEterna;
	}

	/**
	 * Enchanting Stats.
	 * Max Eterna is the highest amount of eterna this object may contribute to.
	 * Eterna is the eterna provided (1F == 2 levels)
	 * Quanta is the quanta provided (1F == 1%)
	 * Arcana is the arcana provided (1F == 1%)
	 */
	public static class Stats {
		public final float maxEterna, eterna, quanta, arcana, rectification;
		public final int clues;

		public Stats(float maxEterna, float eterna, float quanta, float arcana, float rectification, int clues) {
			this.maxEterna = maxEterna;
			this.eterna = eterna;
			this.quanta = quanta;
			this.arcana = arcana;
			this.rectification = rectification;
			this.clues = clues;
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeFloat(this.maxEterna);
			buf.writeFloat(this.eterna);
			buf.writeFloat(this.quanta);
			buf.writeFloat(this.arcana);
			buf.writeFloat(this.rectification);
			buf.writeByte(this.clues);
		}

		public static Stats read(FriendlyByteBuf buf) {
			return new Stats(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readByte());
		}
	}

	public static class BlockStats extends TypeKeyedBase<BlockStats> {

		public final List<Block> blocks;
		public final Stats stats;

		public BlockStats(List<Block> blocks, Stats stats) {
			this.blocks = blocks;
			this.stats = stats;
		}

	}

}