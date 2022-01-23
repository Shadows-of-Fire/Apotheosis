package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.ench.EnchModuleEvents.TridentGetter;

public class SpearfishingEnchant extends Enchantment {

	public SpearfishingEnchant() {
		super(Rarity.UNCOMMON, EnchantmentCategory.TRIDENT, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
	}

	/**
	* Returns the minimal value of enchantability needed on the enchantment level passed.
	*/
	public int getMinCost(int pEnchantmentLevel) {
		return 12 + (pEnchantmentLevel - 1) * 18;
	}

	public int getMaxCost(int pEnchantmentLevel) {
		return 200;
	}

	/**
	* Returns the maximum level that the enchantment can have.
	*/
	public int getMaxLevel() {
		return 5;
	}

	public void addFishes(LivingDropsEvent e) {
		DamageSource src = e.getSource();
		if (src.getDirectEntity() instanceof ThrownTrident trident) {
			if (trident.level.isClientSide) return;
			ItemStack triStack = ((TridentGetter) trident).getTridentItem();
			int level = EnchantmentHelper.getItemEnchantmentLevel(this, triStack);
			if (trident.random.nextFloat() < 3.5F * level) {
				Entity dead = e.getEntityLiving();
				e.getDrops().add(new ItemEntity(trident.level, dead.getX(), dead.getY(), dead.getZ(), new ItemStack(Apoth.Tags.SPEARFISHING_DROPS.getRandomElement(trident.random), 1 + trident.random.nextInt(3))));
			}
		}
	}

}