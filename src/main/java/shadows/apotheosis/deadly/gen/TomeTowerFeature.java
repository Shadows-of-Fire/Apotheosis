package shadows.apotheosis.deadly.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
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
        try {
            if (!checkDimension(ctx.level().getLevel().dimension())) throw new CustomException("Bad dimension");

            var pos = ctx.origin().offset(ctx.random().nextInt(5), -1, ctx.random().nextInt(5));

            if (ctx.level().getBlockState(pos).getBlock() == Blocks.SNOW) pos = pos.below();

            for (int x = 0; x < 9; x++) {
                for (int z = 0; z < 9; z++) {
                    for (int y = 0; y < 4; y++) {
                        BlockPos blockpos = pos.offset(x, y, z);
                        BlockState state = ctx.level().getBlockState(blockpos);
                        Material material = state.getMaterial();
                        boolean flag = material.isSolid();
                        if (y == 0 && !flag) throw new CustomException(String.format("floor is not fully solid. pos: %s", blockpos)); //Exit if the floor is not fully solid.
                        else if (y > 0 && flag) throw new CustomException(String.format("Something in the way. pos: %s", blockpos)); //Exit if there is anything in the way.
                    }
                }
            }

            Optional<StructureTemplate> template = ctx.level().getLevel().getStructureManager().get(TEMPLATE_ID);
            if(template.isEmpty()) throw new CustomException("Failed to get template!");
            Rotation rot = Rotation.getRandom(ctx.random());
            int rotOrd = rot.ordinal();
            pos = pos.offset(rotOrd > 0 && rotOrd < 3 ? 8 : 0, 0, rotOrd > 1 ? 8 : 0);
            template.get().placeInWorld(ctx.level(), pos, pos, new StructurePlaceSettings().setRotation(rot), ctx.random(), Block.UPDATE_CLIENTS);
            DeadlyModule.debugLog(pos, "Tome Tower");
            return true;
        }
        catch (CustomException ex) {
            DeadlyModule.LOGGER.debug("Failed to spawn Tome tower! Reason: {}", ex.getMessage());
        }
        return false;
    }

    static boolean checkDimension(ResourceKey<Level> key){
        return DeadlyConfig.DIM_WHITELIST.contains(key.location());
    }

    class CustomException extends Exception{
        public CustomException(String message){ super(message);}
    }
}

