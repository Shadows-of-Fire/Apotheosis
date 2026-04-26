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
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;
import dev.shadowsoffire.placebo.dynreg.SubtypedSerializer;
import net.neoforged.fml.loading.FMLEnvironment;

public class AffixRegistry extends TieredDynamicRegistry<Affix> {

    /**
     * Public serializer so external mods can register additional Affix subtypes during their setup phase.
     */
    public static final SubtypedSerializer<Affix> SERIALIZER = RegistrySerializer.<Affix>subtypedSynced("affixes")
        .register(Apotheosis.loc("attribute"), AttributeAffix.CODEC)
        .register(Apotheosis.loc("multi_attr"), MultiAttrAffix.CODEC)
        .register(Apotheosis.loc("mob_effect"), MobEffectAffix.CODEC)
        .register(Apotheosis.loc("damage_reduction"), DamageReductionAffix.CODEC)
        .register(Apotheosis.loc("catalyzing"), CatalyzingAffix.CODEC)
        .register(Apotheosis.loc("cleaving"), CleavingAffix.CODEC)
        .register(Apotheosis.loc("enlightened"), EnlightenedAffix.CODEC)
        .register(Apotheosis.loc("executing"), ExecutingAffix.CODEC)
        .register(Apotheosis.loc("festive"), FestiveAffix.CODEC)
        .register(Apotheosis.loc("magical"), MagicalArrowAffix.CODEC)
        .register(Apotheosis.loc("omnetic"), OmneticAffix.CODEC)
        .register(Apotheosis.loc("psychic"), PsychicAffix.CODEC)
        .register(Apotheosis.loc("radial"), RadialAffix.CODEC)
        .register(Apotheosis.loc("retreating"), RetreatingAffix.CODEC)
        .register(Apotheosis.loc("spectral"), SpectralShotAffix.CODEC)
        .register(Apotheosis.loc("telepathic"), TelepathicAffix.CODEC)
        .register(Apotheosis.loc("thunderstruck"), ThunderstruckAffix.CODEC)
        .register(Apotheosis.loc("enchantment"), EnchantmentAffix.CODEC)
        .register(Apotheosis.loc("stoneforming"), StoneformingAffix.CODEC);

    public static final AffixRegistry INSTANCE = new AffixRegistry();

    private Multimap<AffixType, DynamicHolder<Affix>> byType = ImmutableMultimap.of();

    public AffixRegistry() {
        super(Apotheosis.LOGGER, Apotheosis.loc("affixes"), SERIALIZER);
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
        if (!FMLEnvironment.isProduction() && FMLEnvironment.getDist().isClient()) {
            AdventureModuleClient.checkAffixLangKeys();
        }
        if (type == ReloadType.SERVER) {
            this.validateAffixExclusiveSets();
        }
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
