package shadows.apotheosis.deadly.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;

public class TomeTowerFeature extends Feature<NoFeatureConfig> {

	public static final ResourceLocation TEMPLATE_ID = new ResourceLocation(Apotheosis.MODID, "tome_tower");
	public static final TomeTowerFeature INSTANCE = new TomeTowerFeature();

	public TomeTowerFeature() {
		super(NoFeatureConfig.field_236558_a_);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean generate(ISeedReader world, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig cfg) {
		if (!DeadlyConfig.canGenerateIn(world)) return false;

		pos = pos.add(rand.nextInt(5), -1, rand.nextInt(5));

		if (world.getBlockState(pos).getBlock() == Blocks.SNOW) pos = pos.down();

		for (int x = 0; x < 9; x++) {
			for (int z = 0; z < 9; z++) {
				for (int y = 0; y < 4; y++) {
					BlockPos blockpos = pos.add(x, y, z);
					BlockState state = world.getBlockState(blockpos);
					Material material = state.getMaterial();
					boolean flag = material.isSolid();
					if (y == 0 && !flag) return false; //Exit if the floor is n	ot fully solid.
					else if (y > 0 && flag) return false; //Exit if there is anything in the way.
				}
			}
		}

		Template template = ServerLifecycleHooks.getCurrentServer().getTemplateManager().getTemplate(TEMPLATE_ID);
		Rotation rot = Rotation.randomRotation(rand);
		int rotOrd = rot.ordinal();
		pos = pos.add(rotOrd > 0 && rotOrd < 3 ? 8 : 0, 0, rotOrd > 1 ? 8 : 0);
		template.func_237152_b_(world, pos, new PlacementSettings().setRotation(rot), rand);
		DeadlyModule.debugLog(pos, "Tome Tower");
		return true;
	}

}
