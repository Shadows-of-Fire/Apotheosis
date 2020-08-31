package shadows.apotheosis.deadly.loot.affix.impl.ranged;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

/**
 * Ranged Spectral Shot Affix. Has a chance to fire an additional spectral arrow when shooting.
 */
public class SpectralShotAffix extends RangedAffix {

	public SpectralShotAffix(int weight) {
		super(0.1F, 1.0F, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.RANGED;
	}

	@Override
	public void onArrowFired(LivingEntity user, AbstractArrowEntity arrow, ItemStack bow, float level) {
		if (user.world.rand.nextFloat() <= level) {
			if (!user.world.isRemote) {
				ArrowItem arrowitem = (ArrowItem) Items.SPECTRAL_ARROW;
				AbstractArrowEntity spectralArrow = arrowitem.createArrow(user.world, ItemStack.EMPTY, user);
				spectralArrow.shoot(user.rotationPitch, user.rotationYaw, 0.0F, 1 * 3.0F, 1.0F);
				cloneMotion(arrow, spectralArrow);
				spectralArrow.setIsCritical(arrow.getIsCritical());
				spectralArrow.setDamage(arrow.getDamage());
				spectralArrow.setKnockbackStrength(arrow.knockbackStrength);
				spectralArrow.forceFireTicks(arrow.getFireTimer());
				spectralArrow.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
				spectralArrow.getPersistentData().putBoolean("apoth.generated", true);
				arrow.world.addEntity(spectralArrow);
			}
		}
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", String.format("%.2f", lvl * 100)));
		return lvl;
	}

	private void cloneMotion(AbstractArrowEntity src, AbstractArrowEntity dest) {
		dest.setMotion(src.getMotion().scale(1));
		dest.rotationYaw = src.rotationYaw;
		dest.rotationPitch = src.rotationPitch;
		dest.prevRotationYaw = dest.rotationYaw;
		dest.prevRotationPitch = dest.rotationPitch;
	}

}
