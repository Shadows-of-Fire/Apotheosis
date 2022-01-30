package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
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
	public void reflect(ShieldBlockEvent e) {
		LivingEntity user = e.getEntityLiving();
		Entity attacker = e.getDamageSource().getDirectEntity();
		ItemStack shield = user.getUseItem();
		int level;
		if ((level = EnchantmentHelper.getItemEnchantmentLevel(this, shield)) > 0) {
			if (user.level.random.nextInt(Math.max(2, 7 - level)) == 0) {
				DamageSource src = user instanceof Player plr ? DamageSource.playerAttack(plr).setMagic().bypassArmor() : DamageSource.MAGIC;
				if (attacker instanceof LivingEntity livingAttacker) {
					livingAttacker.hurt(src, level * 0.15F * e.getBlockedDamage());
					shield.hurtAndBreak(10, user, ent -> {
						ent.broadcastBreakEvent(EquipmentSlot.OFFHAND);
					});
				}
			}
		}
	}

}