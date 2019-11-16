package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import shadows.Apotheosis;

public class EnchantmentMounted extends Enchantment {

	public EnchantmentMounted() {
		super(Rarity.RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("apotheosis.mounted_strike");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 1 + level * 8;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + level * 4;
	}

	@Override
	public int getMaxLevel() {
		return 6;
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof ItemAxe ? true : super.canApply(stack);
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
		if (user.getRidingEntity() != null) {
			DamageSource source = user instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) user) : DamageSource.GENERIC;
			target.attackEntityFrom(source, (float) Math.pow(1.75F, level) * Apotheosis.localAtkStrength);
		}
	}

}
