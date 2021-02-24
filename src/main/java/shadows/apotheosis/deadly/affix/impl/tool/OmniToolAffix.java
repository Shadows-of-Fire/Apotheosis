package shadows.apotheosis.deadly.affix.impl.tool;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Allows this tool to mine anything that a diamond shovel/axe/pickaxe could.
 */
public class OmniToolAffix extends Affix {

	public OmniToolAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return 1;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
		list.accept(new TranslationTextComponent("affix." + this.getRegistryName() + ".desc"));
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 1;
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL;
	}

}