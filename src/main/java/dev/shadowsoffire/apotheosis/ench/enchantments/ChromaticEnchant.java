package dev.shadowsoffire.apotheosis.ench.enchantments;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import net.minecraft.Util;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class ChromaticEnchant extends Enchantment {

    private static final Map<DyeColor, ItemLike> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), map -> {
        map.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
        map.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
        map.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
        map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        map.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
        map.put(DyeColor.LIME, Blocks.LIME_WOOL);
        map.put(DyeColor.PINK, Blocks.PINK_WOOL);
        map.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
        map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        map.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
        map.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
        map.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
        map.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
        map.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
        map.put(DyeColor.RED, Blocks.RED_WOOL);
        map.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
    });

    public ChromaticEnchant() {
        super(Rarity.UNCOMMON, EnchModule.SHEARS, new EquipmentSlot[] { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }

    @Override
    public int getMinCost(int pLevel) {
        return 40;
    }

    public List<ItemStack> molestSheepItems(Sheep sheep, ItemStack shears, List<ItemStack> items) {
        if (shears.getEnchantmentLevel(this) > 0) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).is(ItemTags.WOOL)) {
                    items.set(i, new ItemStack(ITEM_BY_DYE.get(DyeColor.byId(sheep.random.nextInt(16)))));
                }
            }
        }
        return items;
    }

}
