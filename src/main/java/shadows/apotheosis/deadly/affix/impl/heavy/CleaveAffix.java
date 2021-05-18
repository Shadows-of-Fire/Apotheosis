package shadows.apotheosis.deadly.affix.impl.heavy;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Cleave Affix.  Allows for full strength attacks to trigger a full-strength attack against nearby enemies.
 */
public class CleaveAffix extends Affix {

	private static boolean cleaving = false;

	public CleaveAffix(int weight) {
		super(weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.AXE;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (Apotheosis.localAtkStrength >= 0.98 && !cleaving && user instanceof PlayerEntity) {
			cleaving = true;
			float chance = level % 1;
			int targets = (int) level;
			if (user.world.rand.nextFloat() < chance) {
				Predicate<Entity> pred = e -> !(e instanceof PlayerEntity) && e instanceof LivingEntity && ((LivingEntity) e).canAttack(EntityType.PLAYER);
				List<Entity> nearby = target.world.getEntitiesInAABBexcluding(target, new AxisAlignedBB(target.getPosition()).grow(6), pred);
				if (!user.world.isRemote) for (Entity e : nearby) {
					if (targets > 0) {
						user.ticksSinceLastSwing = 300;
						((PlayerEntity) user).attackTargetEntityWithCurrentItem(e);
						targets--;
					}
				}
			}
			cleaving = false;
		}
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		int nearby = 2 + rand.nextInt(9);
		float chance = Math.min(0.9999F, 0.3F + rand.nextFloat());
		if (modifier != null) chance = modifier.editLevel(this, chance);
		return nearby + chance;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
		float chance = level % 1;
		int targets = (int) level;
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", String.format("%.2f", chance * 100), targets));
	}

	@Override
	public ITextComponent getDisplayName(float level) {
		float chance = level % 1;
		int targets = (int) level;
		return new TranslationTextComponent("affix." + this.getRegistryName() + ".name", String.format("%.2f", chance * 100), targets).mergeStyle(TextFormatting.GRAY);
	}

	@Override
	public float getMin() {
		return 0.3F;
	}

	@Override
	public float getMax() {
		return 0.9999F;
	}

	/**
	 * Handles the upgrading of this affix's level, given two levels.
	 * Default logic is (highest level + lowest level / 2)
	 */
	public float upgradeLevel(float curLvl, float newLvl) {
		float curChance = curLvl % 1;
		int curTargets = (int) curLvl;
		float newChance = newLvl % 1;
		int newTargets = (int) newLvl;
		float chance = super.upgradeLevel(curChance, newChance);
		int targets = Math.min(10, curTargets > newTargets ? curTargets + newTargets / 2 : curTargets / 2 + newTargets);
		return targets + chance;
	}

	/**
	 * Generates a new level, as if the passed level were to be split in two.
	 */
	public float obliterateLevel(float level) {
		float chance = level % 1;
		int targets = (int) level;
		return Math.max(2, (targets / 2)) + Math.max(getMin(), (chance / 2));
	}

}