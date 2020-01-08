package shadows.apotheosis.ench.enchantments;

import java.util.Random;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.util.FakePlayer;

public class EnchantmentIcyThorns extends Enchantment {

	public EnchantmentIcyThorns() {
		super(Rarity.RARE, EnchantmentType.ARMOR_CHEST, new EquipmentSlotType[] { EquipmentSlotType.CHEST });
	}

	@Override
	public int getMinEnchantability(int level) {
		return 40 + level * 15;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return 90 + level * 15;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof ArmorItem ? true : super.canApply(stack);
	}

	@Override
	public void onUserHurt(LivingEntity user, Entity attacker, int level) {
		if (user == null) return;
		Random rand = user.getRNG();
		if (attacker instanceof LivingEntity && !(attacker instanceof FakePlayer)) {
			LivingEntity ent = (LivingEntity) attacker;
			ent.addPotionEffect(new EffectInstance(Effects.SLOWNESS, (100 + rand.nextInt(100)) * level, level));
		}
	}

}