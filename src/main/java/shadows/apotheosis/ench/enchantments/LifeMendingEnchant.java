package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingHealEvent;

public class LifeMendingEnchant extends Enchantment {

	public LifeMendingEnchant() {
		super(Rarity.VERY_RARE, EnchantmentType.BREAKABLE, EquipmentSlotType.values());
	}

	@Override
	public int getMinCost(int level) {
		return 60 + level * 20;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 50;
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
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || stack.getItem().isShield(stack, null);
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_RED);
	}

	private static final EquipmentSlotType[] SLOTS = EquipmentSlotType.values();

	public void lifeMend(LivingHealEvent e) {
		if (e.getEntity().level.isClientSide) return;
		float amt = e.getAmount();
		if (amt <= 0F) return;
		for (EquipmentSlotType slot : SLOTS) {
			ItemStack stack = e.getEntityLiving().getItemBySlot(slot);
			if (!stack.isEmpty() && stack.isDamaged()) {
				int level = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
				if (level <= 0) continue;
				float cost = 1.0F / (1 << level - 1);
				int maxRestore = Math.min(MathHelper.floor(amt / cost), stack.getDamageValue());
				e.setAmount(e.getAmount() - maxRestore * cost);
				stack.setDamageValue(stack.getDamageValue() - maxRestore);
				return;
			}
		}
	}

}