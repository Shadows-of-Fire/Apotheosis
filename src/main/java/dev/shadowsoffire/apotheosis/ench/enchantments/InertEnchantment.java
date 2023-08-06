package dev.shadowsoffire.apotheosis.ench.enchantments;

import com.google.common.base.Predicates;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class InertEnchantment extends Enchantment {

    public static final EnchantmentCategory NULL = EnchantmentCategory.create("apotheosis.null", Predicates.alwaysFalse());

    public InertEnchantment() {
        super(Rarity.VERY_RARE, NULL, new EquipmentSlot[0]);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

}
