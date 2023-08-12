package dev.shadowsoffire.apotheosis.ench.enchantments.masterwork;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.Ench;
import dev.shadowsoffire.apotheosis.ench.asm.EnchHooks;
import dev.shadowsoffire.apotheosis.mixin.CrossbowItemMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class CrescendoEnchant extends Enchantment {

    public CrescendoEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.CROSSBOW, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 55 + (level - 1) * 30; // 50/80/110/140/170
    }

    @Override
    public int getMaxCost(int level) {
        return 200;
    }

    @Override
    public Component getFullname(int level) {
        return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_GREEN);
    }

    private static ThreadLocal<ListTag> nbt = new ThreadLocal<>();

    /**
     * Early hook for the Crescendo of Bolts enchantment.
     * This is required to retain the ammunition's nbt data, as it is thrown away before {@link EnchHooks#onArrowFired}.
     * Injected by {@link CrossbowItemMixin}
     */
    public static void preArrowFired(ItemStack crossbow) {
        if (!Apotheosis.enableEnch) return;
        int level = crossbow.getEnchantmentLevel(Ench.Enchantments.CRESCENDO.get());
        if (level > 0) {
            nbt.set(crossbow.getTag().getList("ChargedProjectiles", Tag.TAG_COMPOUND).copy());
        }
    }

    /**
     * Handles the usage of the Crescendo of Bolts enchantment.
     * The enchantment gives the crossbow extra shots per charge, one per enchantment level.
     * Called from {@link CrossbowItem#use}, before the first return.
     * Injected by {@link CrossbowItemMixin}
     */
    public static void onArrowFired(ItemStack crossbow) {
        if (!Apotheosis.enableEnch) return;
        int level = crossbow.getEnchantmentLevel(Ench.Enchantments.CRESCENDO.get());
        if (level > 0 && nbt.get() != null) {
            int shots = crossbow.getTag().getInt("shots");
            if (shots < level) {
                crossbow.getTag().putInt("shots", shots + 1);
                CrossbowItem.setCharged(crossbow, true);
                crossbow.getTag().put("ChargedProjectiles", nbt.get());
            }
            else {
                crossbow.getTag().remove("shots");
            }
            nbt.set(null);
        }
    }

    /**
     * Arrow fired hook for the Crescendo of Bolts enchantment.
     * This is required to mark generated arrows as creative-only so arrows are not duplicated.
     * Injected by {@link CrossbowItemMixin}
     */
    public static void markGeneratedArrows(Projectile arrow, ItemStack crossbow) {
        if (!Apotheosis.enableEnch) return;
        if (crossbow.getTag().getInt("shots") > 0 && arrow instanceof AbstractArrow arr) {
            arr.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
    }
}
