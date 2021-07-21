package shadows.apotheosis.village.compat;

import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.village.VillageModule;
import shadows.apotheosis.village.fletching.FletchingContainer;

@JeiPlugin
public class VillageJEIPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "village_module");
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		reg.addRecipeCatalyst(new ItemStack(Blocks.FLETCHING_TABLE), FletchingCategory.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		reg.addRecipeCategories(new FletchingCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		reg.addRecipes(Minecraft.getInstance().level.getRecipeManager().getRecipes().stream().filter(r -> r.getType() == VillageModule.FLETCHING).collect(Collectors.toList()), FletchingCategory.UID);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration reg) {
		reg.addRecipeTransferHandler(FletchingContainer.class, FletchingCategory.UID, 1, 3, 4, 9 * 4);
	}

}