package shadows.garden.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;

@JEIPlugin
public class GardenJEIPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry reg) {
		if (!Apotheosis.enableGarden) return;
		reg.addIngredientInfo(new ItemStack(ApotheosisObjects.FARMERS_LEASH), VanillaTypes.ITEM, "info.apotheosis.farmleash");
	}

}
