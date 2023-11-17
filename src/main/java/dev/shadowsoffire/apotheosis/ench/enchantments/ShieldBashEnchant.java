package dev.shadowsoffire.apotheosis.ench.enchantments;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ToolActions;

public class ShieldBashEnchant extends Enchantment {

    public ShieldBashEnchant() {
        super(Rarity.RARE, EnchModule.SHIELD, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 1 + (enchantmentLevel - 1) * 17;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return this.getMinCost(enchantmentLevel) + 40;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) || stack.canPerformAction(ToolActions.SHIELD_BLOCK);
    }

    @Override
    public float getDamageBonus(int pLevel, MobType pType) {
        return 3.5F * pLevel;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity) {
            ItemStack stack = user.getMainHandItem();
            if (stack.getEnchantmentLevel(this) == level) {
                stack.hurtAndBreak(Math.max(1, 20 - level), user, e -> {
                    e.broadcastBreakEvent(EquipmentSlot.OFFHAND);
                });
            }
        }
    }

    @Override
    protected boolean checkCompatibility(Enchantment pOther) {
        return super.checkCompatibility(pOther) && !(pOther instanceof DamageEnchantment);
    }

}
