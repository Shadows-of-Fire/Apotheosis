package dev.shadowsoffire.apotheosis.spawn.enchantment;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.apotheosis.spawn.SpawnerModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public class CapturingEnchant extends Enchantment {

    public CapturingEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 28 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) || EnchModule.AXE.canEnchant(stack.getItem());
    }

    public void handleCapturing(LivingDropsEvent e) {
        Entity killer = e.getSource().getEntity();
        if (killer instanceof LivingEntity living) {
            int level = living.getMainHandItem().getEnchantmentLevel(Apoth.Enchantments.CAPTURING.get());
            LivingEntity killed = e.getEntity();
            if (SpawnerModule.bannedMobs.contains(EntityType.getKey(killed.getType()))) return;
            if (killed.level().random.nextFloat() < level / 250F) {
                ItemStack egg = new ItemStack(ForgeSpawnEggItem.fromEntityType(killed.getType()));
                e.getDrops().add(new ItemEntity(killed.level(), killed.getX(), killed.getY(), killed.getZ(), egg));
            }
        }
    }

}
