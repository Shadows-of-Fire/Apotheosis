package shadows.apotheosis.adventure.loot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.placebo.PlaceboClient.RainbowColor;

public record LootRarity(int defaultWeight, String id, TextColor color, List<LootRule> rules, int ordinal) {

	public static final List<LootRarity> LIST;
	public static final Map<String, LootRarity> BY_ID;
	public static final Map<LootRarity, float[]> WEIGHTS = new HashMap<>();

	//Formatter::off
	public static final LootRarity COMMON = new LootRarity(400, "common", 0x808080, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.25F)
	));

	public static final LootRarity UNCOMMON = new LootRarity(320, "uncommon", 0x33FF33, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.45F, new LootRule(AffixType.EFFECT, 0.25F)),
			new LootRule(AffixType.SOCKET, 0.2F)
	));

	public static final LootRarity RARE = new LootRarity(150, "rare", 0x5555FF, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.EFFECT, 0.25F)),
			new LootRule(AffixType.EFFECT, 1),
			new LootRule(AffixType.EFFECT, 0.33F),
			new LootRule(AffixType.SOCKET, 0.33F),
			new LootRule(AffixType.DURABILITY, 0.1F)
	));

	public static final LootRarity EPIC = new LootRarity(90, "epic", 0xBB00BB, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.EFFECT, 0.45F)),
			new LootRule(AffixType.STAT, 0.5F, new LootRule(AffixType.EFFECT, 0.33F)),
			new LootRule(AffixType.EFFECT, 1),
			new LootRule(AffixType.EFFECT, 0.65F),
			new LootRule(AffixType.SOCKET, 0.5F),
			new LootRule(AffixType.SOCKET, 0.33F),
			new LootRule(AffixType.DURABILITY, 0.3F)
	));

	public static final LootRarity MYTHIC = new LootRarity(40, "mythic", 0xED7014, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.EFFECT, 0.5F)),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.EFFECT, 0.4F)),
			new LootRule(AffixType.EFFECT, 1),
			new LootRule(AffixType.EFFECT, 1),
			new LootRule(AffixType.EFFECT, 0.3F),
			new LootRule(AffixType.SOCKET, 0.5F),
			new LootRule(AffixType.SOCKET, 0.45F),
			new LootRule(AffixType.SOCKET, 0.4F),
			new LootRule(AffixType.DURABILITY, 0.5F)
	));
	//Formatter::on

	public static final LootRarity ANCIENT = new LootRarity(0, "ancient", new RainbowColor(), ImmutableList.of());

	static {
		LIST = ImmutableList.of(COMMON, UNCOMMON, RARE, EPIC, MYTHIC, ANCIENT);
		BY_ID = ImmutableMap.copyOf(LIST.stream().collect(Collectors.toMap(LootRarity::id, Function.identity())));
	}

	private static int num = 0;

	public LootRarity(int defaultWeight, String id, TextColor color, List<LootRule> rules) {
		this(defaultWeight, id, color, rules, num++);
	}

	public LootRarity(int defaultWeight, String id, int color, List<LootRule> rules) {
		this(defaultWeight, id, TextColor.fromRgb(color), rules);
	}

	public boolean isAtLeast(LootRarity other) {
		return this.ordinal() >= other.ordinal();
	}

	/**
	 * Returns the minimum (worst) rarity between this and other.
	 */
	public LootRarity min(@Nullable LootRarity other) {
		if (other == null) return this;
		return this.ordinal <= other.ordinal ? this : other;
	}

	/**
	 * Returns the maximum (best) rarity between this and other.
	 */
	public LootRarity max(@Nullable LootRarity other) {
		if (other == null) return this;
		return this.ordinal >= other.ordinal ? this : other;
	}

	/**
	 * Clamps a loot rarity to within a min/max bound.
	 * @param lowerBound The minimum valid rarity
	 * @param upperBound The maximum valid rarity
	 * @return This, if this is within the bounds, or the min or max if it exceeded that bound.
	 */
	public LootRarity clamp(LootRarity lowerBound, LootRarity upperBound) {
		return this.min(upperBound).max(lowerBound);
	}

	public Component toComponent() {
		return new TranslatableComponent("rarity.apoth." + this.id).withStyle(Style.EMPTY.withColor(this.color));
	}

	public int getModifiedWeight(float luck) {
		return (int) (WEIGHTS.get(this)[0] + WEIGHTS.get(this)[1] * luck);
	}

	public static LootRarity byId(String id) {
		return BY_ID.get(id);
	}

	public static Set<String> ids() {
		return BY_ID.keySet();
	}

	public static Collection<LootRarity> values() {
		return BY_ID.values();
	}

	public static LootRarity random(Random rand, float luck) {
		return random(rand, luck, null, null);
	}

	public static LootRarity random(Random rand, float luck, LootRarity min) {
		return random(rand, luck, min, null);
	}

	/**
	 * Default Chance Table: <br>
	 * Rarity      | Chance <br>
	 * Common      | 40% <br>
	 * Uncommon    | 30% <br>
	 * Rare        | 15% <br>
	 * Epic        | 9% <br>
	 * Mythic      | 5.5% <br>
	 * Ancient     | 0.5% <br>
	 * @return A random loot rarity, offset by the given min value.
	 */
	public static LootRarity random(Random rand, float luck, @Nullable LootRarity min, @Nullable LootRarity max) {
		List<Wrapper<LootRarity>> list = LIST.stream().map(r -> WeightedEntry.wrap(r, r.getModifiedWeight(luck))).toList();
		return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).map(l -> l.clamp(min, max)).get();
	}

	public static record LootRule(AffixType type, float chance, @Nullable LootRule backup) {

		public LootRule(AffixType type, float chance) {
			this(type, chance, null);
		}

		public void execute(ItemStack stack, LootRarity rarity, Set<Affix> currentAffixes, MutableInt sockets, Random rand) {
			if (this.type == AffixType.DURABILITY) return;
			if (rand.nextFloat() <= this.chance) {
				if (this.type == AffixType.SOCKET) {
					sockets.add(1);
					return;
				}
				List<Affix> available = AffixHelper.byType(this.type).stream().filter(a -> a.canApplyTo(stack, rarity) && !currentAffixes.contains(a)).collect(Collectors.toList());
				if (available.size() == 0) {
					if (backup != null) backup.execute(stack, rarity, currentAffixes, sockets, rand);
					else AdventureModule.LOGGER.error("Failed to execute LootRule {}/{}/{}/{}!", stack.getItem().getRegistryName(), rarity.id(), this.type, this.chance);
					return;
				}
				Collections.shuffle(available, rand);
				currentAffixes.add(available.get(0));
			}
		}

	}
}