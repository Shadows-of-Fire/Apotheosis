package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import shadows.apotheosis.ench.EnchModule;

public class GrowthSerumEnchant extends Enchantment {

	public GrowthSerumEnchant() {
		super(Rarity.VERY_RARE, EnchModule.SHEARS, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND });
	}

	@Override
	public int getMinCost(int pLevel) {
		return 55;
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_GREEN);
	}

	public void unshear(SheepEntity sheep, ItemStack shears) {
		if (EnchantmentHelper.getItemEnchantmentLevel(this, shears) > 0 && sheep.random.nextBoolean()) sheep.setSheared(false);
	}

}
