package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.EnchModuleEvents.TridentGetter;

public class SpearfishingEnchant extends Enchantment {

	public SpearfishingEnchant() {
		super(Rarity.UNCOMMON, EnchantmentType.TRIDENT, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	/**
	* Returns the minimal value of enchantability needed on the enchantment level passed.
	*/
	@Override
	public int getMinCost(int pEnchantmentLevel) {
		return 12 + (pEnchantmentLevel - 1) * 18;
	}

	@Override
	public int getMaxCost(int pEnchantmentLevel) {
		return 200;
	}

	/**
	* Returns the maximum level that the enchantment can have.
	*/
	@Override
	public int getMaxLevel() {
		return 5;
	}

	public void addFishes(LivingDropsEvent e) {
		DamageSource src = e.getSource();
		if (src.getDirectEntity() instanceof TridentEntity) {
			TridentEntity trident = (TridentEntity) src.getDirectEntity();
			if (trident.level.isClientSide) return;
			ItemStack triStack = ((TridentGetter) trident).getTridentItem();
			int level = EnchantmentHelper.getItemEnchantmentLevel(this, triStack);
			if (trident.random.nextFloat() < 3.5F * level) {
				Entity dead = e.getEntityLiving();
				e.getDrops().add(new ItemEntity(trident.level, dead.getX(), dead.getY(), dead.getZ(), new ItemStack(EnchModule.SPEARFISHING_DROPS.getRandomElement(trident.random), 1 + trident.random.nextInt(3))));
			}
		}
	}

}