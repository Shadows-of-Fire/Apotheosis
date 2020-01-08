package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentTranslation;
import shadows.apotheosis.deadly.loot.AffixModifier;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.attributes.CustomAttributes;

public class SharpshooterAffix extends AttributeAffix {

	public SharpshooterAffix(int weight) {
		super(CustomAttributes.LONGSHOT_DAMAGE, 5, 15, 0, false, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		float lvl = super.apply(stack, rand, modifier);
		AffixHelper.addLore(stack, new TextComponentTranslation("affix." + this.getRegistryName() + ".desc").getFormattedText());
		return lvl;
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, float level) {
		if (target != null && user.getDistanceSq(target) > 30 * 30) {
			if (user instanceof EntityPlayer) {
				target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) user), level);
			}
		}
	}

}
