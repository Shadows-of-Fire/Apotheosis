package shadows.apotheosis.garden.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

@JeiPlugin
public class GardenJEIPlugin implements IModPlugin {

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enableGarden) return;
		reg.addIngredientInfo(new ItemStack(ApotheosisObjects.FARMERS_LEASH), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.farmleash"));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "garden");
	}

}