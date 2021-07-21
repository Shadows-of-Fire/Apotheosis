package shadows.apotheosis.potion;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import shadows.apotheosis.Apotheosis;

public class TrueInfinityEnchant extends Enchantment {

	protected TrueInfinityEnchant() {
		super(Rarity.VERY_RARE, EnchantmentType.BOW, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return Apotheosis.enableEnch ? 65 : 31;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 200;
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_GREEN);
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return super.checkCompatibility(ench) && ench != Enchantments.INFINITY_ARROWS;
	}
}