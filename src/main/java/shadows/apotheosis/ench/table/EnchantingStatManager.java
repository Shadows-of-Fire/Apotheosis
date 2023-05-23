package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.objects.IEnchantingBlock;
import shadows.apotheosis.ench.table.EnchantingStatManager.BlockStats;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.TypeKeyed.TypeKeyedBase;

public class EnchantingStatManager extends PlaceboJsonReloadListener<BlockStats> {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final EnchantingStatManager INSTANCE = new EnchantingStatManager();
	private final Map<Block, Stats> statsPerBlock = new HashMap<>();

	private float absoluteMaxEterna = 50;

	protected EnchantingStatManager() {
		super(EnchModule.LOGGER, "enchanting_stats", true, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, BlockStats.SERIALIZER);
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
	public static record Stats(float maxEterna, float eterna, float quanta, float arcana, float rectification, int clues) {

		//Formatter::off
		public static Codec<Stats> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				Codec.FLOAT.optionalFieldOf("maxEterna", 15F).forGetter(Stats::maxEterna),
				Codec.FLOAT.optionalFieldOf("eterna", 0F).forGetter(Stats::eterna),
				Codec.FLOAT.optionalFieldOf("quanta", 0F).forGetter(Stats::quanta),
				Codec.FLOAT.optionalFieldOf("arcana", 0F).forGetter(Stats::arcana),
				Codec.FLOAT.optionalFieldOf("rectification", 0F).forGetter(Stats::rectification),
				Codec.INT.optionalFieldOf("clues", 0).forGetter(Stats::clues))
				.apply(inst, Stats::new)
			);
		//Formatter::on

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

		//Formatter::off
		public static Codec<BlockStats> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				Codec.list(ForgeRegistries.BLOCKS.getCodec()).optionalFieldOf("blocks", Collections.emptyList()).forGetter(bs -> bs.blocks),
				TagKey.codec(Registry.BLOCK_REGISTRY).optionalFieldOf("tag").forGetter(bs -> Optional.empty()),
				ForgeRegistries.BLOCKS.getCodec().optionalFieldOf("block").forGetter(bs -> Optional.empty()),
				Stats.CODEC.fieldOf("stats").forGetter(bs -> bs.stats))
				.apply(inst, BlockStats::new)
			);
		//Formatter::on

		public static final PSerializer<BlockStats> SERIALIZER = PSerializer.fromCodec("Enchanting Stats", CODEC);

		public final List<Block> blocks;
		public final Stats stats;

		public BlockStats(List<Block> blocks, Optional<TagKey<Block>> tag, Optional<Block> block, Stats stats) {
			this.blocks = new ArrayList<>();
			if (!blocks.isEmpty()) this.blocks.addAll(blocks);
			if (tag.isPresent()) this.blocks.addAll(EnchantingStatManager.INSTANCE.getContext().getTag(tag.get()).stream().map(Holder::value).toList());
			if (block.isPresent()) this.blocks.add(block.get());
			this.stats = stats;
		}

		@Override
		public PSerializer<? extends BlockStats> getSerializer() {
			return SERIALIZER;
		}

	}

}