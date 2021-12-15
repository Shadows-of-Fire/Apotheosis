package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.EnchModule;

public class ShieldBashEnchant extends Enchantment {

	public ShieldBashEnchant() {
		super(Rarity.RARE, EnchModule.SHIELD, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 1 + (enchantmentLevel - 1) * 17;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return this.getMinCost(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || stack.getItem().isShield(stack, null);
	}

	@Override
	public void doPostAttack(LivingEntity user, Entity target, int level) {
		if (target instanceof LivingEntity) {
			ItemStack stack = user.getMainHandItem();
			if (stack.getItem().isShield(stack, user)) {
				stack.hurtAndBreak(35, user, e -> {
					e.broadcastBreakEvent(EquipmentSlot.OFFHAND);
				});
				DamageSource src = user instanceof Player ? DamageSource.playerAttack((Player) user) : DamageSource.GENERIC;
				((LivingEntity) target).hurt(src, Apotheosis.localAtkStrength * 2.35F * level);
			}
		}
	}

}