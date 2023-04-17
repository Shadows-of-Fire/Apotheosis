package shadows.apotheosis.adventure.compat;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.RecipeTypes;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.AdventureModule.ApothUpgradeRecipe;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import shadows.apotheosis.adventure.affix.socket.AddSocketsRecipe;
import shadows.apotheosis.adventure.affix.socket.SocketHelper;

@JeiPlugin
public class AdventureJEIPlugin implements IModPlugin {

	public static final RecipeType<UpgradeRecipe> APO_SMITHING = RecipeType.create(Apotheosis.MODID, "smithing", ApothUpgradeRecipe.class);
	public static final RecipeType<SalvagingRecipe> SALVAGING = RecipeType.create(Apotheosis.MODID, "salvaging", SalvagingRecipe.class);

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "adventure_module");
	}

	@Override
	@SuppressWarnings("removal")
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enableAdventure) return;
		ItemStack gem = new ItemStack(Apoth.Items.GEM.get());
		shadows.apotheosis.adventure.affix.socket.gem.LegacyGem.setStoredBonus(gem, Attributes.LUCK, new AttributeModifier("debug", 9999, Operation.ADDITION));
		reg.addIngredientInfo(gem, VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.socketing"));

		reg.addIngredientInfo(new ItemStack(Apoth.Items.GEM_DUST.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_crushing"));
		reg.addIngredientInfo(new ItemStack(Apoth.Items.VIAL_OF_EXTRACTION.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_extraction"));
		reg.addIngredientInfo(new ItemStack(Apoth.Items.VIAL_OF_EXPULSION.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_expulsion"));
		reg.addIngredientInfo(AdventureModule.RARITY_MATERIALS.values().stream().map(ItemStack::new).toList(), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.salvaging"));
		ApothSmithingCategory.registerExtension(AddSocketsRecipe.class, new AddSocketsExtension());
		reg.addRecipes(APO_SMITHING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING).stream().filter(r -> r instanceof ApothUpgradeRecipe).toList());
		reg.addRecipes(SALVAGING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeTypes.SALVAGING));
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		if (!Apotheosis.enableAdventure) return;
		reg.addRecipeCategories(new ApothSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
		reg.addRecipeCategories(new SalvagingCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		if (!Apotheosis.enableAdventure) return;
		reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), APO_SMITHING);
		reg.addRecipeCatalyst(new ItemStack(Apoth.Blocks.SALVAGING_TABLE.get()), SALVAGING);
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
			font.draw(stack, text, (125 / 2) - font.width(text) / 2, 23, 0);
		}

	}

}