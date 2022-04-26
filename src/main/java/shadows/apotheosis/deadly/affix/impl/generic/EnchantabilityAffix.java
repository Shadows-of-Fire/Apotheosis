package shadows.apotheosis.deadly.affix.impl.generic;

import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.util.FloatValueRange;

import javax.annotation.Nullable;
import java.util.Random;

public class EnchantabilityAffix extends RangedAffix {

	public EnchantabilityAffix(LootRarity rarity, int min, int max, int weight) {
		super(rarity, new FloatValueRange(min, max), weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return Math.round(super.generateLevel(stack, rand, modifier));
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return Math.round(super.upgradeLevel(curLvl, newLvl));
	}

	@Override
	public boolean isPrefix() {
		return false;
	}

	@Override
	public boolean canApply(LootCategory category) {
		return true;
	}

}