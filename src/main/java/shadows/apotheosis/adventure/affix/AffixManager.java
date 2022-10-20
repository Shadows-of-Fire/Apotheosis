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
		this.registerSerializer(Apotheosis.loc("attribute"), PSerializer.autoRegister("Attribute Affix", AttributeAffix.class));
		this.registerSerializer(Apotheosis.loc("mob_effect"), PSerializer.autoRegister("Potion Affix", PotionAffix.class));
		this.registerSerializer(Apotheosis.loc("damage_reduction"), PSerializer.autoRegister("Damage Reduction Affix", DamageReductionAffix.class));
		this.registerSerializer(Apotheosis.loc("catalyzing"), PSerializer.autoRegister("Catalyzing Affix", CatalyzingAffix.class));
		this.registerSerializer(Apotheosis.loc("cleaving"), PSerializer.autoRegister("Cleaving Affix", CleavingAffix.class));
		this.registerSerializer(Apotheosis.loc("enlightened"), PSerializer.autoRegister("Enlightened Affix", EnlightenedAffix.class));
		this.registerSerializer(Apotheosis.loc("executing"), PSerializer.autoRegister("Executing Affix", ExecutingAffix.class));
		this.registerSerializer(Apotheosis.loc("festive"), PSerializer.autoRegister("Festive Affix", FestiveAffix.class));
		this.registerSerializer(Apotheosis.loc("magical"), PSerializer.autoRegister("Magical Affix", MagicalArrowAffix.class));
		this.registerSerializer(Apotheosis.loc("omnetic"), PSerializer.autoRegister("Omnetic Affix", OmneticAffix.class));
		this.registerSerializer(Apotheosis.loc("psychic"), PSerializer.autoRegister("Psychic Affix", PsychicAffix.class));
		this.registerSerializer(Apotheosis.loc("radial"), PSerializer.autoRegister("Radial Affix", RadialAffix.class));
		this.registerSerializer(Apotheosis.loc("retreating"), PSerializer.autoRegister("Retreating Affix", RetreatingAffix.class));
		this.registerSerializer(Apotheosis.loc("spectral"), PSerializer.autoRegister("Spectral Affix", SpectralShotAffix.class));
		this.registerSerializer(Apotheosis.loc("telepathic"), PSerializer.autoRegister("Telepathic Affix", TelepathicAffix.class));
		this.registerSerializer(Apotheosis.loc("thunderstruck"), PSerializer.autoRegister("Thunderstruck Affix", ThunderstruckAffix.class));
		this.registerSerializer(Apotheosis.loc("socket"), PSerializer.builtin("Socket Affix", SocketAffix::new));
		this.registerSerializer(Apotheosis.loc("durable"), PSerializer.builtin("Durable Affix", DurableAffix::new));
	}

	public Multimap<AffixType, Affix> getTypeMap() {
		return byType;
	}

}
