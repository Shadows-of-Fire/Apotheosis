package shadows.apotheosis.adventure.affix;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.effect.CatalyzingAffix;
import shadows.apotheosis.adventure.affix.effect.CleavingAffix;
import shadows.apotheosis.adventure.affix.effect.DamageReductionAffix;
import shadows.apotheosis.adventure.affix.effect.DurableAffix;
import shadows.apotheosis.adventure.affix.effect.EnlightenedAffix;
import shadows.apotheosis.adventure.affix.effect.ExecutingAffix;
import shadows.apotheosis.adventure.affix.effect.FestiveAffix;
import shadows.apotheosis.adventure.affix.effect.MagicalArrowAffix;
import shadows.apotheosis.adventure.affix.effect.OmneticAffix;
import shadows.apotheosis.adventure.affix.effect.PotionAffix;
import shadows.apotheosis.adventure.affix.effect.PsychicAffix;
import shadows.apotheosis.adventure.affix.effect.RadialAffix;
import shadows.apotheosis.adventure.affix.effect.RetreatingAffix;
import shadows.apotheosis.adventure.affix.effect.SpectralShotAffix;
import shadows.apotheosis.adventure.affix.effect.TelepathicAffix;
import shadows.apotheosis.adventure.affix.effect.ThunderstruckAffix;
import shadows.apotheosis.adventure.affix.socket.SocketAffix;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.util.CachedObject;

public class AffixManager extends PlaceboJsonReloadListener<Affix> {

	public static final AffixManager INSTANCE = new AffixManager();

	private Multimap<AffixType, Affix> byType = ImmutableMultimap.of();

	public AffixManager() {
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
		ImmutableMultimap.Builder<AffixType, Affix> builder = ImmutableMultimap.builder();
		this.registry.values().forEach(a -> builder.put(a.type, a));
		byType = builder.build();
		Preconditions.checkArgument(Affixes.SOCKET.get() instanceof SocketAffix, "Socket Affix not registered!");
		Preconditions.checkArgument(Affixes.DURABLE.get() instanceof DurableAffix, "Durable Affix not registered!");
		CachedObject.invalidateAll(AffixHelper.AFFIX_CACHED_OBJECT);
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

	public Multimap<AffixType, Affix> getTypeMap() {
		return byType;
	}

}
