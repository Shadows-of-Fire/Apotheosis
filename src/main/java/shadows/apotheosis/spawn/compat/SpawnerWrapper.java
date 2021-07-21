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
		new ApothSpawnerTile().save(SPAWNER.get(0).getOrCreateTagElement("BlockEntityTag"));
	}

	final SpawnerModifier modifier;
	final ItemStack output;
	final String tooltip;

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, String tooltip) {
		this.modifier = modifier;
		this.output = SPAWNER.get(0).copy();
		CompoundNBT tag = this.output.getOrCreateTagElement("BlockEntityTag");
		tag.putInt(nbt, tag.getInt(nbt) + modifier.getValue());
		this.tooltip = tooltip;
	}

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, boolean change, String tooltip) {
		this.modifier = modifier;
		this.output = SPAWNER.get(0).copy();
		CompoundNBT tag = this.output.getOrCreateTagElement("BlockEntityTag");
		tag.putBoolean(nbt, change);
		this.tooltip = tooltip;
	}

	public SpawnerWrapper(ResourceLocation entityOut, String tooltip) {
		this.modifier = new EggModifier();
		this.output = SPAWNER.get(0).copy();
		CompoundNBT tag = this.output.getOrCreateTagElement("BlockEntityTag");
		tag.getCompound("SpawnData").putString("id", entityOut.toString());
		this.tooltip = tooltip;
	}

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(SPAWNER, PlaceboUtil.asList(this.modifier.getIngredient().getItems())));
		ingredients.setOutput(VanillaTypes.ITEM, this.output);
	}

	public void drawInfo(Minecraft mc, MatrixStack stack, int width, int height, double mouseX, double mouseY) {
		mc.font.draw(stack, I18n.get(this.tooltip), 0, height - mc.font.lineHeight * 2, 0);
		if (this.modifier.getMin() != -1) mc.font.draw(stack, I18n.get("jei.spw.minmax", this.modifier.getMin(), this.modifier.getMax()), 0, height - mc.font.lineHeight + 3, 0);
	}

	public static class SpawnerInverseWrapper extends SpawnerWrapper {

		public SpawnerInverseWrapper() {
			super(null, "", false, "jei.spw.invert");
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(Collections.emptyList(), PlaceboUtil.asList(SpawnerModifiers.INVERSE.getIngredient().getItems())));
		}

		@Override
		public void drawInfo(Minecraft mc, MatrixStack stack, int width, int height, double mouseX, double mouseY) {
			mc.font.draw(stack, I18n.get(this.tooltip), 0, height - mc.font.lineHeight * 2, 0);
			mc.font.draw(stack, I18n.get("jei.spw.invert2"), 0, height - mc.font.lineHeight + 3, 0);
		}

	}

}