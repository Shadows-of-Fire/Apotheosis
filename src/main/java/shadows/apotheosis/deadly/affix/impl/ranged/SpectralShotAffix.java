package shadows.apotheosis.deadly.affix.impl.ranged;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

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
	public void onArrowFired(LivingEntity user, AbstractArrow arrow, ItemStack bow, float level) {
		if (user.level.random.nextFloat() <= level) {
			if (!user.level.isClientSide) {
				ArrowItem arrowitem = (ArrowItem) Items.SPECTRAL_ARROW;
				AbstractArrow spectralArrow = arrowitem.createArrow(user.level, ItemStack.EMPTY, user);
				spectralArrow.shoot(user.xRot, user.yRot, 0.0F, 1 * 3.0F, 1.0F);
				this.cloneMotion(arrow, spectralArrow);
				spectralArrow.setCritArrow(arrow.isCritArrow());
				spectralArrow.setBaseDamage(arrow.getBaseDamage());
				spectralArrow.setKnockback(arrow.knockback);
				spectralArrow.setRemainingFireTicks(arrow.getRemainingFireTicks());
				spectralArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
				spectralArrow.getPersistentData().putBoolean("apoth.generated", true);
				arrow.level.addFreshEntity(spectralArrow);
			}
		}
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = this.range.getFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level * 100)));
	}

	@Override
	public Component getDisplayName(float level) {
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", fmt(level * 100)).withStyle(ChatFormatting.GRAY);
	}

	private void cloneMotion(AbstractArrow src, AbstractArrow dest) {
		dest.setDeltaMovement(src.getDeltaMovement().scale(1));
		dest.yRot = src.yRot;
		dest.xRot = src.xRot;
		dest.yRotO = dest.yRot;
		dest.xRotO = dest.xRot;
	}

}