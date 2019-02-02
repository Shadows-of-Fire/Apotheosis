package shadows.ench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldProviderHell;

public class EnchantmentHellInfused extends EnchantmentDamage {

	public EnchantmentHellInfused() {
		super(Rarity.VERY_RARE, 0, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("apotheosis.hell_infusion");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 1 + level * 12;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 15;
	}

	@Override
	public int getMaxLevel() {
		return 10;
	}

	@Override
	public float calcDamageByCreature(int level, EnumCreatureAttribute creatureType) {
		return 0;
	}

	@Override
	public String getName() {
		return "enchantment.apotheosis.hell_infusion";
	}

	@Override
	public boolean canApplyTogether(Enchantment ench) {
		return ench != this;
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
