package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.compat.jei.PotionCharmExtension.PotionCharmSubtypes;
import dev.shadowsoffire.apotheosis.recipe.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.ReactiveSmithingRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import dev.shadowsoffire.apothic_enchanting.compat.InfusionRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class AdventureJEIPlugin implements IModPlugin {

    public static final RecipeType<SmithingRecipe> APO_SMITHING = RecipeType.create(Apotheosis.MODID, "smithing", ApothSmithingRecipe.class);
    public static final RecipeType<SalvagingRecipe> SALVAGING = RecipeType.create(Apotheosis.MODID, "salvaging", SalvagingRecipe.class);
    public static final RecipeType<GemCuttingRecipe> GEM_CUTTING = RecipeType.create(Apotheosis.MODID, "gem_cutting", PurityUpgradeRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return Apotheosis.loc("adventure_module");
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        Component socketInfo = Component.translatable("info.apotheosis.socketing");
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            for (Purity purity : Purity.ALL_PURITIES) {
                if (purity.isAtLeast(gem.getMinPurity())) {
                    reg.addIngredientInfo(GemRegistry.createGemStack(gem, purity), VanillaTypes.ITEM_STACK, socketInfo);
                }
            }
        }

        reg.addIngredientInfo(new ItemStack(Apoth.Items.GEM_DUST), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_crushing"));
        reg.addIngredientInfo(new ItemStack(Apoth.Items.SIGIL_OF_UNNAMING), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.unnaming"));
        ApothSmithingCategory.registerExtension(AddSocketsRecipe.class, new AddSocketsExtension());

        reg.addRecipes(APO_SMITHING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING).stream()
            .map(RecipeHolder::value)
            .filter(ReactiveSmithingRecipe.class::isInstance)
            .toList());

        List<SalvagingRecipe> salvagingRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeTypes.SALVAGING).stream()
            .sorted(Comparator.comparing(RecipeHolder::id)) // TODO: Prioritize apoth recipes so that the main affix/gem salvaging is always first.
            .map(RecipeHolder::value)
            .toList();
        reg.addRecipes(SALVAGING, salvagingRecipes);

        reg.addRecipes(GEM_CUTTING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeTypes.GEM_CUTTING).stream()
            .map(RecipeHolder::value)
            .toList());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new ApothSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new SalvagingCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new GemCuttingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), APO_SMITHING);
        reg.addRecipeCatalyst(new ItemStack(Apoth.Blocks.SALVAGING_TABLE.value()), SALVAGING);
        reg.addRecipeCatalyst(new ItemStack(Apoth.Blocks.GEM_CUTTING_TABLE.value()), GEM_CUTTING);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(Apoth.Items.GEM.value(), new GemSubtypes());
        reg.registerSubtypeInterpreter(Apoth.Items.POTION_CHARM.value(), new PotionCharmSubtypes());
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration reg) {
        reg.getCraftingCategory().addExtension(PotionCharmRecipe.class, new PotionCharmExtension());

        InfusionRecipeCategory.registerExtension(CharmInfusionRecipe.class, new CharmInfusionExtension());

        reg.getSmithingCategory().addExtension(SizedUpgradeRecipe.class, new SizedUpgradeRecipeExtension());
    }

    private static final List<ItemStack> DUMMY_INPUTS = Arrays.asList(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.TRIDENT).stream().map(ItemStack::new).toList();

    static class AddSocketsExtension implements ApothSmithingCategory.Extension<AddSocketsRecipe> {
        private static final List<ItemStack> DUMMY_OUTPUTS = DUMMY_INPUTS.stream().map(ItemStack::copy).map(s -> {
            SocketHelper.setSockets(s, 1);
            return s;
        }).toList();

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, AddSocketsRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(VanillaTypes.ITEM_STACK, DUMMY_INPUTS);
            builder.addSlot(RecipeIngredientRole.INPUT, 50, 1).addIngredients(recipe.getInput());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1).addItemStacks(DUMMY_OUTPUTS);
        }

        @Override
        public void draw(AddSocketsRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
            Component text = Component.translatable("text.apotheosis.socket_limit", recipe.getMaxSockets());
            Font font = Minecraft.getInstance().font;
            gfx.drawString(font, text, 125 / 2 - font.width(text) / 2, 23, 0, false);
        }

    }

    /**
     * A Gem Stack is unique to JEI based on the Gem's ID and Rarity.
     */
    static class GemSubtypes implements ISubtypeInterpreter<ItemStack> {
        public String apply(ItemStack stack, UidContext context) {
            UnsocketedGem inst = UnsocketedGem.of(stack);
            if (!inst.isValid()) {
                return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            }
            return inst.gem().getId() + "@" + inst.purity().getSerializedName();
        }

        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            return apply(ingredient, context);
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            return apply(ingredient, context);
        }

    }

}
