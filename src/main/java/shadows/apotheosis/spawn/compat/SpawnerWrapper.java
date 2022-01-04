package shadows.apotheosis.spawn.compat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.Lazy;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.modifiers.EggModifier;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.util.PlaceboUtil;

public class SpawnerWrapper {

	public static final Lazy<List<ItemStack>> SPAWNER = Lazy.of(() -> {
		ItemStack stack = new ItemStack(Blocks.SPAWNER);
		new ApothSpawnerTile(BlockPos.ZERO, Blocks.SPAWNER.defaultBlockState()).save(stack.getOrCreateTagElement("BlockEntityTag"));
		return Collections.singletonList(stack);
	});

	final SpawnerModifier modifier;
	final ItemStack output;
	final String tooltip;

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, String tooltip) {
		this.modifier = modifier;
		this.output = SPAWNER.get().get(0).copy();
		CompoundTag tag = this.output.getOrCreateTagElement("BlockEntityTag");
		tag.putInt(nbt, tag.getInt(nbt) + modifier.getValue());
		this.tooltip = tooltip;
	}

	public SpawnerWrapper(SpawnerModifier modifier, String nbt, boolean change, String tooltip) {
		this.modifier = modifier;
		this.output = SPAWNER.get().get(0).copy();
		CompoundTag tag = this.output.getOrCreateTagElement("BlockEntityTag");
		tag.putBoolean(nbt, change);
		this.tooltip = tooltip;
	}

	public SpawnerWrapper(ResourceLocation entityOut, String tooltip) {
		this.modifier = new EggModifier();
		this.output = SPAWNER.get().get(0).copy();
		CompoundTag tag = this.output.getOrCreateTagElement("BlockEntityTag");
		tag.getCompound("SpawnData").putString("id", entityOut.toString());
		this.tooltip = tooltip;
	}

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(SPAWNER.get(), PlaceboUtil.asList(this.modifier.getIngredient().getItems())));
		ingredients.setOutput(VanillaTypes.ITEM, this.output);
	}

	public void drawInfo(Minecraft mc, PoseStack stack, int width, int height, double mouseX, double mouseY) {
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
		public void drawInfo(Minecraft mc, PoseStack stack, int width, int height, double mouseX, double mouseY) {
			mc.font.draw(stack, I18n.get(this.tooltip), 0, height - mc.font.lineHeight * 2, 0);
			mc.font.draw(stack, I18n.get("jei.spw.invert2"), 0, height - mc.font.lineHeight + 3, 0);
		}

	}

}