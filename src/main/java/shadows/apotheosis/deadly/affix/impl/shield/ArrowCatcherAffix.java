package shadows.apotheosis.deadly.affix.impl.shield;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

public class ArrowCatcherAffix extends Affix {

	public static final MethodHandle getArrowStack;

	static {
		Method getAS = ObfuscationReflectionHelper.findMethod(AbstractArrow.class, "func_184550_j");
		getAS.setAccessible(true);
		try {
			getArrowStack = MethodHandles.lookup().unreflect(getAS);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public ArrowCatcherAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, AffixModifier modifier) {
		int lvl = 1 + rand.nextInt(2);
		if (modifier != null) lvl = (int) modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
		if (level == 1) list.accept(loreComponent("affix." + this.getRegistryName() + ".desc1"));
		else list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level)));
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SHIELD;
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 3;
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return (int) super.upgradeLevel(curLvl, newLvl);
	}

	@Override
	public float obliterateLevel(float level) {
		return (int) super.obliterateLevel(level);
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, float level) {
		Entity iSource = source.getDirectEntity();
		if (iSource instanceof AbstractArrow) {
			AbstractArrow arrow = (AbstractArrow) iSource;
			arrow.remove(RemovalReason.DISCARDED);
			try {
				ItemStack arrowStack = (ItemStack) getArrowStack.invoke(arrow);
				if (!ItemTags.ARROWS.contains(arrowStack.getItem())) return amount;
				arrowStack.setCount((int) level);
				entity.level.addFreshEntity(new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), arrowStack));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return amount;
	}

}
