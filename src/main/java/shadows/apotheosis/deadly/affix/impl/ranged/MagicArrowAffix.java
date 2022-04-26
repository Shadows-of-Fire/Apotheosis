package shadows.apotheosis.deadly.affix.impl.ranged;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Arrow damage is converted into magic damage.
 */
public class MagicArrowAffix extends Affix {

	@Override
	public boolean isPrefix() {
		return false;
	}

	public MagicArrowAffix(LootRarity rarity, int weight) {
		super(rarity, weight);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return 1;
	}

}