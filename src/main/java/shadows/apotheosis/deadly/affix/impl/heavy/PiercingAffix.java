package shadows.apotheosis.deadly.affix.impl.heavy;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Baseline affix for all heavy weapons.  Damage is converted into armor piercing damage.
 */
public class PiercingAffix extends Affix {

	@Override
	public boolean isPrefix() {
		return true;
	}

	public PiercingAffix(LootRarity rarity, int weight) {
		super(rarity, weight);
	}

	@Override
	public boolean canApply(LootCategory type) {
		return type == LootCategory.HEAVY_WEAPON;
	}

}