package dev.shadowsoffire.apotheosis.ench.objects;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class GlowyBlockItem extends BlockItem {

    public GlowyBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    public static class GlowyItem extends Item {

        public GlowyItem(Properties pProperties) {
            super(pProperties);
        }

        @Override
        public boolean isFoil(ItemStack pStack) {
            return true;
        }

    }

}
