package shadows.ench.enchantments;

import java.util.Random;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import shadows.Apotheosis;

public class EnchantmentIcyThorns extends Enchantment {

	public EnchantmentIcyThorns() {
		super(Rarity.RARE, EnumEnchantmentType.ARMOR_CHEST, new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST });
		this.setName(Apotheosis.MODID + ".icy_thorns");
	}

	public int getMinEnchantability(int level) {
		return 40 + level * 15;
	}

	public int getMaxEnchantability(int level) {
		return 90 + level * 15;
	}

	public int getMaxLevel() {
		return 3;
	}

	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof ItemArmor ? true : super.canApply(stack);
	}

	public void onUserHurt(EntityLivingBase user, Entity attacker, int level) {
		Random rand = user.getRNG();
		if (attacker instanceof EntityLivingBase) {
			EntityLivingBase ent = (EntityLivingBase) attacker;
			ent.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, (100 + rand.nextInt(100)) * level, level));
		}
	}

}