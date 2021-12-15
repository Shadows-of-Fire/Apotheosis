package shadows.apotheosis.ench.enchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class HellInfusionEnchantment extends Enchantment {

	public HellInfusionEnchantment() {
		super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
	}

	@Override
	public int getMinCost(int level) {
		return 50 + (level - 1) * 13;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level + 1);
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof AxeItem ? true : super.canEnchant(stack);
	}

	@Override
	public void doPostAttack(LivingEntity user, Entity target, int level) {
		if (user.level.dimension() == Level.NETHER) {
			if (user instanceof Player) {
				DamageSource source = DamageSource.playerAttack((Player) user);
				source.setMagic().bypassArmor();
				target.hurt(source, level * level * 1.3F * Apotheosis.localAtkStrength);
			} else target.hurt(DamageSource.MAGIC, level * level * 1.3F * Apotheosis.localAtkStrength);
		}
	}

	@Override
	public Component getFullname(int level) {
		return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_GREEN);
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return super.checkCompatibility(ench) && ench != ApotheosisObjects.SEA_INFUSION;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || Enchantments.IMPALING.canApplyAtEnchantingTable(stack);
	}
}