package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.util.FloatValueRange;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class RangedAffix extends Affix {

	protected final FloatValueRange range;

	public RangedAffix(LootRarity rarity, FloatValueRange range, int weight) {
		super(rarity, weight);
		this.range = range;
	}

	public RangedAffix(LootRarity rarity, float min, float max, int weight)	{
		this(rarity, new FloatValueRange(min, max), weight);
	}

	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = range.getRandomValue(rand);
		if (modifier != null) return modifier.editLevel(lvl, range.getMin(), range.getMax());
		return Mth.clamp(lvl, range.getMin(), range.getMax());
	}

}