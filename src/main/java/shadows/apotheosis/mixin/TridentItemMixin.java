package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import shadows.apotheosis.Apotheosis;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin extends Item {

	public TridentItemMixin(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		if (!Apotheosis.enableEnch) return super.canApplyAtEnchantingTable(stack, ench);
		return super.canApplyAtEnchantingTable(stack, ench) || ench == Enchantments.SHARPNESS || ench == Enchantments.MOB_LOOTING || ench == Enchantments.PIERCING;
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return Apotheosis.enableEnch ? Apotheosis.MODID : "minecraft";
	}
}
