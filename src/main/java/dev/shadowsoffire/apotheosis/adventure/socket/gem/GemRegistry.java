package dev.shadowsoffire.apotheosis.adventure.socket.gem;

import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class GemRegistry extends WeightedDynamicRegistry<Gem> {

    public static final GemRegistry INSTANCE = new GemRegistry();

    public GemRegistry() {
        super(AdventureModule.LOGGER, "gems", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("gem"), Gem.CODEC);
    }

    /**
     * Pulls a random LootRarity and Gem, and generates an Gem Item
     *
     * @param rand   Random
     * @param rarity The rarity, or null if it should be randomly selected.
     * @param luck   The player's luck level
     * @param filter The filter
     * @return A gem item, or an empty ItemStack if no entries were available for the dimension.
     */
    @SafeVarargs
    public static ItemStack createRandomGemStack(RandomSource rand, ServerLevel level, float luck, Predicate<Gem>... filter) {
        Gem gem = GemRegistry.INSTANCE.getRandomItem(rand, luck, filter);
        if (gem == null) return ItemStack.EMPTY;
        RarityClamp clamp = AdventureConfig.GEM_DIM_RARITIES.get(level.dimension().location());
        LootRarity rarity = gem.clamp(LootRarity.random(rand, luck, clamp));
        return createGemStack(gem, rarity);
    }

    public static ItemStack createGemStack(Gem gem, LootRarity rarity) {
        ItemStack stack = new ItemStack(Items.GEM.get());
        GemItem.setGem(stack, gem);
        AffixHelper.setRarity(stack, rarity);
        return stack;
    }

    @Override
    protected void validateItem(ResourceLocation key, Gem item) {
        super.validateItem(key, item);
        for (LootRarity r = item.minRarity; r != item.maxRarity; r = r.next()) {
            boolean atLeastOne = false;
            for (GemBonus bonus : item.bonuses) {
                if (bonus.supports(r)) atLeastOne = true;
            }
            Preconditions.checkArgument(atLeastOne, "No bonuses provided for supported rarity %s. At least one bonus must be provided, or the rarity should not be supported.", RarityRegistry.INSTANCE.getKey(r));
        }
    }

    /**
     * Public bouncer for gem bonus tag resolution.
     */
    public final ICondition.IContext _getContext() {
        return this.getContext();
    }

}
