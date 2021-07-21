package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.EfficiencyEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class MinersFervorEnchant extends EfficiencyEnchantment {

	public MinersFervorEnchant() {
		super(Rarity.RARE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 30 + (enchantmentLevel - 1) * 30;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return this.getMinCost(enchantmentLevel) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_PURPLE);
	}

	@Override
	protected boolean checkCompatibility(Enchantment e) {
		return super.checkCompatibility(e) && e != Enchantments.BLOCK_EFFICIENCY;
	}

}