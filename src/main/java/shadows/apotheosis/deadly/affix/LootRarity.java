package shadows.apotheosis.deadly.affix;

import java.util.Random;

import net.minecraft.util.text.Color;
import shadows.apotheosis.deadly.DeadlyModuleClient.RainbowColor;
import shadows.apotheosis.deadly.config.DeadlyConfig;

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
	COMMON(0x808080, 1),
	UNCOMMON(0x33FF33, 1),
	RARE(0x5555FF, 2),
	EPIC(0xBB00BB, 3),
	MYTHIC(0xED7014, 3),
	ANCIENT(new RainbowColor(), 3);

	final Color color;
	final int affixes;

	private LootRarity(int color, int affixes) {
		this(Color.fromRgb(color), affixes);
	}

	private LootRarity(Color color, int affixes) {
		this.color = color;
		this.affixes = affixes;
	}

	public Color getColor() {
		return color;
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
		if (range < DeadlyConfig.rarityThresholds[0]) {
			return COMMON;
		} else if (range < DeadlyConfig.rarityThresholds[1]) {
			return UNCOMMON;
		} else if (range < DeadlyConfig.rarityThresholds[2]) {
			return RARE;
		} else if (range < DeadlyConfig.rarityThresholds[3]) {
			return EPIC;
		} else if (range < DeadlyConfig.rarityThresholds[4]) {
			return MYTHIC;
		} else {
			return ANCIENT; //Temporarily disabled, so currently Mythic is 5%
		}
	}

	public static LootRarity random(Random rand) {
		return random(rand, 0);
	}
}