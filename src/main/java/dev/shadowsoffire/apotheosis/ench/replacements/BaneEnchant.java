package dev.shadowsoffire.apotheosis.ench.replacements;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class BaneEnchant extends DamageEnchantment {

    protected final MobType attrib;

    public BaneEnchant(Enchantment.Rarity rarity, MobType attrib, EquipmentSlot... slots) {
        super(rarity, 0, slots);
        this.attrib = attrib;
    }

    @Override
    public int getMinCost(int level) {
        if (this.attrib == MobType.UNDEFINED) return 1 + (level - 1) * 11;
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public float getDamageBonus(int level, MobType attrib) {
        if (this.attrib == MobType.UNDEFINED) return 1 + level * 0.5F;
        if (this.attrib == attrib) return level * 1.5F;
        return 0;
    }

    @Override
    public boolean checkCompatibility(Enchantment ench) {
        if (this.attrib == MobType.UNDEFINED) return ench != this;
        return ench == Enchantments.SHARPNESS ? ench != this : !(ench instanceof BaneEnchant);
    }

    /**
     * Called whenever a mob is damaged with an item that has this enchantment on it.
     */
    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity livingentity) {
            if (this.attrib != MobType.UNDEFINED && livingentity.getMobType() == this.attrib) {
                int i = 20 + user.getRandom().nextInt(10 * level);
                livingentity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, i, 3));
            }
        }

    }
}
