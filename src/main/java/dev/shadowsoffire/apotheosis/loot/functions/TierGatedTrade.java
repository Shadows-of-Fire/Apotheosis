package dev.shadowsoffire.apotheosis.loot.functions;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * This is a bit of a hack, but it's a loot function that removes an item unless the player meets a World Tier criteria.
 * <p>
 * We use this to prevent certain generated unique items from being offered by the Wandering Trader until Summit+
 */
public class TierGatedTrade extends LootItemConditionalFunction {

    public static final MapCodec<TierGatedTrade> CODEC = RecordCodecBuilder.mapCodec(inst -> commonFields(inst)
        .and(
            WorldTier.CODEC.optionalFieldOf("min_tier", WorldTier.SUMMIT).forGetter(f -> f.minTier))
        .apply(inst, TierGatedTrade::new));

    /**
     * The lowest {@link WorldTier} at which the item is granted.
     */
    private final WorldTier minTier;

    public TierGatedTrade(List<LootItemCondition> predicates, WorldTier minTier) {
        super(predicates);
        this.minTier = minTier;
    }

    @Override
    public MapCodec<TierGatedTrade> codec() {
        return CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        Entity trader = ctx.getParameter(LootContextParams.THIS_ENTITY);
        Player player = trader.level().getNearestPlayer(trader, -1);
        WorldTier tier = player == null ? WorldTier.HAVEN : WorldTier.getTier(player);
        if (tier.ordinal() >= this.minTier.ordinal()) {
            return stack;
        }
        return ItemStack.EMPTY;
    }

}
