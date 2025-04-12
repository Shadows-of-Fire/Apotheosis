package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.List;
import java.util.stream.Stream;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class WithdrawalExtension implements ISmithingCategoryExtension<WithdrawalRecipe> {

    private static final List<ItemStack> DUMMY_INPUTS = Stream.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW).map(ItemStack::new).toList();

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(WithdrawalRecipe recipe, T acc) {

    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(WithdrawalRecipe recipe, T acc) {
        List<ItemStack> outputs = DUMMY_INPUTS.stream().map(ItemStack::copy).map(s -> {
            SocketHelper.setSockets(s, 1);
            Gem gem = GemRegistry.INSTANCE.getRandomItem(GenContext.forPlayer(Minecraft.getInstance().player), g -> g.isValidIn(s, ItemStack.EMPTY, Purity.FLAWED));
            if (gem != null) {
                ItemStack gemStack = gem.toStack(Purity.FLAWED);
                return SocketHelper.socketGemInItem(s, gemStack);
            }
            return s;
        }).toList();
        acc.addItemStacks(outputs);
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(WithdrawalRecipe recipe, T acc) {
        acc.addItemStack(new ItemStack(Apoth.Items.SIGIL_OF_WITHDRAWAL));
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(WithdrawalRecipe recipe, T acc) {
        List<ItemStack> outputs = DUMMY_INPUTS.stream().map(ItemStack::copy).map(s -> {
            SocketHelper.setSockets(s, 1);
            Component text = Apotheosis.lang("text", "gems_returned").withStyle(ChatFormatting.AQUA);
            s.set(DataComponents.LORE, new ItemLore(List.of(text), List.of(text)));
            return s;
        }).toList();
        acc.addItemStacks(outputs);
    }

}
