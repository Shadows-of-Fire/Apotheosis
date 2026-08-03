package dev.shadowsoffire.apotheosis.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Simple item extension that always has the enchantment glint.
 */
public class GlowyItem extends Item {

    public GlowyItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

}
