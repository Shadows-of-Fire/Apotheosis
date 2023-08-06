package dev.shadowsoffire.apotheosis.ench.enchantments.masterwork;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public class KnowledgeEnchant extends Enchantment {

    public KnowledgeEnchant() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMinCost(int level) {
        return 55 + (level - 1) * 45;
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
    public Component getFullname(int level) {
        return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_GREEN);
    }

    public void drops(Player p, LivingDropsEvent e) {
        int knowledge = p.getMainHandItem().getEnchantmentLevel(this);
        if (knowledge > 0 && !(e.getEntity() instanceof Player)) {
            int items = 0;
            for (ItemEntity i : e.getDrops())
                items += i.getItem().getCount();
            if (items > 0) e.getDrops().clear();
            items *= knowledge * 25;
            Entity ded = e.getEntity();
            while (items > 0) {
                int i = ExperienceOrb.getExperienceValue(items);
                items -= i;
                p.level().addFreshEntity(new ExperienceOrb(p.level(), ded.getX(), ded.getY(), ded.getZ(), i));
            }
        }
    }
}
