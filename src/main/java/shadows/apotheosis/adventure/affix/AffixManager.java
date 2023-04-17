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
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.PlaceboJsonReloadListener;

public class AffixManager extends PlaceboJsonReloadListener<Affix> {

	public static final AffixManager INSTANCE = new AffixManager();

	private Multimap<AffixType, Affix> byType = ImmutableMultimap.of();

	public AffixManager() {
		super(AdventureModule.LOGGER, "affixes", true, true);
	}

	@Override
	protected void beginReload() {
		super.beginReload();
	}

	@Override
	protected void onReload() {
		super.onReload();
		ImmutableMultimap.Builder<AffixType, Affix> builder = ImmutableMultimap.builder();
		this.registry.values().forEach(a -> builder.put(a.type, a));
		byType = builder.build();
		Preconditions.checkArgument(Affixes.SOCKET.get() instanceof SocketAffix, "Socket Affix not registered!");
		Preconditions.checkArgument(Affixes.DURABLE.get() instanceof DurableAffix, "Durable Affix not registered!");
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(Apotheosis.loc("attribute"), PSerializer.fromCodec("Attribute Affix", AttributeAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("mob_effect"), PSerializer.fromCodec("Potion Affix", PotionAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("damage_reduction"), PSerializer.fromCodec("Damage Reduction Affix", DamageReductionAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("catalyzing"), PSerializer.fromCodec("Catalyzing Affix", CatalyzingAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("cleaving"), PSerializer.fromCodec("Cleaving Affix", CleavingAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("enlightened"), PSerializer.fromCodec("Enlightened Affix", EnlightenedAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("executing"), PSerializer.fromCodec("Executing Affix", ExecutingAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("festive"), PSerializer.fromCodec("Festive Affix", FestiveAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("magical"), PSerializer.fromCodec("Magical Affix", MagicalArrowAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("omnetic"), PSerializer.fromCodec("Omnetic Affix", OmneticAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("psychic"), PSerializer.fromCodec("Psychic Affix", PsychicAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("radial"), PSerializer.fromCodec("Radial Affix", RadialAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("retreating"), PSerializer.fromCodec("Retreating Affix", RetreatingAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("spectral"), PSerializer.fromCodec("Spectral Affix", SpectralShotAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("telepathic"), PSerializer.fromCodec("Telepathic Affix", TelepathicAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("thunderstruck"), PSerializer.fromCodec("Thunderstruck Affix", ThunderstruckAffix.CODEC));
		this.registerSerializer(Apotheosis.loc("socket"), PSerializer.builtin("Socket Affix", SocketAffix::new));
		this.registerSerializer(Apotheosis.loc("durable"), PSerializer.builtin("Durable Affix", DurableAffix::new));
	}

	public Multimap<AffixType, Affix> getTypeMap() {
		return byType;
	}

}
