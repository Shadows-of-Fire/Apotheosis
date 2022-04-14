package shadows.apotheosis.spawn.compat;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.SpawnerModule;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;

@JeiPlugin
public class SpawnerJEIPlugin implements IModPlugin {

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enableSpawner) return;
		List<SpawnerModifier> recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(SpawnerModifier.TYPE);
		recipes.sort((r1, r2) -> r1.getOffhandInput() == Ingredient.EMPTY ? r2.getOffhandInput() == Ingredient.EMPTY ? 0 : -1 : 1);

		reg.addRecipes(recipes, SpawnerCategory.UID);
		if (SpawnerModule.spawnerSilkLevel == 0) {
			reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.spawner.always_drop"));
		} else if (SpawnerModule.spawnerSilkLevel == -1) {
			reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.spawner.no_silk"));
		} else reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.spawner", ((IFormattableTextComponent) Enchantments.SILK_TOUCH.getFullname(SpawnerModule.spawnerSilkLevel)).withStyle(TextFormatting.DARK_BLUE).getString()));

		for (Item i : ForgeRegistries.ITEMS) {
			if (i instanceof SpawnEggItem) reg.addIngredientInfo(new ItemStack(i), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.capturing"));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		if (!Apotheosis.enableSpawner) return;
		reg.addRecipeCatalyst(new ItemStack(Blocks.SPAWNER), this.getPluginUid());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		if (!Apotheosis.enableSpawner) return;
		reg.addRecipeCategories(new SpawnerCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "spawner");
	}

}