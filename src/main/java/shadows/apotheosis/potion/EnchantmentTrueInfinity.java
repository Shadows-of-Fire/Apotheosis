package shadows.apotheosis.potion;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import shadows.apotheosis.Apotheosis;

public class EnchantmentTrueInfinity extends Enchantment {

	protected EnchantmentTrueInfinity() {
		super(Rarity.VERY_RARE, EnchantmentType.BOW, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return Apotheosis.enableEnch ? 65 : 31;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return 135;
	}

	@Override
	public ITextComponent getDisplayName(int level) {
		return super.getDisplayName(level).applyTextStyle(TextFormatting.DARK_GREEN);
	}
}
