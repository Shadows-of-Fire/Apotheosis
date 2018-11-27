package shadows.spawn.compat;

import java.util.Arrays;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import shadows.spawn.SpawnerModifiers;
import shadows.spawn.TileSpawnerExt;

public class SpawnerWrapper implements IRecipeWrapper {

	public static final ItemStack SPAWNER = new ItemStack(Blocks.MOB_SPAWNER);
	static {
		new TileSpawnerExt().writeToNBT(SPAWNER.getOrCreateSubCompound("spawner"));
	}

	ItemStack catalyst;
	ItemStack output;
	String[] tooltips;

	public SpawnerWrapper(ItemStack catalyst, String nbt, int change, String... tooltips) {
		this.catalyst = catalyst;
		this.output = SPAWNER.copy();
		NBTTagCompound tag = output.getOrCreateSubCompound("spawner");
		tag.setInteger(nbt, tag.getInteger(nbt) + change);
		this.tooltips = tooltips;
	}

	public SpawnerWrapper(ItemStack catalyst, String nbt, boolean change, String... tooltips) {
		this.catalyst = catalyst;
		this.output = SPAWNER.copy();
		NBTTagCompound tag = output.getOrCreateSubCompound("spawner");
		tag.setBoolean(nbt, change);
		this.tooltips = tooltips;
	}

	public SpawnerWrapper(ItemStack catalyst, ResourceLocation entityOut, String... tooltips) {
		this.catalyst = catalyst;
		this.output = SPAWNER.copy();
		NBTTagCompound tag = output.getOrCreateSubCompound("spawner");
		tag.getCompoundTag("SpawnData").setString("id", entityOut.toString());
		this.tooltips = tooltips;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(SPAWNER, catalyst));
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}

	@Override
	public void drawInfo(Minecraft mc, int width, int height, int mouseX, int mouseY) {
		for (int i = 0; i < tooltips.length; i++) {
			String translated = "spw.invert".equals(tooltips[i]) ? I18n.format("spw.invert", SpawnerModifiers.inverseItem.getDisplayName()) : I18n.format(tooltips[i]);
			mc.fontRenderer.drawString(translated, 0, height - mc.fontRenderer.FONT_HEIGHT * (2 - i), 0);
		}

	}

}
