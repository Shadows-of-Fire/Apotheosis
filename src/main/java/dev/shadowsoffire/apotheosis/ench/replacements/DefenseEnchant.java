package dev.shadowsoffire.apotheosis.ench.replacements;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

public class DefenseEnchant extends ProtectionEnchantment {

    public DefenseEnchant(Rarity rarity, Type type, EquipmentSlot... slots) {
        super(rarity, type, slots);
    }

    /**
     * Nerfs the points from Fire Protection and Projectile Protection to 1/level from 2/level
     */
    @Override
    public int getDamageProtection(int level, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return 0;
        }
        else if (this.type == ProtectionEnchantment.Type.ALL) {
            return level;
        }
        else if (this.type == ProtectionEnchantment.Type.FIRE && source.is(DamageTypeTags.IS_FIRE)) {
            return level;
        }
        else if (this.type == ProtectionEnchantment.Type.FALL && source.is(DamageTypeTags.IS_FALL)) {
            return level * 3;
        }
        else if (this.type == ProtectionEnchantment.Type.EXPLOSION && source.is(DamageTypeTags.IS_EXPLOSION)) {
            return level * 2;
        }
        else {
            return this.type == ProtectionEnchantment.Type.PROJECTILE && source.is(DamageTypeTags.IS_PROJECTILE) ? level : 0;
        }
    }

    /**
     * Determines if the enchantment passed can be applyied together with this enchantment.
     */
    @Override
    public boolean checkCompatibility(Enchantment ench) {
        if (this == Enchantments.FALL_PROTECTION || this == Enchantments.ALL_DAMAGE_PROTECTION) return ench != this;
        if (ench instanceof ProtectionEnchantment pEnch) {
            if (ench == this) return false;
            return pEnch.type == Type.ALL || pEnch.type == Type.FALL;
        }
        return ench != this;
    }

}
