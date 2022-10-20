package shadows.apotheosis.garden.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;

@JeiPlugin
public class GardenJEIPlugin implements IModPlugin {

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enableGarden) return;
		reg.addIngredientInfo(new ItemStack(Apoth.Items.ENDER_LEAD.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.ender_lead"));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "garden");
	}

}