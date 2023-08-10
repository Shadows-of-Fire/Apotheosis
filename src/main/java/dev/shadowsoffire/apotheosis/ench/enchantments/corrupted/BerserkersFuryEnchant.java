package dev.shadowsoffire.apotheosis.ench.enchantments.corrupted;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class BerserkersFuryEnchant extends Enchantment {

    public BerserkersFuryEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[] { EquipmentSlot.CHEST });
    }

    @Override
    public int getMinCost(int level) {
        return 50 + level * 40;
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
    public boolean isCurse() {
        return true;
    }

    @Override
    public Component getFullname(int level) {
        return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_RED);
    }

    /**
     * Handles the application of Berserker's Fury.
     */
    @SuppressWarnings("deprecation")
    public void livingHurt(LivingHurtEvent e) {
        LivingEntity user = e.getEntity();
        if (e.getSource().getEntity() instanceof Entity && user.getEffect(MobEffects.DAMAGE_RESISTANCE) == null) {
            int level = EnchantmentHelper.getEnchantmentLevel(this, user);
            if (level > 0) {
                if (Affix.isOnCooldown(BuiltInRegistries.ENCHANTMENT.getKey(this), 900, user)) return;
                user.invulnerableTime = 0;
                user.hurt(user.damageSources().source(Apoth.DamageTypes.CORRUPTED), (float) Math.pow(2.5, level));
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, level - 1));
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, level - 1));
                user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, level - 1));
                Affix.startCooldown(BuiltInRegistries.ENCHANTMENT.getKey(this), user);
            }
        }
    }

}
