package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import shadows.apotheosis.Apotheosis;

@Mixin(ShearsItem.class)
public class ShearsItemMixin extends Item {

	public ShearsItemMixin(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		if (!Apotheosis.enableEnch) return super.canApplyAtEnchantingTable(stack, ench);
		return super.canApplyAtEnchantingTable(stack, ench) || ench == Enchantments.UNBREAKING || ench == Enchantments.BLOCK_EFFICIENCY || ench == Enchantments.BLOCK_FORTUNE;
	}

	@Override
	public int getEnchantmentValue() {
		return Apotheosis.enableEnch ? 15 : 0;
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return Apotheosis.enableEnch ? Apotheosis.MODID : "minecraft";
	}

}
