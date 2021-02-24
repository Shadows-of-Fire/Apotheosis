package shadows.apotheosis.deadly.affix.impl.generic;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

public class EnchantabilityAffix extends Affix {

	public EnchantabilityAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		int ench = 5 + rand.nextInt(15);
		if (modifier != null) ench = (int) modifier.editLevel(this, ench);
		return ench;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
		list.accept(new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", (int) level));
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