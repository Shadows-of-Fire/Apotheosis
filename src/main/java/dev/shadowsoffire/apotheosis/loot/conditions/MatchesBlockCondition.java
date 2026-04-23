package dev.shadowsoffire.apotheosis.loot.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;

/**
 * Loot Condition to match against {@link LootContextParams#BLOCK_STATE}.
 * <p>
 * This can also be used as a check that the parameter exists when used with {@link AnyHolderSet}.
 */
public record MatchesBlockCondition(HolderSet<Block> blocks) implements LootItemCondition {

    public static final MapCodec<MatchesBlockCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("valid_blocks").forGetter(MatchesBlockCondition::blocks))
        .apply(inst, MatchesBlockCondition::new));

    @Override
    public boolean test(LootContext ctx) {
        if (ctx.hasParameter(LootContextParams.BLOCK_STATE)) {
            BlockState state = ctx.getParameter(LootContextParams.BLOCK_STATE);
            return this.blocks.contains(state.getBlock().builtInRegistryHolder());
        }
        return false;
    }

    @Override
    public MapCodec<MatchesBlockCondition> codec() {
        return CODEC;
    }

}
