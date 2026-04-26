package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.ExtraGemBonusRegistry.ExtraGemBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;

/**
 * Registry of additional gem bonuses. This can be used to add conditional bonuses to gems, or modify
 * existing gems to add bonuses only when certain mods are present.
 * <p>
 * It can also be used by addon mods to add bonuses to Apotheosis-native gems.
 */
public class ExtraGemBonusRegistry extends DynamicRegistry<ExtraGemBonus> {

    public static final ExtraGemBonusRegistry INSTANCE = new ExtraGemBonusRegistry();

    protected Multimap<DynamicHolder<Gem>, ExtraGemBonus> extraBonuses = HashMultimap.create();

    public ExtraGemBonusRegistry() {
        super(Apotheosis.LOGGER, Apotheosis.loc("extra_gem_bonuses"), RegistrySerializer.synced(ExtraGemBonus.CODEC));
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.extraBonuses = HashMultimap.create();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        for (ExtraGemBonus extraBonus : this.getValues()) {
            this.extraBonuses.put(extraBonus.gem, extraBonus);
        }
    }

    public static Collection<ExtraGemBonus> getBonusesFor(DynamicHolder<Gem> gem) {
        return INSTANCE.extraBonuses.get(gem);
    }

    public static record ExtraGemBonus(DynamicHolder<Gem> gem, List<GemBonus> bonuses) {

        public static final Codec<ExtraGemBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                GemRegistry.INSTANCE.holderCodec().fieldOf("gem").forGetter(ExtraGemBonus::gem),
                GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(ExtraGemBonus::bonuses))
            .apply(inst, ExtraGemBonus::new));

        public static Builder builder(DynamicHolder<Gem> gem) {
            return new Builder(gem);
        }

        public static class Builder {

            protected final DynamicHolder<Gem> gem;
            protected List<GemBonus> bonuses = new ArrayList<>();

            public Builder(DynamicHolder<Gem> gem) {
                this.gem = gem;
            }

            public Builder bonus(LootCategory cat, GemBonus.Builder builder) {
                return this.bonus(new GemClass(cat), builder);
            }

            public Builder bonus(GemClass gClass, GemBonus.Builder builder) {
                this.bonuses.add(builder.build(gClass));
                return this;
            }

            public Builder bonus(GemBonus bonus) {
                this.bonuses.add(bonus);
                return this;
            }

            public ExtraGemBonus build() {
                return new ExtraGemBonus(this.gem, this.bonuses);
            }
        }

    }

}
