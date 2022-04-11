package shadows.apotheosis.ench.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.table.EnchantingRecipe;
import shadows.apotheosis.ench.table.EnchantingStatManager;
import shadows.apotheosis.ench.table.EnchantingStatManager.Stats;

public class EnchantingCategory implements IRecipeCategory<EnchantingRecipe> {

	public static final ResourceLocation UID = new ResourceLocation(Apotheosis.MODID, "enchanting");
	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/enchanting_jei.png");
	private static final Map<Class<?>, Extension<?>> EXTENSIONS = new HashMap<>();

	private final IDrawable background;
	private final IDrawable icon;
	private final TranslationTextComponent localizedName;

	public EnchantingCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(TEXTURES, 0, 0, 170, 56);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.ENCHANTING_TABLE));
		this.localizedName = new TranslationTextComponent("apotheosis.recipes.enchanting");
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
	public String getTitle() {
		return this.localizedName.getString();
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setIngredients(EnchantingRecipe recipe, IIngredients ing) {
		Extension ext = EXTENSIONS.get(recipe.getClass());
		if (ext != null) ext.setIngredients(recipe, ing);
		else {
			ing.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
			ing.setInputIngredients(Arrays.asList(recipe.getInput()));
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setRecipe(IRecipeLayout layout, EnchantingRecipe recipe, IIngredients ing) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		stacks.init(0, true, 5, 5);
		stacks.init(1, false, 36, 5);

		Extension ext = EXTENSIONS.get(recipe.getClass());
		if (ext != null) ext.setRecipe(recipe, layout, ing);
		else {
			stacks.set(ing);
		}
	}

	@Override
	public void draw(EnchantingRecipe recipe, MatrixStack stack, double mouseX, double mouseY) {
		boolean hover = false;
		if (mouseX > 57 && mouseX <= 57 + 108 && mouseY > 4 && mouseY <= 4 + 19) {
			Screen.blit(stack, 57, 4, 0, 0, 71, 108, 19, 256, 256);
			hover = true;
		}

		FontRenderer font = Minecraft.getInstance().font;
		Stats stats = recipe.getRequirements();
		Stats maxStats = recipe.getMaxRequirements();
		font.draw(stack, I18n.get("gui.apotheosis.enchant.eterna"), 16, 26, 0x3DB53D);
		font.draw(stack, I18n.get("gui.apotheosis.enchant.quanta"), 16, 36, 0xFC5454);
		font.draw(stack, I18n.get("gui.apotheosis.enchant.arcana"), 16, 46, 0xA800A8);
		int level = (int) (stats.eterna * 2);

		String s = "" + level;
		int width = 86 - font.width(s);
		EnchantmentNameParts.getInstance().initSeed(recipe.getId().hashCode());
		ITextProperties itextproperties = EnchantmentNameParts.getInstance().getRandomName(font, width);
		int color = hover ? 16777088 : 6839882;
		drawWordWrap(font, itextproperties, 77, 6, width, color, stack);
		color = 8453920;
		font.drawShadow(stack, s, 77 + width, 13, color);

		Minecraft.getInstance().textureManager.bind(TEXTURES);
		int[] pos = { (int) (stats.eterna / EnchantingStatManager.getAbsoluteMaxEterna() * 110), (int) (stats.quanta / 100 * 110), (int) (stats.arcana / 100 * 110) };
		if (stats.eterna > 0) {
			Screen.blit(stack, 56, 27, 0, 56, pos[0], 5, 256, 256);
		}
		if (stats.quanta > 0) {
			Screen.blit(stack, 56, 37, 0, 61, pos[1], 5, 256, 256);
		}
		if (stats.arcana > 0) {
			Screen.blit(stack, 56, 47, 0, 66, pos[2], 5, 256, 256);
		}
		RenderSystem.enableBlend();
		if (maxStats.eterna > 0) {
			Screen.blit(stack, 56 + pos[0], 27, pos[0], 90, (int) ((maxStats.eterna - stats.eterna) / EnchantingStatManager.getAbsoluteMaxEterna() * 110), 5, 256, 256);
		}
		if (maxStats.quanta > 0) {
			Screen.blit(stack, 56 + pos[1], 37, pos[1], 95, (int) ((maxStats.quanta - stats.quanta) / 100 * 110), 5, 256, 256);
		}
		if (maxStats.arcana > 0) {
			Screen.blit(stack, 56 + pos[2], 47, pos[2], 100, (int) ((maxStats.arcana - stats.arcana) / 100 * 110), 5, 256, 256);
		}
		RenderSystem.disableBlend();
		Screen scn = Minecraft.getInstance().screen;
		if (scn == null) return; // We need this to render tooltips, bail if its not there.
		if (hover) {
			List<ITextComponent> list = new ArrayList<>();
			list.add(new TranslationTextComponent("container.enchant.clue", ApotheosisObjects.INFUSION.getFullname(1).getString()).withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		} else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 26 && mouseY <= 27 + 5) {
			List<ITextComponent> list = new ArrayList<>();
			list.add(new TranslationTextComponent("gui.apotheosis.enchant.eterna").withStyle(TextFormatting.GREEN));
			if (maxStats.eterna == stats.eterna) {
				list.add(new TranslationTextComponent("info.apotheosis.eterna_exact", stats.eterna, EnchantingStatManager.getAbsoluteMaxEterna()).withStyle(TextFormatting.GRAY));
			} else {
				list.add(new TranslationTextComponent("info.apotheosis.eterna_at_least", stats.eterna, EnchantingStatManager.getAbsoluteMaxEterna()).withStyle(TextFormatting.GRAY));
				if (maxStats.eterna > -1) list.add(new TranslationTextComponent("info.apotheosis.eterna_at_most", maxStats.eterna, EnchantingStatManager.getAbsoluteMaxEterna()).withStyle(TextFormatting.GRAY));
			}
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		} else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 36 && mouseY <= 37 + 5) {
			List<ITextComponent> list = new ArrayList<>();
			list.add(new TranslationTextComponent("gui.apotheosis.enchant.quanta").withStyle(TextFormatting.RED));
			if (maxStats.quanta == stats.quanta) {
				list.add(new TranslationTextComponent("info.apotheosis.percent_exact", stats.quanta).withStyle(TextFormatting.GRAY));
			} else {
				list.add(new TranslationTextComponent("info.apotheosis.percent_at_least", stats.quanta).withStyle(TextFormatting.GRAY));
				if (maxStats.quanta > -1) list.add(new TranslationTextComponent("info.apotheosis.percent_at_most", maxStats.quanta).withStyle(TextFormatting.GRAY));
			}
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		} else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 46 && mouseY <= 47 + 5) {
			List<ITextComponent> list = new ArrayList<>();
			list.add(new TranslationTextComponent("gui.apotheosis.enchant.arcana").withStyle(TextFormatting.DARK_PURPLE));
			if (maxStats.arcana == stats.arcana) {
				list.add(new TranslationTextComponent("info.apotheosis.percent_exact", stats.arcana).withStyle(TextFormatting.GRAY));
			} else {
				list.add(new TranslationTextComponent("info.apotheosis.percent_at_least", stats.arcana).withStyle(TextFormatting.GRAY));
				if (maxStats.arcana > -1) list.add(new TranslationTextComponent("info.apotheosis.percent_at_most", maxStats.arcana).withStyle(TextFormatting.GRAY));
			}
			scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
		}
	}

	public static void drawWordWrap(FontRenderer font, ITextProperties pText, int pX, int pY, int pMaxWidth, int pColor, MatrixStack stack) {
		for (IReorderingProcessor formattedcharsequence : font.split(pText, pMaxWidth)) {
			font.draw(stack, formattedcharsequence, pX, pY, pColor);
			pY += 9;
		}

	}

	public static <T extends EnchantingRecipe> void registerExtension(Class<T> cls, Extension<T> ext) {
		EXTENSIONS.put(cls, ext);
	}

	public static interface Extension<T extends EnchantingRecipe> {

		public void setIngredients(T recipe, IIngredients ingredients);

		public void setRecipe(T recipe, IRecipeLayout recipeLayout, IIngredients ingredients);
	}

}