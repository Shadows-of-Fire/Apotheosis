package shadows.apotheosis.deadly.gen;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.server.ServerLifecycleHooks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;

public class TomeTowerFeature extends Feature<NoneFeatureConfiguration> {

	public static final ResourceLocation TEMPLATE_ID = new ResourceLocation(Apotheosis.MODID, "tome_tower");
	public static final TomeTowerFeature INSTANCE = new TomeTowerFeature();

	public TomeTowerFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator gen, Random rand, BlockPos pos, NoneFeatureConfiguration cfg) {
		if (!DeadlyConfig.canGenerateIn(world)) return false;

		pos = pos.offset(rand.nextInt(5), -1, rand.nextInt(5));

		if (world.getBlockState(pos).getBlock() == Blocks.SNOW) pos = pos.below();

		for (int x = 0; x < 9; x++) {
			for (int z = 0; z < 9; z++) {
				for (int y = 0; y < 4; y++) {
					BlockPos blockpos = pos.offset(x, y, z);
					BlockState state = world.getBlockState(blockpos);
					Material material = state.getMaterial();
					boolean flag = material.isSolid();
					if (y == 0 && !flag) return false; //Exit if the floor is n	ot fully solid.
					else if (y > 0 && flag) return false; //Exit if there is anything in the way.
				}
			}
		}

		StructureTemplate template = ServerLifecycleHooks.getCurrentServer().getStructureManager().get(TEMPLATE_ID);
		Rotation rot = Rotation.getRandom(rand);
		int rotOrd = rot.ordinal();
		pos = pos.offset(rotOrd > 0 && rotOrd < 3 ? 8 : 0, 0, rotOrd > 1 ? 8 : 0);
		template.placeInWorld(world, pos, new StructurePlaceSettings().setRotation(rot), rand);
		DeadlyModule.debugLog(pos, "Tome Tower");
		return true;
	}

}
