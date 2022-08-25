package shadows.apotheosis.adventure.affix.effect;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

public class SpectralShotAffix extends Affix {

	protected static final StepFunction LEVEL_FUNC = AffixHelper.step(0.2F, 8, 0.05F);

	public SpectralShotAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack).isRanged() && rarity.isAtLeast(LootRarity.EPIC);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(100 * getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public void onArrowFired(ItemStack stack, LootRarity rarity, float level, LivingEntity user, AbstractArrow arrow) {
		if (user.level.random.nextFloat() <= getTrueLevel(rarity, level)) {
			if (!user.level.isClientSide) {
				ArrowItem arrowitem = (ArrowItem) Items.SPECTRAL_ARROW;
				AbstractArrow spectralArrow = arrowitem.createArrow(user.level, ItemStack.EMPTY, user);
				spectralArrow.shoot(user.getXRot(), user.getYRot(), 0.0F, 2.0F, 1.0F);
				this.cloneMotion(arrow, spectralArrow);
				spectralArrow.setCritArrow(arrow.isCritArrow());
				spectralArrow.setBaseDamage(arrow.getBaseDamage());
				spectralArrow.setKnockback(arrow.knockback);
				spectralArrow.setRemainingFireTicks(arrow.getRemainingFireTicks());
				spectralArrow.pickup = Pickup.CREATIVE_ONLY;
				spectralArrow.getPersistentData().putBoolean("apoth.attrib.done", true);
				arrow.level.addFreshEntity(spectralArrow);
			}
		}
	}

	private void cloneMotion(AbstractArrow src, AbstractArrow dest) {
		dest.setDeltaMovement(src.getDeltaMovement().scale(1));
		dest.setYRot(src.getYRot());
		dest.setXRot(src.getXRot());
		dest.yRotO = dest.yRotO;
		dest.xRotO = dest.xRotO;
	}

	private static float getTrueLevel(LootRarity rarity, float level) {
		return (rarity.ordinal() - LootRarity.EPIC.ordinal()) * 0.2F + LEVEL_FUNC.get(level);
	}

}
