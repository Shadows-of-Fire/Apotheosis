package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

@Mixin(ShearsItem.class)
public class ShearsItemMixin extends Item {

    public ShearsItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
        if (!Apotheosis.enableEnch) return super.canApplyAtEnchantingTable(stack, ench);
        return super.canApplyAtEnchantingTable(stack, ench) || ench == Enchantments.UNBREAKING || ench == Enchantments.BLOCK_EFFICIENCY || ench == Enchantments.BLOCK_FORTUNE;
    }

    @Override
    public int getEnchantmentValue() {
        return Apotheosis.enableEnch ? 15 : 0;
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {
        return Apotheosis.enableEnch && this == Items.SHEARS ? Apotheosis.MODID : super.getCreatorModId(itemStack);
    }

}
