package shadows.apotheosis.deadly.affix.impl.heavy;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.EquipmentType;

/**
 * Targets below a certain percent HP threshold are instantly killed.
 */
public class ExecuteAffix extends RangedAffix {

	private static final DamageSource EXECUTION = new DamageSource("apoth.execute").setDamageAllowedInCreativeMode().setDamageIsAbsolute();

	public ExecuteAffix(int weight) {
		super(0.05F, 0.1F, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.AXE;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (target instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) target;
			if (living.getHealth() / living.getMaxHealth() < level) {
				living.attackEntityFrom(EXECUTION, Float.MAX_VALUE);
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

	@Override
	public float getMin() {
		return 0.03F;
	}

	@Override
	public float getMax() {
		return 0.2F;
	}

}