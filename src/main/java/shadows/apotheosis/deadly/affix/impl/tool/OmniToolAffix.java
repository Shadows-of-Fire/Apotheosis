package shadows.apotheosis.deadly.affix.impl.tool;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Allows this tool to mine anything that a diamond shovel/axe/pickaxe could.
 */
public class OmniToolAffix extends Affix {

	@Override
	public boolean isPrefix() {
		return true;
	}

	public OmniToolAffix(LootRarity rarity, int weight) {
		super(rarity, weight);
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return 1;
	}

	@Override
	public boolean canApply(LootCategory lootCategory) { return lootCategory == LootCategory.BREAKER; }

}