package dev.shadowsoffire.apotheosis.adventure.compat;

import java.util.Arrays;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Blocks;
import dev.shadowsoffire.apotheosis.adventure.compat.GemCuttingCategory.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting.GemCuttingBlock;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting.GemCuttingMenu;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("removal")
public class GemCuttingCategory implements IRecipeCategory<GemCuttingRecipe> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/gem_cutting_jei.png");

    private final IDrawable background;
    private final IDrawable icon;

    public GemCuttingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 148, 78).addPadding(0, 0, 0, 0).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.GEM_CUTTING_TABLE.get()));
    }

    @Override
    public RecipeType<GemCuttingRecipe> getRecipeType() {
        return AdventureJEIPlugin.GEM_CUTTING;
    }

    @Override
    public Component getTitle() {
        return GemCuttingBlock.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GemCuttingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredient(VanillaTypes.ITEM_STACK, recipe.gem);

        builder.addSlot(RecipeIngredientRole.INPUT, 48, 4).addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.materials));
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 56).addIngredient(VanillaTypes.ITEM_STACK, recipe.gem);
        builder.addSlot(RecipeIngredientRole.INPUT, 76, 56).addIngredient(VanillaTypes.ITEM_STACK, recipe.dust);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).addIngredient(VanillaTypes.ITEM_STACK, recipe.out);
    }

    public static class GemCuttingRecipe {

        protected final ItemStack out, gem, dust;
        protected final ItemStack[] materials;

        public GemCuttingRecipe(Gem gem, LootRarity rarity) {
            this.out = GemRegistry.createGemStack(gem, rarity.next());
            this.gem = GemRegistry.createGemStack(gem, rarity);
            this.dust = new ItemStack(Adventure.Items.GEM_DUST.get(), GemCuttingMenu.getDustCost(rarity));
            LootRarity min = RarityRegistry.getMinRarity().get();
            if (rarity == min) {
                this.materials = new ItemStack[2];
                this.materials[0] = new ItemStack(rarity.getMaterial(), GemCuttingMenu.STD_MAT_COST);
                this.materials[1] = new ItemStack(rarity.next().getMaterial(), GemCuttingMenu.NEXT_MAT_COST);
            }
            else if ("ancient".equals(RarityRegistry.INSTANCE.getKey(rarity.next()).getPath())) { // Special case ancient because the material is unavailable.
                this.materials = new ItemStack[2];
                this.materials[0] = new ItemStack(rarity.prev().getMaterial(), GemCuttingMenu.PREV_MAT_COST);
                this.materials[1] = new ItemStack(rarity.getMaterial(), GemCuttingMenu.STD_MAT_COST);
            }
            else {
                this.materials = new ItemStack[3];
                this.materials[0] = new ItemStack(rarity.prev().getMaterial(), GemCuttingMenu.PREV_MAT_COST);
                this.materials[1] = new ItemStack(rarity.getMaterial(), GemCuttingMenu.STD_MAT_COST);
                this.materials[2] = new ItemStack(rarity.next().getMaterial(), GemCuttingMenu.NEXT_MAT_COST);
            }
        }

    }

}
