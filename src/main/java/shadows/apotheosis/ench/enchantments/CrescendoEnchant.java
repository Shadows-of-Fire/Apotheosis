package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class CrescendoEnchant extends Enchantment {

	public CrescendoEnchant() {
		super(Rarity.RARE, EnchantmentType.CROSSBOW, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	public int getMinEnchantability(int level) {
		return 45 + (level - 1) * 20;
	}

	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 50;
	}

	@Override
	public ITextComponent getDisplayName(int level) {
		return super.getDisplayName(level).applyTextStyle(TextFormatting.DARK_GREEN);
	}
}