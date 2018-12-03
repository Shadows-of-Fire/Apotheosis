package shadows.spawn.compat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import shadows.placebo.util.PlaceboUtil;
import shadows.spawn.SpawnerModifier;
import shadows.spawn.SpawnerModifiers;
import shadows.spawn.TileSpawnerExt;

public class SpawnerWrapper implements IRecipeWrapper {

	public static final List<ItemStack> SPAWNER = Collections.singletonList(new ItemStack(Blocks.MOB_SPAWNER));
	static {
		new TileSpawnerExt().writeToNBT(SPAWNER.get(0).getOrCreateSubCompound("spawner"));
	}

	SpawnerModifier modifier;
	ItemStack output;
	String[] tooltips;

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, int change, String... tooltips) {
		this.modifier = modifier;
		this.output = SPAWNER.get(0).copy();
		NBTTagCompound tag = output.getOrCreateSubCompound("spawner");
		tag.setInteger(nbt, tag.getInteger(nbt) + change);
		this.tooltips = tooltips;
	}

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, boolean change, String... tooltips) {
		this.modifier = modifier;
		this.output = SPAWNER.get(0).copy();
		NBTTagCompound tag = output.getOrCreateSubCompound("spawner");
		tag.setBoolean(nbt, change);
		this.tooltips = tooltips;
	}

	public SpawnerWrapper(ItemStack catalyst, ResourceLocation entityOut, String... tooltips) {
		this.modifier = new SpawnerModifier(catalyst, (a, b) -> {
		});
		this.output = SPAWNER.get(0).copy();
		NBTTagCompound tag = output.getOrCreateSubCompound("spawner");
		tag.getCompoundTag("SpawnData").setString("id", entityOut.toString());
		this.tooltips = tooltips;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(SPAWNER, PlaceboUtil.asList(modifier.getIngredient().getMatchingStacks())));
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}

	@Override
	public void drawInfo(Minecraft mc, int width, int height, int mouseX, int mouseY) {
		for (int i = 0; i < tooltips.length; i++)
			mc.fontRenderer.drawString(I18n.format(tooltips[i]), 0, height - mc.fontRenderer.FONT_HEIGHT * (2 - i), 0);
	}

	public static class SpawnerInverseWrapper extends SpawnerWrapper {

		public SpawnerInverseWrapper() {
			super(null, "", false, "spw.invert", "spw.invert2");
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(Collections.singletonList(ItemStack.EMPTY), PlaceboUtil.asList(SpawnerModifiers.inverseItem.getMatchingStacks())));
			ingredients.setOutput(VanillaTypes.ITEM, ItemStack.EMPTY);
		}

	}

}
