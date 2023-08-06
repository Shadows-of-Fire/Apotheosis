package dev.shadowsoffire.apotheosis.ench.enchantments;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.ench.EnchModuleEvents.TridentGetter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SpearfishingEnchant extends Enchantment {

    public SpearfishingEnchant() {
        super(Rarity.UNCOMMON, EnchantmentCategory.TRIDENT, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    @Override
    public int getMinCost(int pEnchantmentLevel) {
        return 12 + (pEnchantmentLevel - 1) * 18;
    }

    @Override
    public int getMaxCost(int pEnchantmentLevel) {
        return 200;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    @Override
    public int getMaxLevel() {
        return 5;
    }

    public void addFishes(LivingDropsEvent e) {
        DamageSource src = e.getSource();
        if (src.getDirectEntity() instanceof ThrownTrident trident) {
            if (trident.level().isClientSide) return;
            ItemStack triStack = ((TridentGetter) trident).getTridentItem();
            int level = triStack.getEnchantmentLevel(this);
            if (trident.random.nextFloat() < 3.5F * level) {
                Entity dead = e.getEntity();
                e.getDrops().add(new ItemEntity(trident.level(), dead.getX(), dead.getY(), dead.getZ(),
                    new ItemStack(ForgeRegistries.ITEMS.tags().getTag(Apoth.Tags.SPEARFISHING_DROPS).getRandomElement(trident.random).orElse(Items.AIR), 1 + trident.random.nextInt(3))));
            }
        }
    }

}
