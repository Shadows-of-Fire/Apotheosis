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
	public int getMinEnchantability(int enchantmentLevel) {
		return 30 + (enchantmentLevel - 1) * 30;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return this.getMinEnchantability(enchantmentLevel) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public ITextComponent getDisplayName(int level) {
		return ((IFormattableTextComponent) super.getDisplayName(level)).mergeStyle(TextFormatting.DARK_PURPLE);
	}

	@Override
	protected boolean canApplyTogether(Enchantment e) {
		return super.canApplyTogether(e) && e != Enchantments.EFFICIENCY;
	}

}