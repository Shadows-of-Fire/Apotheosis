package shadows.apotheosis.deadly.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;

import java.util.Optional;

public class TomeTowerFeature extends Feature<NoneFeatureConfiguration> {

    public static final ResourceLocation TEMPLATE_ID = new ResourceLocation(Apotheosis.MODID, "tome_tower");
//    public static final TomeTowerFeature INSTANCE = new TomeTowerFeature();

    public TomeTowerFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {

        if (!DeadlyConfig.DIM_WHITELIST.contains((ctx.level().getLevel().dimension().location()))){
//                DeadlyModule.LOGGER.info("Cannot spawn tome tower, bad dimension");
            return false;
        }

        var pos = ctx.origin().offset(ctx.random().nextInt(5), -1, ctx.random().nextInt(5));

        if (ctx.level().getBlockState(pos).getBlock() == Blocks.SNOW) pos = pos.below();

        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos blockpos = pos.offset(x, y, z);
                    BlockState state = ctx.level().getBlockState(blockpos);
                    Material material = state.getMaterial();
                    boolean flag = material.isSolid();
                    if (y == 0 && !flag){
//                            DeadlyModule.LOGGER.error("floor is not fully solid. pos: {}", blockpos);
                        return false;
                    }
                    else if (y > 0 && flag) {
//                            DeadlyModule.LOGGER.error("Something in the way. pos: {}", blockpos);
                        return false;
                    }
                }
            }
        }

        Optional<StructureTemplate> template = ctx.level().getLevel().getStructureManager().get(TEMPLATE_ID);
        if(template.isEmpty()) {
            DeadlyModule.LOGGER.error("Failed to get template!");
            return false;
        }
        Rotation rot = Rotation.getRandom(ctx.random());
        int rotOrd = rot.ordinal();
        pos = pos.offset(rotOrd > 0 && rotOrd < 3 ? 8 : 0, 0, rotOrd > 1 ? 8 : 0);
        template.get().placeInWorld(ctx.level(), pos, pos, new StructurePlaceSettings().setRotation(rot), ctx.random(), Block.UPDATE_CLIENTS);
        DeadlyModule.debugLog(pos, "Tome Tower");
        return true;
    }
}

