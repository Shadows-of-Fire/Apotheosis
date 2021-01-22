package shadows.apotheosis.spawn.compat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.modifiers.EggModifier;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.util.PlaceboUtil;

public class SpawnerWrapper {

	public static final List<ItemStack> SPAWNER = Collections.singletonList(new ItemStack(Blocks.SPAWNER));
	static {
		new ApothSpawnerTile().write(SPAWNER.get(0).getOrCreateChildTag("BlockEntityTag"));
	}

	final SpawnerModifier modifier;
	final ItemStack output;
	final String tooltip;

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, String tooltip) {
		this.modifier = modifier;
		output = SPAWNER.get(0).copy();
		CompoundNBT tag = output.getOrCreateChildTag("BlockEntityTag");
		tag.putInt(nbt, tag.getInt(nbt) + modifier.getValue());
		this.tooltip = tooltip;
	}

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, boolean change, String tooltip) {
		this.modifier = modifier;
		output = SPAWNER.get(0).copy();
		CompoundNBT tag = output.getOrCreateChildTag("BlockEntityTag");
		tag.putBoolean(nbt, change);
		this.tooltip = tooltip;
	}

	public SpawnerWrapper(ResourceLocation entityOut, String tooltip) {
		modifier = new EggModifier();
		output = SPAWNER.get(0).copy();
		CompoundNBT tag = output.getOrCreateChildTag("BlockEntityTag");
		tag.getCompound("SpawnData").putString("id", entityOut.toString());
		this.tooltip = tooltip;
	}

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(SPAWNER, PlaceboUtil.asList(modifier.getIngredient().getMatchingStacks())));
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}

	public void drawInfo(Minecraft mc, MatrixStack stack, int width, int height, double mouseX, double mouseY) {
		mc.fontRenderer.drawString(stack, I18n.format(tooltip), 0, height - mc.fontRenderer.FONT_HEIGHT * 2, 0);
		if (modifier.getMin() != -1) mc.fontRenderer.drawString(stack, I18n.format("jei.spw.minmax", modifier.getMin(), modifier.getMax()), 0, height - mc.fontRenderer.FONT_HEIGHT + 3, 0);
	}

	public static class SpawnerInverseWrapper extends SpawnerWrapper {

		public SpawnerInverseWrapper() {
			super(null, "", false, "jei.spw.invert");
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(Collections.emptyList(), PlaceboUtil.asList(SpawnerModifiers.INVERSE.getIngredient().getMatchingStacks())));
		}

		@Override
		public void drawInfo(Minecraft mc, MatrixStack stack, int width, int height, double mouseX, double mouseY) {
			mc.fontRenderer.drawString(stack, I18n.format(tooltip), 0, height - mc.fontRenderer.FONT_HEIGHT * 2, 0);
			mc.fontRenderer.drawString(stack, I18n.format("jei.spw.invert2"), 0, height - mc.fontRenderer.FONT_HEIGHT + 3, 0);
		}

	}

}