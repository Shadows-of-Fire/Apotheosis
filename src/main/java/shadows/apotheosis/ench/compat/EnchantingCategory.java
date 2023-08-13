package shadows.apotheosis.ench.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
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
    public static final RecipeType<EnchantingRecipe> TYPE = RecipeType.create(Apotheosis.MODID, "enchanting", EnchantingRecipe.class);
    public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/enchanting_jei.png");
    private static final Map<Class<?>, Extension<?>> EXTENSIONS = new HashMap<>();

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public EnchantingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURES, 0, 0, 170, 56);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.ENCHANTING_TABLE));
        this.localizedName = Component.translatable("apotheosis.recipes.enchanting");
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
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public RecipeType<EnchantingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EnchantingRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 6, 6);
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 37, 6);
        Extension<?> ext = EXTENSIONS.get(recipe.getClass());
        if (ext != null) ext.setRecipe(builder, input, output, recipe, focuses);
        else {
            input.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getInput().getItems()));
            output.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem());
        }
    }

    @Override
    public void draw(EnchantingRecipe recipe, IRecipeSlotsView slots, PoseStack stack, double mouseX, double mouseY) {
        boolean hover = false;
        if (mouseX > 57 && mouseX <= 57 + 108 && mouseY > 4 && mouseY <= 4 + 19) {
            GuiComponent.blit(stack, 57, 4, 0, 0, 71, 108, 19, 256, 256);
            hover = true;
        }

        Font font = Minecraft.getInstance().font;
        Stats stats = recipe.getRequirements();
        Stats maxStats = recipe.getMaxRequirements();
        font.draw(stack, I18n.get("gui.apotheosis.enchant.eterna"), 16, 26, 0x3DB53D);
        font.draw(stack, I18n.get("gui.apotheosis.enchant.quanta"), 16, 36, 0xFC5454);
        font.draw(stack, I18n.get("gui.apotheosis.enchant.arcana"), 16, 46, 0xA800A8);
        int level = (int) (stats.eterna() * 2);

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
        int[] pos = { (int) (stats.eterna() / EnchantingStatManager.getAbsoluteMaxEterna() * 110), (int) (stats.quanta() / 100 * 110), (int) (stats.arcana() / 100 * 110) };
        if (stats.eterna() > 0) {
            GuiComponent.blit(stack, 56, 27, 0, 56, pos[0], 5, 256, 256);
        }
        if (stats.quanta() > 0) {
            GuiComponent.blit(stack, 56, 37, 0, 61, pos[1], 5, 256, 256);
        }
        if (stats.arcana() > 0) {
            GuiComponent.blit(stack, 56, 47, 0, 66, pos[2], 5, 256, 256);
        }
        RenderSystem.enableBlend();
        if (maxStats.eterna() > 0) {
            GuiComponent.blit(stack, 56 + pos[0], 27, pos[0], 90, (int) ((maxStats.eterna() - stats.eterna()) / EnchantingStatManager.getAbsoluteMaxEterna() * 110), 5, 256, 256);
        }
        if (maxStats.quanta() > 0) {
            GuiComponent.blit(stack, 56 + pos[1], 37, pos[1], 95, (int) ((maxStats.quanta() - stats.quanta()) / 100 * 110), 5, 256, 256);
        }
        if (maxStats.arcana() > 0) {
            GuiComponent.blit(stack, 56 + pos[2], 47, pos[2], 100, (int) ((maxStats.arcana() - stats.arcana()) / 100 * 110), 5, 256, 256);
        }
        RenderSystem.disableBlend();
        Screen scn = Minecraft.getInstance().screen;
        if (scn == null) return; // We need this to render tooltips, bail if its not there.
        if (hover) {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("container.enchant.clue", Apoth.Enchantments.INFUSION.get().getFullname(1).getString()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
        }
        else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 26 && mouseY <= 27 + 5) {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("gui.apotheosis.enchant.eterna").withStyle(ChatFormatting.GREEN));
            if (maxStats.eterna() == stats.eterna()) {
                list.add(Component.translatable("info.apotheosis.eterna_exact", stats.eterna(), EnchantingStatManager.getAbsoluteMaxEterna()).withStyle(ChatFormatting.GRAY));
            }
            else {
                list.add(Component.translatable("info.apotheosis.eterna_at_least", stats.eterna(), EnchantingStatManager.getAbsoluteMaxEterna()).withStyle(ChatFormatting.GRAY));
                if (maxStats.eterna() > -1) list.add(Component.translatable("info.apotheosis.eterna_at_most", maxStats.eterna(), EnchantingStatManager.getAbsoluteMaxEterna()).withStyle(ChatFormatting.GRAY));
            }
            scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
        }
        else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 36 && mouseY <= 37 + 5) {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("gui.apotheosis.enchant.quanta").withStyle(ChatFormatting.RED));
            if (maxStats.quanta() == stats.quanta()) {
                list.add(Component.translatable("info.apotheosis.percent_exact", stats.quanta()).withStyle(ChatFormatting.GRAY));
            }
            else {
                list.add(Component.translatable("info.apotheosis.percent_at_least", stats.quanta()).withStyle(ChatFormatting.GRAY));
                if (maxStats.quanta() > -1) list.add(Component.translatable("info.apotheosis.percent_at_most", maxStats.quanta()).withStyle(ChatFormatting.GRAY));
            }
            scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
        }
        else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 46 && mouseY <= 47 + 5) {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("gui.apotheosis.enchant.arcana").withStyle(ChatFormatting.DARK_PURPLE));
            if (maxStats.arcana() == stats.arcana()) {
                list.add(Component.translatable("info.apotheosis.percent_exact", stats.arcana()).withStyle(ChatFormatting.GRAY));
            }
            else {
                list.add(Component.translatable("info.apotheosis.percent_at_least", stats.arcana()).withStyle(ChatFormatting.GRAY));
                if (maxStats.arcana() > -1) list.add(Component.translatable("info.apotheosis.percent_at_most", maxStats.arcana()).withStyle(ChatFormatting.GRAY));
            }
            scn.renderComponentTooltip(stack, list, (int) mouseX, (int) mouseY);
        }
    }

    public static void drawWordWrap(Font font, FormattedText pText, int pX, int pY, int pMaxWidth, int pColor, PoseStack stack) {
        for (FormattedCharSequence formattedcharsequence : font.split(pText, pMaxWidth)) {
            font.draw(stack, formattedcharsequence, pX, pY, pColor);
            pY += 9;
        }

    }

    public static <T extends EnchantingRecipe> void registerExtension(Class<T> cls, Extension<T> ext) {
        EXTENSIONS.put(cls, ext);
    }

    public static interface Extension<T extends EnchantingRecipe> {
        public void setRecipe(IRecipeLayoutBuilder builder, IRecipeSlotBuilder input, IRecipeSlotBuilder output, EnchantingRecipe recipe, IFocusGroup focuses);
    }

}
