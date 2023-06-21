package shadows.apotheosis.adventure.loot;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.SimpleMapCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.color.GradientColor;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.TypeKeyed.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

public class LootRarity implements ILuckyWeighted, Comparable<LootRarity> {

	public static final List<LootRarity> LIST;
	public static final Map<String, LootRarity> BY_ID;

	public static final Codec<LootRarity> CODEC = ExtraCodecs.stringResolverCodec(LootRarity::id, LootRarity::byId);

	//Formatter::off
	public static final LootRarity COMMON = new LootRarity("common", 0x808080, 0, 400, 0, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.25F)
	));

	public static final LootRarity UNCOMMON = new LootRarity("uncommon", 0x33FF33, 1, 320, 1.5F, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.45F, new LootRule(AffixType.ABILITY, 0.25F)),
			new LootRule(AffixType.SOCKET, 0.45F)
	));

	public static final LootRarity RARE = new LootRarity("rare", 0x5555FF, 2, 150, 3, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.25F)),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 0.33F),
			new LootRule(AffixType.SOCKET, 0.65F),
			new LootRule(AffixType.SOCKET, 0.45F),
			new LootRule(AffixType.DURABILITY, 0.1F)
	));

	public static final LootRarity EPIC = new LootRarity("epic", 0xBB00BB, 3, 90, 4.5F, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.45F)),
			new LootRule(AffixType.STAT, 0.5F, new LootRule(AffixType.ABILITY, 0.33F)),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 0.65F),
			new LootRule(AffixType.SOCKET, 0.85F),
			new LootRule(AffixType.SOCKET, 0.65F),
			new LootRule(AffixType.SOCKET, 0.45F),
			new LootRule(AffixType.DURABILITY, 0.3F)
	));

	public static final LootRarity MYTHIC = new LootRarity("mythic", 0xED7014, 4, 40, 6, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.5F)),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.4F)),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 0.3F),
			new LootRule(AffixType.SOCKET, 1F),
			new LootRule(AffixType.SOCKET, 0.85F),
			new LootRule(AffixType.SOCKET, 0.65F),
			new LootRule(AffixType.DURABILITY, 0.5F)
	));

	public static final LootRarity ANCIENT = new LootRarity("ancient", GradientColor.RAINBOW, 5, 0, 0, ImmutableList.of(
			new LootRule(AffixType.ANCIENT, 1), 
			new LootRule(AffixType.STAT, 1), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.7F)), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.6F)), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.5F)), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.4F)), 
			new LootRule(AffixType.ABILITY, 1), new LootRule(AffixType.ABILITY, 1), 
			new LootRule(AffixType.ABILITY, 0.75F), 
			new LootRule(AffixType.ABILITY, 0.45F), 
			new LootRule(AffixType.SOCKET, 1F), 
			new LootRule(AffixType.SOCKET, 0.85F), 
			new LootRule(AffixType.SOCKET, 0.65F), 
			new LootRule(AffixType.SOCKET, 0.45F), 
			new LootRule(AffixType.SOCKET, 0.25F), 
			new LootRule(AffixType.DURABILITY, 0.75F)
	));

	//Formatter::on

	static {
		LIST = ImmutableList.of(COMMON, UNCOMMON, RARE, EPIC, MYTHIC, ANCIENT);
		BY_ID = ImmutableMap.copyOf(LIST.stream().collect(Collectors.toMap(LootRarity::id, Function.identity())));
	}

	private final String id;
	private final TextColor color;
	private final int ordinal;

	private int weight;
	private float quality;
	private List<LootRule> rules;

	private LootRarity(String id, TextColor color, int ordinal, int weight, float quality, List<LootRule> rules) {
		this.id = id;
		this.color = color;
		this.ordinal = ordinal;
		this.weight = weight;
		this.quality = quality;
		this.rules = rules;
	}

	private LootRarity(String id, int color, int ordinal, int weight, float quality, List<LootRule> rules) {
		this(id, TextColor.fromRgb(color), ordinal, weight, quality, rules);
	}

	public String id() {
		return this.id;
	}

	public TextColor color() {
		return this.color;
	}

	public int ordinal() {
		return this.ordinal;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	public List<LootRule> rules() {
		return this.rules;
	}

	public LootRarity prev() {
		if (this == COMMON) return this;
		return LIST.get(this.ordinal - 1);
	}

	public LootRarity next() {
		if (this == ANCIENT) return this;
		return LIST.get(this.ordinal + 1);
	}

	/**
	 * Checks if this rarity is the same or worse than the passed rarity.
	 */
	public boolean isAtMost(LootRarity other) {
		return this.ordinal() <= other.ordinal();
	}

	/**
	 * Checks if this rarity is the same or better than the passed rarity.
	 */
	public boolean isAtLeast(LootRarity other) {
		return this.ordinal() >= other.ordinal();
	}

	/**
	 * Returns the minimum (worst) rarity between a and b.
	 */
	public static LootRarity min(LootRarity a, @Nullable LootRarity b) {
		if (b == null) return a;
		return a.ordinal <= b.ordinal ? a : b;
	}

	/**
	 * Returns the maximum (best) rarity between a and b.
	 */
	public static LootRarity max(LootRarity a, @Nullable LootRarity b) {
		if (b == null) return a;
		return a.ordinal >= b.ordinal ? a : b;
	}

	/**
	 * Returns true if the passed item is a rarity material.
	 */
	public static boolean isRarityMat(ItemStack stack) {
		return AdventureModule.RARITY_MATERIALS.containsValue(stack.getItem());
	}

	@Nullable
	public static LootRarity getMaterialRarity(ItemStack stack) {
		return AdventureModule.RARITY_MATERIALS.inverse().get(stack.getItem());
	}

	public ItemStack getMaterial() {
		return new ItemStack(AdventureModule.RARITY_MATERIALS.get(this));
	}

	/**
	 * Clamps a loot rarity to within a min/max bound.
	 * @param lowerBound The minimum valid rarity
	 * @param upperBound The maximum valid rarity
	 * @return This, if this is within the bounds, or the min or max if it exceeded that bound.
	 */
	public LootRarity clamp(@Nullable LootRarity lowerBound, @Nullable LootRarity upperBound) {
		return LootRarity.max(LootRarity.min(this, upperBound), lowerBound);
	}

	public Component toComponent() {
		return Component.translatable("rarity.apoth." + this.id).withStyle(Style.EMPTY.withColor(this.color));
	}

	void update(RarityStub stub) {
		this.weight = stub.weight;
		this.quality = stub.quality;
		this.rules = ImmutableList.copyOf(stub.rules);
	}

	@Override
	public String toString() {
		return "LootRarity{" + this.id + "}";
	}

	@Nullable
	public static LootRarity byId(String id) {
		return BY_ID.get(id.toLowerCase(Locale.ROOT));
	}

	public static Set<String> ids() {
		return BY_ID.keySet();
	}

	public static List<LootRarity> values() {
		return LIST;
	}

	public static LootRarity random(RandomSource rand, float luck) {
		return random(rand, luck, null, null);
	}

	public static LootRarity random(RandomSource rand, float luck, @Nullable Clamped item) {
		if (item == null) return random(rand, luck);
		return random(rand, luck, item.getMinRarity(), item.getMaxRarity());
	}

	public static LootRarity random(RandomSource rand, float luck, @Nullable LootRarity min, @Nullable LootRarity max) {
		List<Wrapper<LootRarity>> list = LIST.stream().filter(r -> r.clamp(min, max) == r).map(r -> r.<LootRarity>wrap(luck)).toList();
		return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).get();
	}

	public static <T> SimpleMapCodec<LootRarity, T> mapCodec(Codec<T> codec) {
		return Codec.simpleMap(LootRarity.CODEC, codec, Keyable.forStrings(() -> LootRarity.values().stream().map(LootRarity::id)));
	}

	public static class RarityStub extends TypeKeyedBase<RarityStub> implements ILuckyWeighted {
		//Formatter::off
		public static final Codec<RarityStub> CODEC = RecordCodecBuilder.create(inst -> 
			inst.group(
				Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("quality", 0F).forGetter(ILuckyWeighted::getQuality),
				new ListCodec<>(LootRule.CODEC).fieldOf("rules").forGetter(RarityStub::rules))
				.apply(inst, RarityStub::new)
			);
		//Formatter::on
		public static final PSerializer<RarityStub> SERIALIZER = PSerializer.fromCodec("Loot Rarity", CODEC);

		int weight;
		float quality;
		List<LootRule> rules;

		public RarityStub(int weight, float quality, List<LootRule> rules) {
			this.weight = weight;
			this.quality = quality;
			this.rules = rules;
		}

		@Override
		public int getWeight() {
			return this.weight;
		}

		@Override
		public float getQuality() {
			return this.quality;
		}

		public List<LootRule> rules() {
			return this.rules;
		}

		@Override
		public PSerializer<? extends RarityStub> getSerializer() {
			return SERIALIZER;
		}

	}

	public static record LootRule(AffixType type, float chance, @Nullable LootRule backup) {

		//Formatter::off
		public static final Codec<LootRule> CODEC = RecordCodecBuilder.create(inst -> 
			inst.group(
				new EnumCodec<>(AffixType.class).fieldOf("type").forGetter(LootRule::type),
				Codec.FLOAT.fieldOf("chance").forGetter(LootRule::chance),
				ExtraCodecs.lazyInitializedCodec(() -> LootRule.CODEC).optionalFieldOf("backup").forGetter(rule -> Optional.ofNullable(rule.backup())))
				.apply(inst, LootRule::new)
			);
		//Formatter::on

		private static Random jRand = new Random();

		public LootRule(AffixType type, float chance) {
			this(type, chance, Optional.empty());
		}

		public LootRule(AffixType type, float chance, Optional<LootRule> backup) {
			this(type, chance, backup.orElse(null));
		}

		public void execute(ItemStack stack, LootRarity rarity, Set<Affix> currentAffixes, MutableInt sockets, RandomSource rand) {
			if (this.type == AffixType.DURABILITY) return;
			if (rand.nextFloat() <= this.chance) {
				if (this.type == AffixType.SOCKET) {
					sockets.add(1);
					return;
				}
				List<Affix> available = AffixHelper.byType(this.type).stream().filter(a -> a.canApplyTo(stack, LootCategory.forItem(stack), rarity) && !currentAffixes.contains(a)).collect(Collectors.toList());
				if (available.size() == 0) {
					if (backup != null) backup.execute(stack, rarity, currentAffixes, sockets, rand);
					else AdventureModule.LOGGER.error("Failed to execute LootRule {}/{}/{}/{}!", ForgeRegistries.ITEMS.getKey(stack.getItem()), rarity.id(), this.type, this.chance);
					return;
				}
				jRand.setSeed(rand.nextLong());
				Collections.shuffle(available, jRand);
				currentAffixes.add(available.get(0));
			}
		}
	}

	public static interface Clamped {

		public LootRarity getMinRarity();

		public LootRarity getMaxRarity();

		default LootRarity clamp(@Nullable LootRarity rarity) {
			if (rarity == null) return getMinRarity();
			return rarity.clamp(getMinRarity(), getMaxRarity());
		}

		public static record Impl(LootRarity min, LootRarity max) implements Clamped {

			@Override
			public LootRarity getMinRarity() {
				return min;
			}

			@Override
			public LootRarity getMaxRarity() {
				return max;
			}

		}

	}

	@Override
	public int compareTo(LootRarity o) {
		return Integer.compare(this.ordinal, o.ordinal);
	}
}