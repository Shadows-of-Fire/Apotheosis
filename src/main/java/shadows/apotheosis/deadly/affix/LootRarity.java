package shadows.apotheosis.deadly.affix;

import java.util.Random;

import net.minecraft.util.text.TextFormatting;

/**
 * Represents a tier of loot.  Each tier is stronger than the last.
 * Common Items will receive an affix with it's effect cut in half.
 * Uncommon Items will receive one affix.
 * Rare Items will receive two affixes.
 * Epic Items will receive three affixes, with a chance for affix modifiers.
 * Mythic Items will receive three affixes, with a higher chance for modifiers, and become unbreakable.
 * Ancient Items are special rare items that have their own properties.  They may also roll affixes and modifiers, depending on the unique.
 */
public enum LootRarity {
	COMMON(TextFormatting.GRAY, 1),
	UNCOMMON(TextFormatting.YELLOW, 1),
	RARE(TextFormatting.BLUE, 2),
	EPIC(TextFormatting.RED, 3),
	MYTHIC(TextFormatting.DARK_GREEN, 3),
	ANCIENT(TextFormatting.AQUA, 3);

	final TextFormatting color;
	final int affixes;

	private LootRarity(TextFormatting color, int affixes) {
		this.color = color;
		this.affixes = affixes;
	}

	public TextFormatting getColor() {
		return this.color;
	}

	public int getAffixes() {
		return this.affixes;
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