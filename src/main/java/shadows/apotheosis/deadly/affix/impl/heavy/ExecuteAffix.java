package shadows.apotheosis.deadly.affix.impl.heavy;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Targets below a certain percent HP threshold are instantly killed.
 */
public class ExecuteAffix extends RangedAffix {

	private static final DamageSource EXECUTION = new DamageSource("apoth.execute").bypassInvul().bypassMagic();

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
				living.hurt(EXECUTION, Float.MAX_VALUE);
			}
		}
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = this.range.sample(rand);
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

	@Override
	public float getMin() {
		return 0.03F;
	}

	@Override
	public float getMax() {
		return 0.2F;
	}

}