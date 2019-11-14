package shadows.deadly.loot;

import java.util.Random;

import net.minecraft.util.text.TextFormatting;

/**
 * Represents a tier of loot.  A rarity will be randomly selected upon generation of a loot item.
 * Higher tiers unlock more affixes, and stronger affixes can also be selected.
 * Transcended items come with an additional designated enchantment.
 * Ancient items are unbreakable, retain the trascended enchantment
 */
public enum LootRarity {

	COMMON(TextFormatting.WHITE, 0),
	MAGICAL(TextFormatting.AQUA, 1),
	EMPOWERED(TextFormatting.YELLOW, 2),
	TRANSCENDED(TextFormatting.GREEN, 2),
	ANCIENT(TextFormatting.BLUE, 3),
	UNIQUE(TextFormatting.GOLD, 4);

	final TextFormatting color;
	final int affixes;

	private LootRarity(TextFormatting color, int affixes) {
		this.color = color;
		this.affixes = affixes;
	}

	public TextFormatting getColor() {
		return color;
	}

	/**
	 * Chance table: <br>
	 * Rarity      | Chance <br>
	 * Common      | 40% <br>
	 * Magical     | 30% <br>
	 * Empowered   | 18% <br>
	 * Transcended | 7% <br>
	 * Ancient     | 4% <br>
	 * Unique      | 1% <br>
	 * @return A random loot rarity.
	 */
	public static LootRarity random(Random rand) {
		int range = rand.nextInt(1000);
		if (range < 400) {
			return COMMON;
		} else if (range < 700) {
			return MAGICAL;
		} else if (range < 880) {
			return EMPOWERED;
		} else if (range < 950) {
			return TRANSCENDED;
		} else if (range < 990) {
			return ANCIENT;
		} else {
			return UNIQUE;
		}
	}
}
