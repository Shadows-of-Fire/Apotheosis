package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import shadows.apotheosis.Apotheosis;

@Mixin(ShearsItem.class)
public class ShearsItemMixin extends Item {

	public ShearsItemMixin(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		return super.canApplyAtEnchantingTable(stack, ench) || ench == Enchantments.UNBREAKING || ench == Enchantments.BLOCK_EFFICIENCY || ench == Enchantments.BLOCK_FORTUNE;
	}

	@Override
	public int getEnchantmentValue() {
		return 15;
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return Apotheosis.MODID;
	}

}
