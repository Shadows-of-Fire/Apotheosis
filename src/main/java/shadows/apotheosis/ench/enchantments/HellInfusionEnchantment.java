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
import net.minecraft.world.dimension.NetherDimension;
import shadows.apotheosis.Apotheosis;

public class HellInfusionEnchantment extends Enchantment {

	public HellInfusionEnchantment() {
		super(Rarity.VERY_RARE, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinEnchantability(int level) {
		return 40 + (level - 1) * 7;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level + 1);
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof AxeItem ? true : super.canApply(stack);
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, int level) {
		if (user.world.dimension instanceof NetherDimension) {
			if (user instanceof PlayerEntity) {
				DamageSource source = DamageSource.causePlayerDamage((PlayerEntity) user);
				source.setMagicDamage().setDamageBypassesArmor();
				target.attackEntityFrom(source, level * level * 1.3F * Apotheosis.localAtkStrength);
			} else target.attackEntityFrom(DamageSource.MAGIC, level * level * 1.3F * Apotheosis.localAtkStrength);
		}
	}

}
