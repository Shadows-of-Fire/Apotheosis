package shadows.apotheosis.adventure.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.socket.GemItem;

@JeiPlugin
public class AdventureJEIPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "adventure_module");
	}

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		ItemStack gem = new ItemStack(Apoth.Items.GEM.get());
		GemItem.setStoredBonus(gem, Attributes.LUCK, new AttributeModifier("debug", 9999, Operation.ADDITION));
		reg.addIngredientInfo(gem, VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.socketing"));

		reg.addIngredientInfo(new ItemStack(Apoth.Items.GEM_DUST.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_crushing"));
		reg.addIngredientInfo(new ItemStack(Apoth.Items.VIAL_OF_EXTRACTION.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_extraction"));
		reg.addIngredientInfo(new ItemStack(Apoth.Items.VIAL_OF_EXPULSION.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_expulsion"));
		reg.addIngredientInfo(AdventureModule.RARITY_MATERIALS.values().stream().map(ItemStack::new).toList(), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.salvaging"));
	}

}