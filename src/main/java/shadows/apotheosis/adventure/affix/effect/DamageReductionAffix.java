package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.json.PSerializer;
import shadows.placebo.util.StepFunction;

public class DamageReductionAffix extends Affix {

	//Formatter::off
	public static final Codec<DamageReductionAffix> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			DamageType.CODEC.fieldOf("damage_type").forGetter(a -> a.type),
			GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
			LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
			.apply(inst, DamageReductionAffix::new)
		);
	//Formatter::on
	public static final PSerializer<DamageReductionAffix> SERIALIZER = PSerializer.fromCodec("Damage Reduction Affix", CODEC);

	protected final DamageType type;
	protected final Map<LootRarity, StepFunction> values;
	protected final Set<LootCategory> types;

	public DamageReductionAffix(DamageType type, Map<LootRarity, StepFunction> levelFuncs, Set<LootCategory> types) {
		super(AffixType.ABILITY);
		this.type = type;
		this.values = levelFuncs;
		this.types = types;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
		return !cat.isNone() && (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		var comp = Component.translatable("affix.apotheosis:damage_reduction.desc", Component.translatable("misc.apotheosis." + this.type.id), fmt(100 * this.getTrueLevel(rarity, level)));
		list.accept(comp);
	}

	@Override
	public float onHurt(ItemStack stack, LootRarity rarity, float level, DamageSource src, LivingEntity ent, float amount) {
		if (src.isBypassInvul() || src.isBypassMagic()) return amount;
		if (this.type.test(src)) return amount * (1 - this.getTrueLevel(rarity, level));
		return super.onHurt(stack, rarity, level, src, ent, amount);
	}

	private float getTrueLevel(LootRarity rarity, float level) {
		return this.values.get(rarity).get(level);
	}

	@Override
	public PSerializer<? extends Affix> getSerializer() {
		return SERIALIZER;
	}

	public static enum DamageType implements Predicate<DamageSource> {
		PHYSICAL("physical", d -> !d.isMagic() && !d.isFire() && !d.isExplosion() && !d.isFall()),
		MAGIC("magic", DamageSource::isMagic),
		FIRE("fire", DamageSource::isFire),
		FALL("fall", DamageSource::isFall),
		EXPLOSION("explosion", DamageSource::isExplosion);

		public static Codec<DamageType> CODEC = new EnumCodec<>(DamageType.class);

		private final String id;
		private final Predicate<DamageSource> predicate;

		private DamageType(String id, Predicate<DamageSource> predicate) {
			this.id = id;
			this.predicate = predicate;
		}

		public String getId() {
			return this.id;
		}

		@Override
		public boolean test(DamageSource t) {
			return this.predicate.test(t);
		}
	}

}
