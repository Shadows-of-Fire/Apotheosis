package shadows.apotheosis.spawn.compat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.TileSpawnerExt;
import shadows.apotheosis.spawn.modifiers.EggModifier;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.placebo.util.PlaceboUtil;

public class SpawnerWrapper {

	public static final List<ItemStack> SPAWNER = Collections.singletonList(new ItemStack(Blocks.SPAWNER));
	static {
		new TileSpawnerExt().write(SPAWNER.get(0).getOrCreateChildTag("BlockEntityTag"));
	}

	SpawnerModifier modifier;
	ItemStack output;
	String[] tooltips;

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, String... tooltips) {
		this.modifier = modifier;
		output = SPAWNER.get(0).copy();
		CompoundNBT tag = output.getOrCreateChildTag("BlockEntityTag");
		tag.putInt(nbt, tag.getInt(nbt) + modifier.getValue());
		this.tooltips = tooltips;
	}

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, boolean change, String... tooltips) {
		this.modifier = modifier;
		output = SPAWNER.get(0).copy();
		CompoundNBT tag = output.getOrCreateChildTag("BlockEntityTag");
		tag.putBoolean(nbt, change);
		this.tooltips = tooltips;
	}

	public SpawnerWrapper(ItemStack catalyst, ResourceLocation entityOut, String... tooltips) {
		modifier = new EggModifier(catalyst);
		output = SPAWNER.get(0).copy();
		CompoundNBT tag = output.getOrCreateChildTag("BlockEntityTag");
		tag.getCompound("SpawnData").putString("id", entityOut.toString());
		this.tooltips = tooltips;
	}

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(SPAWNER, PlaceboUtil.asList(modifier.getIngredient().getMatchingStacks())));
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}

	public void drawInfo(Minecraft mc, int width, int height, double mouseX, double mouseY) {
		for (int i = 0; i < tooltips.length; i++)
			mc.fontRenderer.drawString(I18n.format(tooltips[i]), 0, height - mc.fontRenderer.FONT_HEIGHT * (2 - i), 0);
	}

	public static class SpawnerInverseWrapper extends SpawnerWrapper {

		public SpawnerInverseWrapper() {
			super(null, "", false, "jei.spw.invert", "jei.spw.invert2");
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(Collections.emptyList(), PlaceboUtil.asList(SpawnerModifiers.inverseItem.getMatchingStacks())));
		}

	}

}
