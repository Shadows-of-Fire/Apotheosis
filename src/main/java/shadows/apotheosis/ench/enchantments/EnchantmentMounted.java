package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import shadows.apotheosis.Apotheosis;

public class EnchantmentMounted extends Enchantment {

	public EnchantmentMounted() {
		super(Rarity.RARE, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
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
		return stack.getItem() instanceof AxeItem ? true : super.canApply(stack);
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, int level) {
		if (user.getRidingEntity() != null) {
			DamageSource source = user instanceof PlayerEntity ? DamageSource.causePlayerDamage((PlayerEntity) user) : DamageSource.GENERIC;
			target.attackEntityFrom(source, (float) Math.pow(1.75F, level) * Apotheosis.localAtkStrength);
		}
	}

}
