package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.List;
import java.util.stream.Stream;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public class UnnamingExtension implements ISmithingCategoryExtension<UnnamingRecipe> {

    private static final List<Item> DUMMY_ITEMS = Stream.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW).toList();

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(UnnamingRecipe recipe, T acc) {

    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(UnnamingRecipe recipe, T acc) {
        List<ItemStack> outputs = getDummyItems().toList();
        acc.addItemStacks(outputs);
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(UnnamingRecipe recipe, T acc) {
        acc.addItemStack(new ItemStack(Apoth.Items.SIGIL_OF_UNNAMING));
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(UnnamingRecipe recipe, T acc) {
        List<ItemStack> outputs = getDummyItems()
            .map(stack -> {
                LootRarity rarity = AffixHelper.getRarity(stack).get();
                Component comp = Component.translatable("%2$s", "", "").withStyle(Style.EMPTY.withColor(rarity.color()).withItalic(false));
                AffixHelper.setName(stack, comp);
                return stack;
            })
            .toList();
        acc.addItemStacks(outputs);
    }

    private Stream<ItemStack> getDummyItems() {
        RandomSource rand = new LegacyRandomSource(0);
        LootRarity rarity = RarityRegistry.getSortedRarities().getLast();
        return DUMMY_ITEMS.stream().map(ItemStack::new)
            .map(stack -> {
                LootController.createLootItem(stack, rarity, GenContext.dummy(rand));
                return stack;
            });
    }

}
