package dev.shadowsoffire.apotheosis.ench.enchantments;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public class ReboundingEnchant extends Enchantment {

    public ReboundingEnchant() {
        super(Rarity.RARE, EnchModule.CORE_ARMOR, new EquipmentSlot[] { EquipmentSlot.CHEST, EquipmentSlot.LEGS });
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 22 + (level - 1) * 18;
    }

    @Override
    public int getMaxCost(int level) {
        return 200;
    }

    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int level) {
        if (attacker != null && user.distanceToSqr(attacker) <= 4D && attacker.canChangeDimensions()) {
            level = EnchantmentHelper.getEnchantmentLevel(this, user);
            Vec3 vec = new Vec3(attacker.getX() - user.getX(), attacker.getY() - user.getY(), attacker.getZ() - user.getZ());
            attacker.push(vec.x * 2 * level, vec.y * 3 * level, vec.z * 2 * level);
        }
    }

}
