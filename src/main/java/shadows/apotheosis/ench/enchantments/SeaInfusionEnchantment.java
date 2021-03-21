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
	public int getMinEnchantability(int level) {
		return 50 + (level - 1) * 13;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level + 1);
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
		if (user.isInWaterRainOrBubbleColumn()) {
			if (user instanceof PlayerEntity) {
				DamageSource source = DamageSource.causePlayerDamage((PlayerEntity) user);
				source.setMagicDamage().setDamageBypassesArmor();
				target.attackEntityFrom(source, level * level * 0.8F * Apotheosis.localAtkStrength);
			} else target.attackEntityFrom(DamageSource.MAGIC, level * level * 0.8F * Apotheosis.localAtkStrength);
		}
	}

	@Override
	public ITextComponent getDisplayName(int level) {
		return ((IFormattableTextComponent) super.getDisplayName(level)).mergeStyle(TextFormatting.DARK_GREEN);
	}

	@Override
	protected boolean canApplyTogether(Enchantment ench) {
		return super.canApplyTogether(ench) && ench != ApotheosisObjects.HELL_INFUSION;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || Enchantments.IMPALING.canApplyAtEnchantingTable(stack);
	}
}