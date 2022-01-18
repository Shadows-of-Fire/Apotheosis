package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ToolActions;
import shadows.apotheosis.ench.EnchModule;

public class ReflectiveEnchant extends Enchantment {

	public ReflectiveEnchant() {
		super(Rarity.RARE, EnchModule.SHIELD, new EquipmentSlot[] { EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND });
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return enchantmentLevel * 18;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 200;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || stack.canPerformAction(ToolActions.SHIELD_BLOCK);
	}

	/**
	 * Enables application of the reflective defenses enchantment.
	 * Called from {@link LivingEntity#blockUsingShield(LivingEntity)}
	 */
	public void reflectiveHook(LivingEntity user, LivingEntity attacker) {
		int level;
		if ((level = EnchantmentHelper.getItemEnchantmentLevel(this, user.getUseItem())) > 0) {
			if (user.level.random.nextInt(Math.max(2, 7 - level)) == 0) {
				DamageSource src = user instanceof Player ? DamageSource.playerAttack((Player) user).setMagic().bypassArmor() : DamageSource.MAGIC;
				attacker.hurt(src, level * 1.6F);
				user.getUseItem().hurtAndBreak(10, attacker, e -> {
					e.broadcastBreakEvent(EquipmentSlot.OFFHAND);
				});
			}
		}
	}

}