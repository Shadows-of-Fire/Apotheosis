package shadows.apotheosis.deadly.affix.impl.ranged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import java.util.function.Consumer;

/**
 * Ranged Spectral Shot Affix. Has a chance to fire an additional spectral arrow when shooting.
 */
public class SpectralShotAffix extends RangedAffix {

	public SpectralShotAffix(LootRarity rarity, float min, float max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

	@Override
	public void onArrowFired(LivingEntity user, AbstractArrow arrow, ItemStack bow, float level) {
		if (user.level.random.nextFloat() <= level) {
			if (!user.level.isClientSide) {
				ArrowItem arrowitem = (ArrowItem) Items.SPECTRAL_ARROW;
				AbstractArrow spectralArrow = arrowitem.createArrow(user.level, ItemStack.EMPTY, user);
				spectralArrow.shoot(user.getXRot(), user.getYRot(), 0.0F, 1 * 3.0F, 1.0F);
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
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
		super.addInformation(stack, level * 100, list);
	}

	@Override
	public Component getDisplayName(float level) {
		return super.getDisplayName(level * 100);
	}

	private void cloneMotion(AbstractArrow src, AbstractArrow dest) {
		dest.setDeltaMovement(src.getDeltaMovement().scale(1));
		dest.setYRot(src.getYRot());
		dest.setXRot(src.getXRot());
		dest.yRotO = dest.getYRot();
		dest.xRotO = dest.getXRot();
	}

}