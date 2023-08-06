package dev.shadowsoffire.apotheosis.ench.enchantments.masterwork;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ScavengerEnchant extends Enchantment {

    public ScavengerEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMinCost(int level) {
        return 55 + level * level * 12; // 57 / 103 / 163
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

    private static final MethodHandle dropFromLootTable;
    static {
        Method m = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_7625_", DamageSource.class, boolean.class);
        try {
            m.setAccessible(true);
            dropFromLootTable = MethodHandles.lookup().unreflect(m);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("LivingEntity#dropFromLootTable not located!");
        }
    }

    public void drops(Player p, LivingDropsEvent e) throws Throwable {
        if (p.level().isClientSide) return;
        int scavenger = p.getMainHandItem().getEnchantmentLevel(this);
        if (scavenger > 0 && p.level().random.nextInt(100) < scavenger * 2.5F) {
            e.getEntity().captureDrops(new ArrayList<>());
            dropFromLootTable.invoke(e.getEntity(), e.getSource(), true);
            e.getDrops().addAll(e.getEntity().captureDrops(null));
        }
    }

}
