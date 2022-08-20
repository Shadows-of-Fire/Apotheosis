package shadows.apotheosis.adventure.loot;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.placebo.PlaceboClient.RainbowColor;

public record LootRarity(String id, TextColor color, List<LootRule> rules, int ordinal) {

	public static final Map<String, LootRarity> BY_ID;

	//Formatter::off
	public static final LootRarity COMMON = new LootRarity("common", 0x808080, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.25F)
	));

	public static final LootRarity UNCOMMON = new LootRarity("uncommon", 0x33FF33, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.45F, new LootRule(AffixType.EFFECT, 0.25F)),
			new LootRule(AffixType.SOCKET, 0.2F)
	));

	public static final LootRarity RARE = new LootRarity("rare", 0x5555FF, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.EFFECT, 0.25F)),
			new LootRule(AffixType.EFFECT, 1),
			new LootRule(AffixType.EFFECT, 0.33F),
			new LootRule(AffixType.SOCKET, 0.33F),
			new LootRule(AffixType.DURABILITY, 0.1F)
	));

	public static final LootRarity EPIC = new LootRarity("epic", 0xBB00BB, ImmutableList.of(
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

	public static final LootRarity MYTHIC = new LootRarity("mythic", 0xED7014, ImmutableList.of(
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

	public static final LootRarity ANCIENT = new LootRarity("ancient", new RainbowColor(), ImmutableList.of());

	static {
		BY_ID = ImmutableMap.<String, LootRarity>builder().put(COMMON.id, COMMON).put(UNCOMMON.id, UNCOMMON).put(RARE.id, RARE).put(EPIC.id, EPIC).put(MYTHIC.id, MYTHIC).put(ANCIENT.id, ANCIENT).build();
	}

	private static int num = 0;

	public LootRarity(String id, TextColor color, List<LootRule> rules) {
		this(id, color, rules, num++);
	}

	public LootRarity(String id, int color, List<LootRule> rules) {
		this(id, TextColor.fromRgb(color), rules);
	}

	public boolean isAtLeast(LootRarity other) {
		return this.ordinal() >= other.ordinal();
	}

	public Component toComponent() {
		return new TranslatableComponent("rarity.apoth." + this.id).withStyle(Style.EMPTY.withColor(this.color));
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
	public static LootRarity random(Random rand, int min) {
		int range = min + rand.nextInt(1000 - min);
		if (range < AdventureConfig.rarityThresholds[0]) {
			return COMMON;
		} else if (range < AdventureConfig.rarityThresholds[1]) {
			return UNCOMMON;
		} else if (range < AdventureConfig.rarityThresholds[2]) {
			return RARE;
		} else if (range < AdventureConfig.rarityThresholds[3]) {
			return EPIC;
		} else if (range < AdventureConfig.rarityThresholds[4]) {
			return MYTHIC;
		} else {
			return MYTHIC; // ANCIENT;
		}
	}

	public static LootRarity random(Random rand) {
		return random(rand, 0);
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