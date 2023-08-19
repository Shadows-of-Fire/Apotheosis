package shadows.apotheosis.adventure.compat;

import java.util.Arrays;

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
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.Blocks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingBlock;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingMenu;
import shadows.apotheosis.adventure.compat.GemCuttingCategory.GemCuttingRecipe;
import shadows.apotheosis.adventure.loot.LootRarity;

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
        builder.addSlot(RecipeIngredientRole.INPUT, 46, 14).addIngredient(VanillaTypes.ITEM_STACK, recipe.gem);
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 14).addIngredient(VanillaTypes.ITEM_STACK, recipe.dust);
        builder.addSlot(RecipeIngredientRole.INPUT, 46, 57).addIngredient(VanillaTypes.ITEM_STACK, recipe.gem);
        builder.addSlot(RecipeIngredientRole.INPUT, 87, 14).addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.materials));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 129, 14).addIngredient(VanillaTypes.ITEM_STACK, recipe.out);
    }

    public static class GemCuttingRecipe {

        protected final ItemStack out, gem, dust;
        protected final ItemStack[] materials;

        public GemCuttingRecipe(Gem gem, LootRarity rarity) {
            this.out = GemManager.createGemStack(gem, rarity.next());
            this.gem = GemManager.createGemStack(gem, rarity);
            this.dust = new ItemStack(Apoth.Items.GEM_DUST.get(), GemCuttingMenu.getDustCost(rarity));
            LootRarity min = LootRarity.COMMON;
            if (rarity == min) {
                this.materials = new ItemStack[2];
                this.materials[0] = new ItemStack(rarity.getMaterial().getItem(), GemCuttingMenu.STD_MAT_COST);
                this.materials[1] = new ItemStack(rarity.next().getMaterial().getItem(), GemCuttingMenu.NEXT_MAT_COST);
            }
            else if (rarity.next() == LootRarity.ANCIENT) { // Special case ancient because the material is unavailable.
                this.materials = new ItemStack[2];
                this.materials[0] = new ItemStack(rarity.prev().getMaterial().getItem(), GemCuttingMenu.PREV_MAT_COST);
                this.materials[1] = new ItemStack(rarity.getMaterial().getItem(), GemCuttingMenu.STD_MAT_COST);
            }
            else {
                this.materials = new ItemStack[3];
                this.materials[0] = new ItemStack(rarity.prev().getMaterial().getItem(), GemCuttingMenu.PREV_MAT_COST);
                this.materials[1] = new ItemStack(rarity.getMaterial().getItem(), GemCuttingMenu.STD_MAT_COST);
                this.materials[2] = new ItemStack(rarity.next().getMaterial().getItem(), GemCuttingMenu.NEXT_MAT_COST);
            }
        }

    }

}
