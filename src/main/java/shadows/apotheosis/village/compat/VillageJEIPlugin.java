package shadows.apotheosis.village.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.Apoth.RecipeTypes;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.village.fletching.FletchingContainer;
import shadows.apotheosis.village.fletching.FletchingRecipe;

@JeiPlugin
public class VillageJEIPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "village_module");
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		if (!Apotheosis.enableVillage) return;
		reg.addRecipeCatalyst(new ItemStack(Blocks.FLETCHING_TABLE), FletchingCategory.TYPE);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		if (!Apotheosis.enableVillage) return;
		reg.addRecipeCategories(new FletchingCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enableVillage) return;
		reg.addRecipes(FletchingCategory.TYPE, Minecraft.getInstance().level.getRecipeManager().getRecipes().stream().filter(r -> r.getType() == RecipeTypes.FLETCHING).map(r -> (FletchingRecipe) r).toList());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration reg) {
		if (!Apotheosis.enableVillage) return;
		reg.addRecipeTransferHandler(FletchingContainer.class, FletchingCategory.TYPE, 1, 3, 4, 9 * 4);
	}

}