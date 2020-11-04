package shadows.apotheosis.deadly.affix.impl.melee;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Slain monsters have a chance to explode into a loot pinata.
 */
public class LootPinataAffix extends RangedAffix {

	public LootPinataAffix(int weight) {
		super(0.001F, 0.02F, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SWORD;
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", String.format("%.2f", lvl * 100)));
		return lvl;
	}

	@Override
	public float getMax() {
		return 0.03F;
	}
}