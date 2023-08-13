package dev.shadowsoffire.apotheosis.adventure.affix;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import dev.shadowsoffire.apotheosis.Apoth.Affixes;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.CatalyzingAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.CleavingAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.EnlightenedAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.ExecutingAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.FestiveAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.MagicalArrowAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.OmneticAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PsychicAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.RadialAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.RetreatingAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.SpectralShotAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.ThunderstruckAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.SocketAffix;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class AffixRegistry extends DynamicRegistry<Affix> {

    public static final AffixRegistry INSTANCE = new AffixRegistry();

    private Multimap<AffixType, DynamicHolder<Affix>> byType = ImmutableMultimap.of();

    public AffixRegistry() {
        super(AdventureModule.LOGGER, "affixes", true, true);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.byType = ImmutableMultimap.of();
    }

    @Override
    protected void onReload() {
        super.onReload();
        ImmutableMultimap.Builder<AffixType, DynamicHolder<Affix>> builder = ImmutableMultimap.builder();
        this.registry.values().forEach(a -> builder.put(a.type, this.holder(a.getId())));
        this.byType = builder.build();
        Preconditions.checkArgument(Affixes.SOCKET.get() instanceof SocketAffix, "Socket Affix not registered!");
        Preconditions.checkArgument(Affixes.DURABLE.get() instanceof DurableAffix, "Durable Affix not registered!");
        if (!FMLEnvironment.production && FMLEnvironment.dist.isClient()) {
            AdventureModuleClient.checkAffixLangKeys();
        }
        RarityRegistry.INSTANCE.validateLootRules();
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(Apotheosis.loc("attribute"), AttributeAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("mob_effect"), PotionAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("damage_reduction"), DamageReductionAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("catalyzing"), CatalyzingAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("cleaving"), CleavingAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("enlightened"), EnlightenedAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("executing"), ExecutingAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("festive"), FestiveAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("magical"), MagicalArrowAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("omnetic"), OmneticAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("psychic"), PsychicAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("radial"), RadialAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("retreating"), RetreatingAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("spectral"), SpectralShotAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("telepathic"), TelepathicAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("thunderstruck"), ThunderstruckAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("socket"), SocketAffix.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("durable"), DurableAffix.SERIALIZER);
    }

    public Multimap<AffixType, DynamicHolder<Affix>> getTypeMap() {
        return this.byType;
    }

}
