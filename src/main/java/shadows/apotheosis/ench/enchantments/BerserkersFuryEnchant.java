package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class BerserkersFuryEnchant extends Enchantment {

	public BerserkersFuryEnchant() {
		super(Rarity.VERY_RARE, EnchantmentType.ARMOR, new EquipmentSlotType[] { EquipmentSlotType.CHEST, EquipmentSlotType.LEGS });
	}

	@Override
	public int getMinCost(int level) {
		return 40 + level * 30;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean isCurse() {
		return true;
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_RED);
	}

}