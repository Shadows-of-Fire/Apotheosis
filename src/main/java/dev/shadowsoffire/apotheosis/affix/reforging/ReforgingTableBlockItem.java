package dev.shadowsoffire.apotheosis.affix.reforging;

import java.util.Comparator;
import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

public class ReforgingTableBlockItem extends BlockItem {

    public ReforgingTableBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Component.translatable("block.apotheosis.reforging_table.desc").withStyle(ChatFormatting.GRAY));

        LootRarity max = this.computeMaxRarity();
        if (max == null) {
            return;
        }

        LootRarity globalMax = RarityRegistry.getSortedRarities().stream()
            .max(Comparator.comparingInt(LootRarity::sortIndex))
            .orElse(null);

        if (globalMax != null && max.sortIndex() < globalMax.sortIndex()) {
            tooltip.accept(Component.translatable("block.apotheosis.reforging_table.desc2", max.toComponent()).withStyle(ChatFormatting.GRAY));
        }
    }

    @SuppressWarnings("deprecation")
    private LootRarity computeMaxRarity() {
        LootRarity best = null;
        for (RecipeHolder<ReforgingRecipe> holder : ReforgingRecipeCache.all()) {
            ReforgingRecipe recipe = holder.value();
            if (!recipe.tables().contains(this.getBlock().builtInRegistryHolder()) || !recipe.rarity().isBound()) {
                continue;
            }
            LootRarity r = recipe.rarity().get();
            if (best == null || r.sortIndex() > best.sortIndex()) {
                best = r;
            }
        }
        return best;
    }
}
