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

public class IcyThornsEnchant extends Enchantment {

	public IcyThornsEnchant() {
		super(Rarity.RARE, EnchantmentType.ARMOR_CHEST, new EquipmentSlotType[] { EquipmentSlotType.CHEST });
	}

	@Override
	public int getMinCost(int level) {
		return 40 + (level - 1) * 35;
	}

	@Override
	public int getMaxCost(int level) {
		return 90 + level * 15;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof ArmorItem ? true : super.canEnchant(stack);
	}

	@Override
	public void doPostHurt(LivingEntity user, Entity attacker, int level) {
		if (user == null) return;
		Random rand = user.getRandom();
		if (attacker instanceof LivingEntity && !(attacker instanceof FakePlayer)) {
			LivingEntity ent = (LivingEntity) attacker;
			ent.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, (100 + rand.nextInt(100)) * level, level));
		}
	}

}