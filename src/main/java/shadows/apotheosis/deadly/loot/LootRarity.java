package shadows.apotheosis.deadly.loot;

import java.util.Random;

import net.minecraft.util.text.TextFormatting;

/**
 * Represents a tier of loot.  Each tier is stronger than the last.
 * Common Items may receive either a "weak" prefix or suffix, or nothing.
 * Uncommon Items will receive a prefix or suffix.
 * Rare Items will receive a prefix and suffix.
 * Epic Items will receive an epic affix, a prefix, a suffix, and an affix modifier.
 * Mythic Items will receive all that an epic item receives, with another affix modifier, that may be on the epic affix.
 * Ancient Items are special rare items that have their own properties.  They may also roll affixes and modifiers, depending on the unique.
 */
public enum LootRarity {
	COMMON(TextFormatting.GRAY),
	UNCOMMON(TextFormatting.YELLOW),
	RARE(TextFormatting.BLUE),
	EPIC(TextFormatting.DARK_PURPLE),
	MYTHIC(TextFormatting.DARK_GREEN),
	ANCIENT(TextFormatting.GOLD);

	final TextFormatting color;

	private LootRarity(TextFormatting color) {
		this.color = color;
	}

	public TextFormatting getColor() {
		return color;
	}

	/**
	 * Chance table: <br>
	 * Rarity      | Chance <br>
	 * Common      | 40% <br>
	 * Uncommon    | 30% <br>
	 * Rare        | 18% <br>
	 * Epic        | 7% <br>
	 * Mythic      | 4% <br>
	 * Ancient     | 1% <br>
	 * @return A random loot rarity, offset by the given min value.
	 */
	public static LootRarity random(Random rand, int min) {
		int range = min + rand.nextInt(1000 - min);
		if (range < 400) {
			return COMMON;
		} else if (range < 700) {
			return UNCOMMON;
		} else if (range < 880) {
			return RARE;
		} else if (range < 950) {
			return EPIC;
		} else if (range < 1000) {
			return MYTHIC;
		} else {
			return ANCIENT; //Temporarily disabled, so currently Mythic is 5%
		}
	}

	public static LootRarity random(Random rand) {
		return random(rand, 0);
	}
}
