package shadows.apotheosis.adventure.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.RecipeTypes;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule.ApothUpgradeRecipe;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingRecipe.OutputData;
import shadows.apotheosis.adventure.affix.socket.AddSocketsRecipe;
import shadows.apotheosis.adventure.affix.socket.SocketHelper;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.apotheosis.adventure.compat.GemCuttingCategory.GemCuttingRecipe;
import shadows.apotheosis.adventure.loot.LootRarity;

@JeiPlugin
public class AdventureJEIPlugin implements IModPlugin {

    public static final RecipeType<UpgradeRecipe> APO_SMITHING = RecipeType.create(Apotheosis.MODID, "smithing", ApothUpgradeRecipe.class);
    public static final RecipeType<SalvagingRecipe> SALVAGING = RecipeType.create(Apotheosis.MODID, "salvaging", SalvagingRecipe.class);
    public static final RecipeType<GemCuttingRecipe> GEM_CUTTING = RecipeType.create(Apotheosis.MODID, "gem_cutting", GemCuttingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Apotheosis.MODID, "adventure_module");
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration reg) {
        if (!Apotheosis.enableAdventure) return;
        ItemStack gem = new ItemStack(Apoth.Items.GEM.get());
        Gem gemObj = GemManager.INSTANCE.getRandomItem(new LegacyRandomSource(1854));
        GemItem.setGem(gem, gemObj);
        GemItem.setLootRarity(gem, gemObj.getMaxRarity());
        reg.addIngredientInfo(gem, VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.socketing"));

        reg.addIngredientInfo(new ItemStack(Apoth.Items.GEM_DUST.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_crushing"));
        reg.addIngredientInfo(new ItemStack(Apoth.Items.VIAL_OF_EXTRACTION.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_extraction"));
        reg.addIngredientInfo(new ItemStack(Apoth.Items.VIAL_OF_EXPULSION.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_expulsion"));
        reg.addIngredientInfo(new ItemStack(Apoth.Items.VIAL_OF_UNNAMING.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.unnaming"));
        ApothSmithingCategory.registerExtension(AddSocketsRecipe.class, new AddSocketsExtension());
        reg.addRecipes(APO_SMITHING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING).stream().filter(r -> r instanceof ApothUpgradeRecipe).toList());
        List<SalvagingRecipe> salvagingRecipes = new ArrayList<>(Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeTypes.SALVAGING));
        salvagingRecipes.sort(Comparator.comparingInt(recipe -> recipe.getOutputs().stream().mapToInt(OutputData::getMax).max().orElse(0)));
        reg.addRecipes(SALVAGING, salvagingRecipes);

        List<GemCuttingRecipe> gemCutRecipes = new ArrayList<>();
        for (Gem g : GemManager.INSTANCE.getValues()) {
            LootRarity r = LootRarity.COMMON;
            LootRarity max = LootRarity.ANCIENT;
            while (r != max) {
                if (g.clamp(r) == r) gemCutRecipes.add(new GemCuttingRecipe(g, r));
                r = r.next();
            }
        }
        reg.addRecipes(GEM_CUTTING, gemCutRecipes);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        if (!Apotheosis.enableAdventure) return;
        reg.addRecipeCategories(new ApothSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new SalvagingCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new GemCuttingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        if (!Apotheosis.enableAdventure) return;
        reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), APO_SMITHING);
        reg.addRecipeCatalyst(new ItemStack(Apoth.Blocks.SALVAGING_TABLE.get()), SALVAGING);
        reg.addRecipeCatalyst(new ItemStack(Apoth.Blocks.GEM_CUTTING_TABLE.get()), GEM_CUTTING);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        if (!Apotheosis.enableAdventure) return;
        reg.registerSubtypeInterpreter(Apoth.Items.GEM.get(), new GemSubtypes());
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
        public void draw(AddSocketsRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
            Component text = Component.translatable("text.apotheosis.socket_limit", recipe.getMaxSockets());
            Font font = Minecraft.getInstance().font;
            font.draw(stack, text, 125 / 2 - font.width(text) / 2, 23, 0);
        }

    }

    /**
     * A Gem Stack is unique to JEI based on the Gem's ID and Rarity.
     */
    static class GemSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {

        @Override
        public String apply(ItemStack stack, UidContext context) {
            Gem gem = GemItem.getGem(stack);
            LootRarity rarity = GemItem.getLootRarity(stack);
            if (gem == null) return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            return gem.getId() + "@" + rarity.id();
        }

    }

}
