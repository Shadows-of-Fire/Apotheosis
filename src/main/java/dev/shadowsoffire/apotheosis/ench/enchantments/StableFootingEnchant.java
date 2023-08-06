package dev.shadowsoffire.apotheosis.ench.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class StableFootingEnchant extends Enchantment {

    public StableFootingEnchant() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[] { EquipmentSlot.FEET });
    }

    @Override
    public int getMinCost(int level) {
        return 40;
    }

    @Override
    public int getMaxCost(int level) {
        return 200;
    }

    public void breakSpeed(PlayerEvent.BreakSpeed e) {
        Player p = e.getEntity();
        if (!p.onGround() && EnchantmentHelper.getEnchantmentLevel(this, p) > 0) {
            if (e.getOriginalSpeed() < e.getNewSpeed() * 5) e.setNewSpeed(e.getNewSpeed() * 5F);
        }
    }

}
