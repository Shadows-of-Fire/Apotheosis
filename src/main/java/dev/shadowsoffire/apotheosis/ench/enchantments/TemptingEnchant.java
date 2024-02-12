package dev.shadowsoffire.apotheosis.ench.enchantments;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.apotheosis.mixin.TemptGoalMixin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class TemptingEnchant extends Enchantment {

    public TemptingEnchant() {
        super(Rarity.UNCOMMON, EnchModule.HOE, new EquipmentSlot[0]);
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 0;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return 200;
    }

    /**
     * Allows checking if an item with Tempting can make an animal follow.
     * Called from {@link TemptGoal#shouldFollow(LivingEntity)}
     * Injected by {@link TemptGoalMixin}
     */
    public boolean shouldFollow(LivingEntity target) {
        return this.shouldFollow(target.getMainHandItem()) || this.shouldFollow(target.getOffhandItem());
    }

    /**
     * Checks if a stack has the tempting enchantment.<br>
     * Explicitly checks instanceof HoeItem since this code path is extremely hot, and getEnchantmentLevel is expensive.
     */
    private boolean shouldFollow(ItemStack stack) {
        return stack.getItem() instanceof HoeItem && stack.getEnchantmentLevel(this) > 0;
    }

}
