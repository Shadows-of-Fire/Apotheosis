package dev.shadowsoffire.apotheosis.affix;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.effect.CatalyzingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.CleavingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.affix.effect.EnchantmentAffix;
import dev.shadowsoffire.apotheosis.affix.effect.EnlightenedAffix;
import dev.shadowsoffire.apotheosis.affix.effect.ExecutingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.FestiveAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MagicalArrowAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MultiAttrAffix;
import dev.shadowsoffire.apotheosis.affix.effect.OmneticAffix;
import dev.shadowsoffire.apotheosis.affix.effect.PsychicAffix;
import dev.shadowsoffire.apotheosis.affix.effect.RadialAffix;
import dev.shadowsoffire.apotheosis.affix.effect.RetreatingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.SpectralShotAffix;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.affix.effect.ThunderstruckAffix;
import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.neoforged.fml.loading.FMLEnvironment;

public class AffixRegistry extends TieredDynamicRegistry<Affix> {

    public static final AffixRegistry INSTANCE = new AffixRegistry();

    private Multimap<AffixType, DynamicHolder<Affix>> byType = ImmutableMultimap.of();

    public AffixRegistry() {
        super(Apotheosis.LOGGER, "affixes", true, true);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.byType = ImmutableMultimap.of();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        ImmutableMultimap.Builder<AffixType, DynamicHolder<Affix>> builder = ImmutableMultimap.builder();
        this.registry.values().forEach(a -> builder.put(a.definition().type(), this.holder(a)));
        this.byType = builder.build();
        if (!FMLEnvironment.production && FMLEnvironment.dist.isClient()) {
            AdventureModuleClient.checkAffixLangKeys();
        }
        if (type == ReloadType.SERVER) {
            this.validateAffixExclusiveSets();
        }
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerCodec(Apotheosis.loc("attribute"), AttributeAffix.CODEC);
        this.registerCodec(Apotheosis.loc("multi_attr"), MultiAttrAffix.CODEC);
        this.registerCodec(Apotheosis.loc("mob_effect"), MobEffectAffix.CODEC);
        this.registerCodec(Apotheosis.loc("damage_reduction"), DamageReductionAffix.CODEC);
        this.registerCodec(Apotheosis.loc("catalyzing"), CatalyzingAffix.CODEC);
        this.registerCodec(Apotheosis.loc("cleaving"), CleavingAffix.CODEC);
        this.registerCodec(Apotheosis.loc("enlightened"), EnlightenedAffix.CODEC);
        this.registerCodec(Apotheosis.loc("executing"), ExecutingAffix.CODEC);
        this.registerCodec(Apotheosis.loc("festive"), FestiveAffix.CODEC);
        this.registerCodec(Apotheosis.loc("magical"), MagicalArrowAffix.CODEC);
        this.registerCodec(Apotheosis.loc("omnetic"), OmneticAffix.CODEC);
        this.registerCodec(Apotheosis.loc("psychic"), PsychicAffix.CODEC);
        this.registerCodec(Apotheosis.loc("radial"), RadialAffix.CODEC);
        this.registerCodec(Apotheosis.loc("retreating"), RetreatingAffix.CODEC);
        this.registerCodec(Apotheosis.loc("spectral"), SpectralShotAffix.CODEC);
        this.registerCodec(Apotheosis.loc("telepathic"), TelepathicAffix.CODEC);
        this.registerCodec(Apotheosis.loc("thunderstruck"), ThunderstruckAffix.CODEC);
        this.registerCodec(Apotheosis.loc("enchantment"), EnchantmentAffix.CODEC);
        this.registerCodec(Apotheosis.loc("stoneforming"), StoneformingAffix.CODEC);
    }

    public Multimap<AffixType, DynamicHolder<Affix>> getTypeMap() {
        return this.byType;
    }

    /**
     * Validates that all affixes in the registry only name bound affixes in their exclusive sets.
     */
    protected void validateAffixExclusiveSets() {
        for (Affix a : this.registry.values()) {
            for (DynamicHolder<Affix> other : a.definition.exclusiveSet()) {
                if (!other.isBound()) {
                    this.logger.error("The affix {} contains the unknown affix {} in its exclusive set!", a.id(), other.getId());
                }
            }
        }
    }

}
