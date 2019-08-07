package shadows.ench.enchantments;

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
import shadows.ench.EnchModule;

public class EnchantmentHellInfused extends Enchantment {

	public EnchantmentHellInfused() {
		super(Rarity.VERY_RARE, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinEnchantability(int level) {
		return 50 + level * 7;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + level * 4;
	}

	@Override
	public int getMaxLevel() {
		return 10;
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
				target.attackEntityFrom(source, level * level * 0.5F * EnchModule.localAtkStrength);
			} else target.attackEntityFrom(DamageSource.MAGIC, level * level * 0.5F * EnchModule.localAtkStrength);
		}
	}

}
