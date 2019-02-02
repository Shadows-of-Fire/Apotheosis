package shadows.ench;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.Apotheosis;

public class BlockHellBookshelf extends Block {

	public BlockHellBookshelf(ResourceLocation name) {
		super(Material.ROCK, MapColor.BLACK);
		setHardness(2.0F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setTranslationKey(Apotheosis.MODID + ".hellshelf");
		setRegistryName(name);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> info, ITooltipFlag flag) {
		info.add(I18n.format("info.apotheosis.hellshelf"));
	}

}
