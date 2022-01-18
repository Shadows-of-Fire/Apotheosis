package shadows.apotheosis.ench.enchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingHealEvent;

public class LifeMendingEnchant extends Enchantment {

	public LifeMendingEnchant() {
		super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
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
		return super.canApplyAtEnchantingTable(stack) || stack.canPerformAction(ToolActions.SHIELD_BLOCK);
	}

	@Override
	public Component getFullname(int level) {
		return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_RED);
	}

	private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

	public void lifeMend(LivingHealEvent e) {
		if (e.getEntity().level.isClientSide) return;
		float amt = e.getAmount();
		if (amt <= 0F) return;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = e.getEntityLiving().getItemBySlot(slot);
			if (!stack.isEmpty() && stack.isDamaged()) {
				int level = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
				float cost = 1.0F / (1 << (level - 1));
				int maxRestore = Math.min(Mth.floor(amt / cost), stack.getDamageValue());
				e.setAmount(e.getAmount() - maxRestore * cost);
				stack.setDamageValue(stack.getDamageValue() - maxRestore);
				return;
			}
		}
	}

}