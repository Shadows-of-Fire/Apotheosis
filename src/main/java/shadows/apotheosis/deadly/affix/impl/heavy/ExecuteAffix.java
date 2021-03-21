package shadows.apotheosis.deadly.affix.impl.heavy;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

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
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = this.range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level * 100)));
	}

	@Override
	public ITextComponent getDisplayName(float level) {
		return new TranslationTextComponent("affix." + this.getRegistryName() + ".name", fmt(level * 100)).mergeStyle(TextFormatting.GRAY);
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