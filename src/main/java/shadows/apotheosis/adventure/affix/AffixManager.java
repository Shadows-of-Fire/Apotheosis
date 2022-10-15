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
import shadows.placebo.json.SerializerBuilder;

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
		this.registerSerializer(Apotheosis.loc("attribute"), new SerializerBuilder<Affix>("Attribute Affix").autoRegister(AttributeAffix.class));
		this.registerSerializer(Apotheosis.loc("mob_effect"), new SerializerBuilder<Affix>("Potion Affix").autoRegister(PotionAffix.class));
		this.registerSerializer(Apotheosis.loc("damage_reduction"), new SerializerBuilder<Affix>("Damage Reduction Affix").autoRegister(DamageReductionAffix.class));
		this.registerSerializer(Apotheosis.loc("catalyzing"), new SerializerBuilder<Affix>("Catalyzing Affix").autoRegister(CatalyzingAffix.class));
		this.registerSerializer(Apotheosis.loc("cleaving"), new SerializerBuilder<Affix>("Cleaving Affix").autoRegister(CleavingAffix.class));
		this.registerSerializer(Apotheosis.loc("enlightened"), new SerializerBuilder<Affix>("Enlightened Affix").autoRegister(EnlightenedAffix.class));
		this.registerSerializer(Apotheosis.loc("executing"), new SerializerBuilder<Affix>("Executing Affix").autoRegister(ExecutingAffix.class));
		this.registerSerializer(Apotheosis.loc("festive"), new SerializerBuilder<Affix>("Festive Affix").autoRegister(FestiveAffix.class));
		this.registerSerializer(Apotheosis.loc("magical"), new SerializerBuilder<Affix>("Magical Affix").autoRegister(MagicalArrowAffix.class));
		this.registerSerializer(Apotheosis.loc("omnetic"), new SerializerBuilder<Affix>("Omnetic Affix").autoRegister(OmneticAffix.class));
		this.registerSerializer(Apotheosis.loc("psychic"), new SerializerBuilder<Affix>("Psychic Affix").autoRegister(PsychicAffix.class));
		this.registerSerializer(Apotheosis.loc("radial"), new SerializerBuilder<Affix>("Radial Affix").autoRegister(RadialAffix.class));
		this.registerSerializer(Apotheosis.loc("retreating"), new SerializerBuilder<Affix>("Retreating Affix").autoRegister(RetreatingAffix.class));
		this.registerSerializer(Apotheosis.loc("spectral"), new SerializerBuilder<Affix>("Spectral Affix").autoRegister(SpectralShotAffix.class));
		this.registerSerializer(Apotheosis.loc("telepathic"), new SerializerBuilder<Affix>("Telepathic Affix").autoRegister(TelepathicAffix.class));
		this.registerSerializer(Apotheosis.loc("thunderstruck"), new SerializerBuilder<Affix>("Thunderstruck Affix").autoRegister(ThunderstruckAffix.class));
		this.registerSerializer(Apotheosis.loc("socket"), new SerializerBuilder<Affix>("Socket Affix").builtin(SocketAffix::new));
		this.registerSerializer(Apotheosis.loc("durable"), new SerializerBuilder<Affix>("Durable Affix").builtin(DurableAffix::new));
	}

	public Multimap<AffixType, Affix> getTypeMap() {
		return byType;
	}

}
