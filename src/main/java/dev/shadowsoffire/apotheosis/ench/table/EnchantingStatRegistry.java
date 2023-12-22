package dev.shadowsoffire.apotheosis.ench.table;

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

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry.BlockStats;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantingStatRegistry extends DynamicRegistry<BlockStats> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final EnchantingStatRegistry INSTANCE = new EnchantingStatRegistry();
    private final Map<Block, Stats> statsPerBlock = new HashMap<>();

    private float absoluteMaxEterna = 50;

    protected EnchantingStatRegistry() {
        super(EnchModule.LOGGER, "enchanting_stats", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("enchanting_stats"), BlockStats.CODEC);
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
        return ((IEnchantingBlock) block).getMaxEnchantingPower(state, world, pos);
    }

    /**
     * Retrieves the Quanta value for a specific block.
     * This can be provided by a stat file, or {@link IEnchantingBlock#getQuantaBonus}
     * 1F of Quanta = 1% of Quanta in the enchanting table.
     */
    public static float getQuanta(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).quanta;
        return ((IEnchantingBlock) block).getQuantaBonus(state, world, pos);
    }

    /**
     * Retrieves the Arcana value for a specific block.
     * This can be provided by a stat file, or {@link IEnchantingBlock#getArcanaBonus}
     * 1F of Arcana = 1% of Arcana in the enchanting table.
     */
    public static float getArcana(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).arcana;
        return ((IEnchantingBlock) block).getArcanaBonus(state, world, pos);
    }

    /**
     * Retrieves the Quanta Rectification value for a specific block.
     * See {@link IEnchantingBlock#getQuantaRectification}
     */
    public static float getQuantaRectification(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).rectification;
        return ((IEnchantingBlock) block).getQuantaRectification(state, world, pos);
    }

    /**
     * Retrieves the number of bonus clues this block provides.
     * See {@link IEnchantingBlock#getBonusClues}
     */
    public static int getBonusClues(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (INSTANCE.statsPerBlock.containsKey(block)) return INSTANCE.statsPerBlock.get(block).clues;
        return ((IEnchantingBlock) block).getBonusClues(state, world, pos);
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

        public static Codec<Stats> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                PlaceboCodecs.nullableField(Codec.FLOAT, "maxEterna", 15F).forGetter(Stats::maxEterna),
                PlaceboCodecs.nullableField(Codec.FLOAT, "eterna", 0F).forGetter(Stats::eterna),
                PlaceboCodecs.nullableField(Codec.FLOAT, "quanta", 0F).forGetter(Stats::quanta),
                PlaceboCodecs.nullableField(Codec.FLOAT, "arcana", 0F).forGetter(Stats::arcana),
                PlaceboCodecs.nullableField(Codec.FLOAT, "rectification", 0F).forGetter(Stats::rectification),
                PlaceboCodecs.nullableField(Codec.INT, "clues", 0).forGetter(Stats::clues))
            .apply(inst, Stats::new));

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

    public static class BlockStats implements CodecProvider<BlockStats> {

        public static Codec<BlockStats> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                PlaceboCodecs.nullableField(Codec.list(ForgeRegistries.BLOCKS.getCodec()), "blocks", Collections.emptyList()).forGetter(bs -> bs.blocks),
                PlaceboCodecs.nullableField(TagKey.codec(Registries.BLOCK), "tag").forGetter(bs -> Optional.empty()),
                PlaceboCodecs.nullableField(ForgeRegistries.BLOCKS.getCodec(), "block").forGetter(bs -> Optional.empty()),
                Stats.CODEC.fieldOf("stats").forGetter(bs -> bs.stats))
            .apply(inst, BlockStats::new));

        public final List<Block> blocks;
        public final Stats stats;

        public BlockStats(List<Block> blocks, Optional<TagKey<Block>> tag, Optional<Block> block, Stats stats) {
            this.blocks = new ArrayList<>();
            if (!blocks.isEmpty()) this.blocks.addAll(blocks);
            if (tag.isPresent()) this.blocks.addAll(EnchantingStatRegistry.INSTANCE.getContext().getTag(tag.get()).stream().map(Holder::value).toList());
            if (block.isPresent()) this.blocks.add(block.get());
            this.stats = stats;
        }

        @Override
        public Codec<? extends BlockStats> getCodec() {
            return CODEC;
        }

    }

}
