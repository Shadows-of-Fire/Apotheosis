package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import shadows.Apotheosis;
import shadows.ench.EnchModule;

public class EnchantmentShieldBash extends Enchantment {

	public EnchantmentShieldBash() {
		super(Rarity.RARE, null, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName(Apotheosis.MODID + ".shield_bash");
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 1 + (enchantmentLevel - 1) * 11;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return getMinEnchantability(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem().isShield(stack, null);
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
		if (target instanceof EntityLivingBase) {
			ItemStack stack = user.getHeldItemMainhand();
			if (stack.getItem().isShield(stack, user)) {
				stack.damageItem(35, user);
				DamageSource src = user instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) user) : DamageSource.GENERIC;
				((EntityLivingBase) target).attackEntityFrom(src, EnchModule.localAtkStrength * 2.35F * level);
			}
		}
	}

}