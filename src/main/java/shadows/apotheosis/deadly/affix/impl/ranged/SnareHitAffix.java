package shadows.apotheosis.deadly.affix.impl.ranged;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.EquipmentType;

/**
 * Targets hit with an arrow are snared (by application of slowness 11)
 */
public class SnareHitAffix extends Affix {

	public SnareHitAffix(int weight) {
		super(weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		int lvl = 2 + rand.nextInt(5);
		if (modifier != null) lvl = (int) modifier.editLevel(this, lvl);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", lvl));
		return lvl;
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.RANGED;
	}

	@Override
	public void onArrowImpact(AbstractArrowEntity arrow, RayTraceResult res, Type type, float level) {
		if (type == Type.ENTITY) {
			Entity hit = ((EntityRayTraceResult) res).getEntity();
			if (hit instanceof LivingEntity) {
				((LivingEntity) hit).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 100, 10));
			}
		}
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 10;
	}

}