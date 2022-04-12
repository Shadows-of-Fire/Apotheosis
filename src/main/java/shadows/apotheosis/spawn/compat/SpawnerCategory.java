package shadows.apotheosis.spawn.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.spawn.modifiers.StatModifier;

public class SpawnerCategory implements IRecipeCategory<SpawnerModifier> {

	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/spawner_jei.png");
	public static final ResourceLocation UID = new ResourceLocation(Apotheosis.MODID, "spawner_modifiers");

	private final IDrawable bg;
	private final IDrawable icon;
	private final ITextComponent title;
	private final ItemStackRenderer renderer;
	private final ItemStack spawner = new ItemStack(Items.SPAWNER);

	public SpawnerCategory(IGuiHelper helper) {
		this.bg = helper.drawableBuilder(TEXTURES, 0, 0, 168, 75).build();
		this.icon = helper.createDrawableIngredient(new ItemStack(Items.SPAWNER));
		this.title = new TranslationTextComponent("title.apotheosis.spawner");
		this.renderer = new ItemStackRenderer();
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public String getTitle() {
		return this.title.getString();
	}

	@Override
	public IDrawable getBackground() {
		return this.bg;
	}

	@Override
	public void setRecipe(IRecipeLayout layout, SpawnerModifier recipe, IIngredients ing) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		stacks.init(0, true, 10, 10);
		if (recipe.getOffhandInput() != Ingredient.EMPTY) stacks.init(1, true, 10, 47);
		stacks.set(ing);
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public Class<? extends SpawnerModifier> getRecipeClass() {
		return SpawnerModifier.class;
	}

	@Override
	public void setIngredients(SpawnerModifier recipe, IIngredients ing) {
		ing.setInputIngredients(Arrays.asList(recipe.getMainhandInput(), recipe.getOffhandInput(), Ingredient.of(Blocks.SPAWNER)));
		ing.setOutput(VanillaTypes.ITEM, new ItemStack(Blocks.SPAWNER));
	}

	@Override
	public void draw(SpawnerModifier recipe, MatrixStack stack, double mouseX, double mouseY) {
		if (recipe.getOffhandInput() == Ingredient.EMPTY) {
			Screen.blit(stack, 1, 31, 0, 0, 88, 28, 34, 256, 256);
		}

		Screen scn = Minecraft.getInstance().screen;
		if (scn == null) return; // We need this to render tooltips, bail if its not there.
		if (mouseX >= -1 && mouseX < 9 && mouseY >= 13 && mouseY < 13 + 12) {
			Screen.blit(stack, -1, 13, 0, 0, 75, 10, 12, 256, 256);
			scn.renderComponentTooltip(stack, Arrays.asList(new TranslationTextComponent("misc.apotheosis.mainhand")), (int) mouseX, (int) mouseY);
		} else if (mouseX >= -1 && mouseX < 9 && mouseY >= 50 && mouseY < 50 + 12 && recipe.getOffhandInput() != Ingredient.EMPTY) {
			Screen.blit(stack, -1, 50, 0, 0, 75, 10, 12, 256, 256);
			scn.renderComponentTooltip(stack, Arrays.asList(new TranslationTextComponent("misc.apotheosis.offhand"), new TranslationTextComponent("misc.apotheosis.not_consumed").withStyle(TextFormatting.GRAY)), (int) mouseX, (int) mouseY);
		} else if (mouseX >= 33 && mouseX < 33 + 16 && mouseY >= 30 && mouseY < 30 + 16) {
			scn.renderComponentTooltip(stack, Arrays.asList(new TranslationTextComponent("misc.apotheosis.rclick_spawner")), (int) mouseX, (int) mouseY);
		}

		renderer.render(stack, 31, 29, spawner);

		FontRenderer font = Minecraft.getInstance().font;
		int top = 75 / 2 - (recipe.getStatModifiers().size() * (font.lineHeight + 2)) / 2 + 2;
		int left = 168;
		for (StatModifier<?> s : recipe.getStatModifiers()) {
			String value = s.value.toString();
			if (value.equals("true")) value = "+";
			else if (value.equals("false")) value = "-";
			else if (s.value instanceof Number && ((Number) s.value).intValue() > 0) value = "+" + value;
			ITextComponent msg = new TranslationTextComponent("misc.apotheosis.concat", value, s.stat.name());
			int width = font.width(msg);
			boolean hover = mouseX >= left - width && mouseX < left && mouseY >= top && mouseY < top + font.lineHeight + 1;
			font.draw(stack, msg, left - font.width(msg), top, hover ? 0x8080FF : 0x333333);

			int maxWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
			maxWidth = maxWidth - (maxWidth - 210) / 2 - 210;

			if (hover) {
				List<ITextComponent> list = new ArrayList<>();
				list.add(s.stat.name().withStyle(TextFormatting.GREEN, TextFormatting.UNDERLINE));
				list.add(s.stat.desc().withStyle(TextFormatting.GRAY));
				if (s.value instanceof Number) {
					if (((Number) s.min).intValue() > 0 || ((Number) s.max).intValue() != Integer.MAX_VALUE) list.add(new StringTextComponent(" "));
					if (((Number) s.min).intValue() > 0) list.add(new TranslationTextComponent("misc.apotheosis.min_value", s.min).withStyle(TextFormatting.GRAY));
					if (((Number) s.max).intValue() != Integer.MAX_VALUE) list.add(new TranslationTextComponent("misc.apotheosis.max_value", s.max).withStyle(TextFormatting.GRAY));
				}
				renderComponentTooltip(scn, stack, list, left + 6, (int) mouseY, maxWidth, font);
			}

			top += font.lineHeight + 2;
		}
	}

	private static void renderComponentTooltip(Screen scn, MatrixStack stack, List<ITextComponent> list, int x, int y, int maxWidth, FontRenderer font) {
		List<ITextProperties> text = list.stream().map(c -> font.getSplitter().splitLines(c, maxWidth, c.getStyle())).flatMap(List::stream).collect(Collectors.toList());
		scn.renderWrappedToolTip(stack, text, x, y, font);
	}

}