package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.mixin.TemptGoalMixin;

public class TemptingEnchant extends Enchantment {

	public TemptingEnchant() {
		super(Rarity.UNCOMMON, EnchModule.HOE, new EquipmentSlot[0]);
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 0;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 200;
	}

	/**
	 * Allows checking if an item with Tempting can make an animal follow.
	 * Called from {@link TemptGoal#shouldFollow(LivingEntity)}
	 * Injected by {@link TemptGoalMixin}
	 */
	public boolean shouldFollow(LivingEntity target) {
		ItemStack stack = target.getMainHandItem();
		if (stack.getEnchantmentLevel(this) > 0) return true;
		stack = target.getOffhandItem();
		return stack.getEnchantmentLevel(this) > 0;
	}

}