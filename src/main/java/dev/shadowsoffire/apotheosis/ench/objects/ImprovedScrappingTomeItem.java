package dev.shadowsoffire.apotheosis.ench.objects;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;

public class ImprovedScrappingTomeItem extends BookItem {

    static Random rand = new Random();

    public ImprovedScrappingTomeItem() {
        super(new Item.Properties());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flagIn) {
        if (stack.isEnchanted()) return;
        tooltip.add(Component.translatable("info.apotheosis.improved_scrap_tome").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.apotheosis.improved_scrap_tome2").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    public static boolean updateAnvil(AnvilUpdateEvent ev) {
        ItemStack weapon = ev.getLeft();
        ItemStack book = ev.getRight();
        if (!(book.getItem() instanceof ImprovedScrappingTomeItem) || book.isEnchanted() || !weapon.isEnchanted()) return false;

        Map<Enchantment, Integer> wepEnch = EnchantmentHelper.getEnchantments(weapon);
        ItemStack out = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(wepEnch, out);
        ev.setMaterialCost(1);
        ev.setCost(wepEnch.size() * 10);
        ev.setOutput(out);
        return true;
    }
}
