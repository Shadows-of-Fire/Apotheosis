package shadows.apotheosis.deadly.affix.impl.ranged;

import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Drops from killed enemies are teleported to the shooter.
 */
public class TeleportDropsAffix extends RangedAffix {

	public TeleportDropsAffix(LootRarity rarity, int min, int max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return (int) super.upgradeLevel(curLvl, newLvl);
	}

}