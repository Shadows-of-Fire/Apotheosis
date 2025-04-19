package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.List;
import java.util.stream.Stream;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public class MaliceExtension implements ISmithingCategoryExtension<MaliceRecipe> {

    private static final List<Item> DUMMY_ITEMS = Stream.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW).toList();

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(MaliceRecipe recipe, T acc) {

    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(MaliceRecipe recipe, T acc) {
        List<ItemStack> outputs = getDummyItems().toList();
        acc.addItemStacks(outputs);
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(MaliceRecipe recipe, T acc) {
        acc.addItemStack(new ItemStack(Apoth.Items.SIGIL_OF_MALICE));
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(MaliceRecipe recipe, T acc) {
        List<ItemStack> outputs = getDummyItems()
            .map(stack -> {
                stack.set(Apoth.Components.MALICE_MARKER, true);
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
                AffixHelper.setName(stack, Apotheosis.lang("text", "any_affix_item").withStyle(Style.EMPTY.withColor(rarity.color()).withItalic(false)));
                return stack;
            });
    }

}
