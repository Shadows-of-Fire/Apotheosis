package shadows.ench;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.RecipeHelper;

public class EnchModule {

	@ObjectHolder("apotheosis:hellshelf")
	public static final BlockHellBookshelf HELLSHELF = null;

	@ObjectHolder("minecraft:web")
	public static final Item COBWEB = null;

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		Configuration config = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));
		setEnch(ToolMaterial.GOLD, 40);
		setEnch(ArmorMaterial.GOLD, 40);
		for (ArmorMaterial a : ArmorMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Armor", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this armor material."));
		for (ToolMaterial a : ToolMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Tools", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this tool material."));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Block b;
		e.getRegistry().register(b = new BlockHellBookshelf(new ResourceLocation(Apotheosis.MODID, "hellshelf")));
		ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));
	}

	@SubscribeEvent
	public void models(ModelRegistryEvent e) {
		PlaceboUtil.sMRL(HELLSHELF, 0, "normal");
	}

	@SubscribeEvent
	public void recipes(Register<IRecipe> e) {
		RecipeHelper helper = new RecipeHelper(Apotheosis.MODID, Apotheosis.MODNAME, new ArrayList<>());
		helper.addShaped(HELLSHELF, 3, 3, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Items.BLAZE_ROD, Blocks.BOOKSHELF, Items.BLAZE_POWDER, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK);
		helper.register(e.getRegistry());
	}

	@SubscribeEvent
	public void removeEnch(AnvilUpdateEvent e) {
		if (!EnchantmentHelper.getEnchantments(e.getLeft()).isEmpty() && e.getRight().getItem() == COBWEB) {
			ItemStack stack = e.getLeft().copy();
			EnchantmentHelper.setEnchantments(Collections.emptyMap(), stack);
			e.setCost(1);
			e.setMaterialCost(1);
			e.setOutput(stack);
		}
	}

	public static void setEnch(ToolMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ToolMaterial.class, mat, ench, "enchantability", "field_78008_j");
	}

	public static void setEnch(ArmorMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ArmorMaterial.class, mat, ench, "enchantability", "field_78055_h");
	}

}
