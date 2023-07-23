package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.WeightedJsonReloadListener;

public class GemManager extends WeightedJsonReloadListener<Gem> {

    public static final GemManager INSTANCE = new GemManager();

    public GemManager() {
        super(AdventureModule.LOGGER, "gems", true, false);
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT, Gem.SERIALIZER);
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
        Gem gem = GemManager.INSTANCE.getRandomItem(rand, luck, filter);
        if (gem == null) return ItemStack.EMPTY;
        LootRarity.Clamped clamp = AdventureConfig.GEM_DIM_RARITIES.get(level.dimension().location());
        LootRarity rarity = gem.clamp(LootRarity.random(rand, luck, clamp));
        return createGemStack(gem, rand, rarity, luck);
    }

    public static ItemStack createGemStack(Gem gem, RandomSource rand, @Nullable LootRarity rarity, float luck) {
        ItemStack stack = new ItemStack(Apoth.Items.GEM.get());
        GemItem.setGem(stack, gem);
        if (rarity == null) rarity = LootRarity.random(rand, luck, gem);
        GemItem.setLootRarity(stack, rarity);
        return stack;
    }

    /**
     * Public bouncer for gem bonus tag resolution.
     */
    public final ICondition.IContext _getContext() {
        return getContext();
    }

}
