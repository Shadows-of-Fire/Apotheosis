package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class SeaInfusionEnchantment extends Enchantment {

	public SeaInfusionEnchantment() {
		super(Rarity.VERY_RARE, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
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
		if (user.isInWaterRainOrBubble()) {
			if (user instanceof PlayerEntity) {
				DamageSource source = DamageSource.playerAttack((PlayerEntity) user);
				source.setMagic().bypassArmor();
				target.hurt(source, level * level * 0.8F * Apotheosis.localAtkStrength);
			} else target.hurt(DamageSource.MAGIC, level * level * 0.8F * Apotheosis.localAtkStrength);
		}
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_GREEN);
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return super.checkCompatibility(ench) && ench != ApotheosisObjects.HELL_INFUSION;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || Enchantments.IMPALING.canApplyAtEnchantingTable(stack);
	}
}