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
import net.minecraft.world.WorldProviderHell;
import shadows.ench.EnchModule;

public class EnchantmentHellInfused extends Enchantment {

	public EnchantmentHellInfused() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("apotheosis.hell_infusion");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 50 + level * 7;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + level * 4;
	}

	@Override
	public int getMaxLevel() {
		return 10;
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof ItemAxe ? true : super.canApply(stack);
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
		if (user instanceof EntityPlayer && user.world.provider instanceof WorldProviderHell) {
			target.attackEntityFrom(DamageSource.MAGIC, level * level * 0.5F * EnchModule.localAtkStrength);
		}
	}

}
