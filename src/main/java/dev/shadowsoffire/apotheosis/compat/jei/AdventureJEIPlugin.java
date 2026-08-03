package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipeCache;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.compat.enchanting.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.compat.jei.PotionCharmExtension.PotionCharmSubtypes;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.recipe.SupremacyRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipeCache;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseScreen;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import dev.shadowsoffire.apothic_enchanting.compat.InfusionRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class AdventureJEIPlugin implements IModPlugin {

    public static final IRecipeType<SmithingRecipe> APO_SMITHING = IRecipeType.create(Apotheosis.MODID, "smithing", ApothSmithingRecipe.class);
    public static final IRecipeType<SalvagingRecipe> SALVAGING = IRecipeType.create(Apotheosis.MODID, "salvaging", SalvagingRecipe.class);
    public static final IRecipeType<GemCuttingRecipe> GEM_CUTTING = IRecipeType.create(Apotheosis.MODID, "gem_cutting", PurityUpgradeRecipe.class);

    @Override
    public Identifier getPluginUid() {
        return Apotheosis.loc("adventure_module");
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        Component socketInfo = Component.translatable("info.apotheosis.socketing");
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            for (Purity purity : Purity.ALL_PURITIES) {
                if (purity.isAtLeast(gem.getMinPurity())) {
                    reg.addIngredientInfo(gem.toStack(purity), VanillaTypes.ITEM_STACK, socketInfo);
                }
            }
        }

        reg.addIngredientInfo(new ItemStack(Apoth.Items.GEM_DUST), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_crushing"));
        reg.addIngredientInfo(new ItemStack(Apoth.Items.SIGIL_OF_UNNAMING), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.unnaming"));

        reg.addRecipes(SALVAGING, SalvagingRecipeCache.all().stream()
            .map(RecipeHolder::value)
            .toList());

        reg.addRecipes(GEM_CUTTING, GemCuttingRecipeCache.all().stream()
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
        reg.addCraftingStation(APO_SMITHING, new ItemStack(Blocks.SMITHING_TABLE));
        reg.addCraftingStation(SALVAGING, new ItemStack(Apoth.Blocks.SALVAGING_TABLE.value()));
        reg.addCraftingStation(GEM_CUTTING, new ItemStack(Apoth.Blocks.GEM_CUTTING_TABLE.value()));
    }

    /**
     * Registers the exclusion zones for screens with areas outside their normal bounds, which prevents the JEI overlay
     * (and the overlays of recipe viewers that consume JEI plugins through a bridge, such as EMI) from rendering below them.
     */
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration reg) {
        reg.addGuiContainerHandler(GemCaseScreen.class, new IGuiContainerHandler<GemCaseScreen>(){
            @Override
            public List<Rect2i> getGuiExtraAreas(GemCaseScreen screen) {
                return screen.getExclusionAreas();
            }
        });
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(Apoth.Items.GEM.value(), new GemSubtypes());
        reg.registerSubtypeInterpreter(Apoth.Items.POTION_CHARM.value(), new PotionCharmSubtypes());
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration reg) {
        reg.getCraftingCategory().addExtension(PotionCharmRecipe.class, new PotionCharmExtension());
        if (ApothicEnchantingCompat.isLoaded()) {
            AEExtensions.register();
        }
        reg.getSmithingCategory().addExtension(SizedUpgradeRecipe.class, new SizedUpgradeRecipeExtension());
        reg.getSmithingCategory().addExtension(AddSocketsRecipe.class, new AddSocketsExtension());
        reg.getSmithingCategory().addExtension(WithdrawalRecipe.class, new WithdrawalExtension());
        reg.getSmithingCategory().addExtension(UnnamingRecipe.class, new UnnamingExtension());
        reg.getSmithingCategory().addExtension(MaliceRecipe.class, new MaliceExtension());
        reg.getSmithingCategory().addExtension(SupremacyRecipe.class, new SupremacyExtension());
    }

    /**
     * Holder class that prevents classloading Apothic Enchanting types when it is not installed.
     */
    private static class AEExtensions {

        static void register() {
            InfusionRecipeCategory.registerExtension(CharmInfusionRecipe.class, new CharmInfusionExtension());
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
            return this.apply(ingredient, context);
        }

    }

}
