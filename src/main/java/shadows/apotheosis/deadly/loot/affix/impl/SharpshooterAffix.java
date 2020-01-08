package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.loot.AffixModifier;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.attributes.CustomAttributes;

public class SharpshooterAffix extends AttributeAffix {

	public SharpshooterAffix(int weight) {
		super(CustomAttributes.LONGSHOT_DAMAGE, 5, 15, Operation.ADDITION, false, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		float lvl = super.apply(stack, rand, modifier);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc"));
		return lvl;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (target != null && user.getDistanceSq(target) > 30 * 30) {
			if (user instanceof PlayerEntity) {
				target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) user), level);
			}
		}
	}

}
