package dev.shadowsoffire.apotheosis.socket.gem;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.ExtraGemBonusRegistry.ExtraGemBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GemRegistry extends TieredDynamicRegistry<Gem> {

    public static final GemRegistry INSTANCE = new GemRegistry();

    public GemRegistry() {
        super(Apotheosis.LOGGER, "gems", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("gem"), Gem.CODEC);
    }

    @Override
    protected void validateItem(ResourceLocation key, Gem item) {
        super.validateItem(key, item);
        for (Purity p : Purity.values()) {
            if (p.isAtLeast(item.getMinPurity())) {
                boolean atLeastOne = false;
                for (GemBonus bonus : item.bonuses) {
                    if (bonus.supports(p)) {
                        atLeastOne = true;
                    }
                }
                Preconditions.checkArgument(atLeastOne, "No bonuses provided for supported purity %s. At least one bonus must be provided, or the minimum purity should be raised.", p.getName());
            }
        }
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        if (type != ReloadType.INTEGRATED_CLIENT) {
            for (Gem gem : this.getValues()) {
                DynamicHolder<Gem> holder = this.holder(gem);
                for (ExtraGemBonus extraBonus : ExtraGemBonusRegistry.getBonusesFor(holder)) {
                    for (GemBonus bonus : extraBonus.bonuses()) {
                        try {
                            gem.appendExtraBonus(bonus);
                        }
                        catch (Exception ex) {
                            ResourceLocation extraBonusKey = ExtraGemBonusRegistry.INSTANCE.getKey(extraBonus);
                            this.logger.warn("Failed to apply extra gem bonus for class {} to gem {}.", bonus.getGemClass().key(), holder.getId());
                            this.logger.warn("Exception while applying ExtraGemBonus %s: ".formatted(extraBonusKey), ex);
                        }
                    }
                }
            }
        }
    }

    @Override
    @Nullable
    public Gem getRandomItem(GenContext ctx) {
        return this.getRandomItem(ctx, Constraints.eval(ctx));
    }

    /**
     * Creates a new {@link ItemStack} containing the provided {@link Gem}.
     * <p>
     * The provided purity will be automatically clamped based on {@link Gem#getMinPurity()}.
     * 
     * @deprecated Use {@link Gem#toStack(Purity)} instead.
     */
    @Deprecated
    public static ItemStack createGemStack(Gem gem, Purity purity) {
        return gem.toStack(purity);
    }

    /**
     * Pulls a random Gem and Purity, then generates an item stack holding them.
     *
     * @param rand   A random
     * @param rarity The rarity, or null if it should be randomly selected.
     * @param luck   The player's luck level
     * @param filter The filter
     * @return A gem item, or an empty ItemStack if no entries were available for the dimension.
     */
    public static ItemStack createRandomGemStack(GenContext ctx) {
        Gem gem = GemRegistry.INSTANCE.getRandomItem(ctx);
        if (gem == null) {
            return ItemStack.EMPTY;
        }
        Purity purity = Purity.random(ctx);
        return gem.toStack(purity);
    }

}
