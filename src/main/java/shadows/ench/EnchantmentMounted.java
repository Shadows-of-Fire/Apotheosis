package shadows.ench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;

public class EnchantmentMounted extends EnchantmentDamage {

	public EnchantmentMounted() {
		super(Rarity.RARE, 0, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("apotheosis.mounted_strike");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 1 + level * 8;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 8;
	}

	@Override
	public int getMaxLevel() {
		return 6;
	}

	@Override
	public float calcDamageByCreature(int level, EnumCreatureAttribute creatureType) {
		return 0;
	}

	@Override
	public String getName() {
		return "enchantment.apotheosis.mounted_strike";
	}

	@Override
	public boolean canApplyTogether(Enchantment ench) {
		return ench != this;
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
		if (user instanceof EntityPlayer && user.getRidingEntity() instanceof EntityHorse) {
			target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) user), (float) Math.pow(1.75F, level) * EnchModule.localAtkStrength);
		}
	}

}
