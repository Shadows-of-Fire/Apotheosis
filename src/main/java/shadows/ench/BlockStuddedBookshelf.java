package shadows.ench;

import net.minecraft.block.BlockBookshelf;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.Apotheosis;

public class BlockStuddedBookshelf extends BlockBookshelf {

	public BlockStuddedBookshelf(ResourceLocation name) {
		setHardness(2.5F);
		setSoundType(SoundType.WOOD);
		setTranslationKey(Apotheosis.MODID + ".bookshelf");
		setRegistryName(name);
	}

	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return 2;
	}

}
