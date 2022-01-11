package shadows.apotheosis.ench.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.table.EnchantingRecipe;
import shadows.apotheosis.ench.table.EnchantingStatManager;
import shadows.apotheosis.ench.table.EnchantingStatManager.Stats;

public class EnchantingCategory implements IRecipeCategory<EnchantingRecipe> {

	public static final ResourceLocation UID = new ResourceLocation(Apotheosis.MODID, "enchanting");
	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/enchanting_jei.png");

	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;

	public EnchantingCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(TEXTURES, 0, 0, 170, 56);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.ENCHANTING_TABLE));
		this.localizedName = new TranslatableComponent("apotheosis.recipes.enchanting");
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public Class<EnchantingRecipe> getRecipeClass() {
		return EnchantingRecipe.class;
	}

	@Override
	public Component getTitle() {
		return this.localizedName;
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public void setIngredients(EnchantingRecipe recipe, IIngredients ing) {
		ing.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
		ing.setInputIngredients(Arrays.asList(recipe.getInput()));
	}

	@Override
	public void setRecipe(IRecipeLayout layout, EnchantingRecipe recipe, IIngredients ing) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		stacks.init(0, true, 5, 5);
		stacks.init(1, false, 36, 5);
		stacks.set(ing);
	}

	@Override
	public void draw(EnchantingRecipe recipe, PoseStack stack, double mouseX, double mouseY) {
		boolean hover = false;
		if (mouseX > 57 && mouseX <= 57 + 108 && mouseY > 4 && mouseY <= 4 + 19) {
			Screen.blit(stack, 57, 4, 0, 0, 71, 108, 19, 256, 256);
			hover = true;
		}

		Font font = Minecraft.getInstance().font;
		Stats stats = recipe.getRequirements();
		font.draw(stack, I18n.get("gui.apotheosis.enchant.eterna"), 16, 26, 0x3DB53D);
		font.draw(stack, I18n.get("gui.apotheosis.enchant.quanta"), 16, 36, 0xFC5454);
		font.draw(stack, I18n.get("gui.apotheosis.enchant.arcana"), 16, 46, 0xA800A8);
		int level = (int) (stats.eterna * 2);

		String s = "" + level;
		int width = 86 - font.width(s);
		EnchantmentNames.getInstance().initSeed(recipe.getId().hashCode());
		FormattedText itextproperties = EnchantmentNames.getInstance().getRandomName(font, width);
		int color = hover ? 16777088 : 6839882;
		drawWordWrap(font, itextproperties, 77, 6, width, color, stack);
		color = 8453920;
		font.drawShadow(stack, s, 77 + width, 13, color);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURES);
		if (stats.eterna > 0) {
			Screen.blit(stack, 56, 27, 0, 56, (int) (stats.eterna / EnchantingStatManager.getAbsoluteMaxEterna() * 110), 5, 256, 256);
		}
		if (stats.quanta > 0) {
			Screen.blit(stack, 56, 37, 0, 61, (int) (stats.quanta / 100 * 110), 5, 256, 256);
		}
		if (stats.arcana > 0) {
			Screen.blit(stack, 56, 47, 0, 66, (int) (stats.arcana / 100 * 110), 5, 256, 256);
		}
		Screen scn = Minecraft.getInstance().screen;
		if (scn == null) return; // We need this to render tooltips, bail if its not there.
		if (hover) {
			List<Component> list = new ArrayList<>();
			list.add(new TranslatableComponent("container.enchant.clue", Apoth.Enchantments.INFUSION.getFullname(recipe.getDisplayLevel()).getString()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		} else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 27 && mouseY <= 27 + 5) {
			List<Component> list = new ArrayList<>();
			list.add(new TranslatableComponent("gui.apotheosis.enchant.eterna").withStyle(ChatFormatting.GREEN));
			list.add(new TranslatableComponent("info.apotheosis.req_et", stats.eterna, EnchantingStatManager.getAbsoluteMaxEterna()).withStyle(ChatFormatting.GRAY));
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		} else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 37 && mouseY <= 37 + 5) {
			List<Component> list = new ArrayList<>();
			list.add(new TranslatableComponent("gui.apotheosis.enchant.quanta").withStyle(ChatFormatting.RED));
			list.add(new TranslatableComponent("info.apotheosis.req_p", stats.quanta).withStyle(ChatFormatting.GRAY));
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		} else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 47 && mouseY <= 47 + 5) {
			List<Component> list = new ArrayList<>();
			list.add(new TranslatableComponent("gui.apotheosis.enchant.arcana").withStyle(ChatFormatting.DARK_PURPLE));
			list.add(new TranslatableComponent("info.apotheosis.req_p", stats.arcana).withStyle(ChatFormatting.GRAY));
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		}
	}

	public static void drawWordWrap(Font font, FormattedText pText, int pX, int pY, int pMaxWidth, int pColor, PoseStack stack) {
		for (FormattedCharSequence formattedcharsequence : font.split(pText, pMaxWidth)) {
			font.draw(stack, formattedcharsequence, (float) pX, (float) pY, pColor);
			pY += 9;
		}

	}

}