package shadows.apotheosis.deadly.compat;

import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.recipe.SoulfireCookingRecipe;

@JeiPlugin
public class DeadlyJEIPlugin implements IModPlugin {

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		reg.addRecipeCategories(new SoulfireCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		reg.addRecipes(Minecraft.getInstance().level.getRecipeManager().getRecipes().stream().filter(r -> r instanceof SoulfireCookingRecipe).collect(Collectors.toList()), SoulfireCategory.UID);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		reg.addRecipeCatalyst(new ItemStack(Blocks.SOUL_CAMPFIRE), SoulfireCategory.UID);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "deadly");
	}

}
