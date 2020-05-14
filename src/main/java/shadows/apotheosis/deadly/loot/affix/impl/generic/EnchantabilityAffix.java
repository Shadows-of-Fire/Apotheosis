package shadows.apotheosis.deadly.loot.affix.impl.generic;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

public class EnchantabilityAffix extends Affix {

	public EnchantabilityAffix(int weight) {
		super(weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		int ench = 5 + rand.nextInt(15);
		if (modifier != null) ench = (int) modifier.editLevel(this, ench);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", ench));
		return ench;
	}

	@Override
	public float getMin() {
		return 5;
	}

	@Override
	public float getMax() {
		return 30;
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return true;
	}

}
