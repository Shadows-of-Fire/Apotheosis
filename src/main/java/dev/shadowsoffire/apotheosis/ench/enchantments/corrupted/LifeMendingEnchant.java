package dev.shadowsoffire.apotheosis.ench.enchantments.corrupted;

import java.util.List;

import dev.shadowsoffire.apotheosis.adventure.compat.AdventureCuriosCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.ModList;

public class LifeMendingEnchant extends Enchantment {

    public LifeMendingEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public int getMinCost(int level) {
        return 65 + (level - 1) * 35;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 50;
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) || stack.canPerformAction(ToolActions.SHIELD_BLOCK);
    }

    @Override
    protected boolean checkCompatibility(Enchantment pOther) {
        return super.checkCompatibility(pOther) && pOther != Enchantments.MENDING;
    }

    @Override
    public Component getFullname(int level) {
        return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_RED);
    }

    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    private boolean lifeMend(LivingHealEvent e, ItemStack stack) {
        if (!stack.isEmpty() && stack.isDamaged()) {
            int level = stack.getEnchantmentLevel(this);
            if (level <= 0) return false;
            float cost = 1.0F / (1 << level - 1);
            int maxRestore = Math.min(Mth.floor(e.getAmount() / cost), stack.getDamageValue());
            e.setAmount(e.getAmount() - maxRestore * cost);
            stack.setDamageValue(stack.getDamageValue() - maxRestore);
            return true;
        }
        return false;
    }

    public void lifeMend(LivingHealEvent e) {
        if (e.getEntity().level().isClientSide) return;
        float amt = e.getAmount();
        if (amt <= 0F) return;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = e.getEntity().getItemBySlot(slot);
            if (this.lifeMend(e, stack)) return;

        }
        if (ModList.get().isLoaded("curios")) {
            List<ItemStack> stacks = AdventureCuriosCompat.getLifeMendingCurios(e.getEntity());
            for (ItemStack stack : stacks) {
                if (this.lifeMend(e, stack)) return;
            }
        }
    }

}
