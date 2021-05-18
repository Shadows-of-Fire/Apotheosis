package shadows.apotheosis.deadly.affix.impl.shield;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

public class ArrowCatcherAffix extends Affix {

	public static final MethodHandle getArrowStack;

	static {
		Method getAS = ObfuscationReflectionHelper.findMethod(AbstractArrowEntity.class, "func_184550_j");
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
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
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
		Entity iSource = source.getImmediateSource();
		if (iSource instanceof AbstractArrowEntity) {
			AbstractArrowEntity arrow = (AbstractArrowEntity) iSource;
			arrow.remove();
			try {
				ItemStack arrowStack = (ItemStack) getArrowStack.invoke(arrow);
				arrowStack.setCount((int) level);
				entity.world.addEntity(new ItemEntity(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), arrowStack));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return amount;
	}

}
