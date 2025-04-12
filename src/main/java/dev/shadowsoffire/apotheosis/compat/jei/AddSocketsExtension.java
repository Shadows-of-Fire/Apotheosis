package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.List;
import java.util.stream.Stream;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class AddSocketsExtension implements ISmithingCategoryExtension<AddSocketsRecipe> {

    private static final List<ItemStack> DUMMY_INPUTS = Stream.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW).map(ItemStack::new).toList();

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(AddSocketsRecipe recipe, T acc) {

    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(AddSocketsRecipe recipe, T acc) {
        acc.addItemStacks(DUMMY_INPUTS);
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(AddSocketsRecipe recipe, T acc) {
        acc.addIngredients(recipe.getInput());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(AddSocketsRecipe recipe, T acc) {
        List<ItemStack> outputs = DUMMY_INPUTS.stream().map(ItemStack::copy).map(s -> {
            SocketHelper.setSockets(s, 1);
            Component text = Apotheosis.lang("text", "socket_limit", recipe.getMaxSockets()).withStyle(ChatFormatting.AQUA);
            s.set(DataComponents.LORE, new ItemLore(List.of(text), List.of(text)));
            return s;
        }).toList();
        acc.addItemStacks(outputs);
    }

}
