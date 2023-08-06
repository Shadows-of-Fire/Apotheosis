package dev.shadowsoffire.apotheosis.ench.enchantments;

import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.util.FakePlayer;

public class IcyThornsEnchant extends Enchantment {

    public IcyThornsEnchant() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[] { EquipmentSlot.CHEST });
    }

    @Override
    public int getMinCost(int level) {
        return 35 + (level - 1) * 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 200;
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
    protected boolean checkCompatibility(Enchantment pOther) {
        return super.checkCompatibility(pOther) && pOther != Enchantments.THORNS;
    }

    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int level) {
        if (user == null) return;
        RandomSource rand = user.getRandom();
        if (attacker instanceof LivingEntity ent && !(attacker instanceof FakePlayer)) {
            ent.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (100 + rand.nextInt(100)) * level, level));
        }
    }

}
