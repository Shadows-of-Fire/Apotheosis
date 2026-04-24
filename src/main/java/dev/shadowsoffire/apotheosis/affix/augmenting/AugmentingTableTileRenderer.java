package dev.shadowsoffire.apotheosis.affix.augmenting;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingTableTile.AnimationStage;
import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class AugmentingTableTileRenderer implements BlockEntityRenderer<AugmentingTableTile, AugmentingTableTileRenderer.State> {

    public AugmentingTableTileRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AugmentingTableTile blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.time = blockEntity.time;
        state.partialTicks = partialTicks;
        state.stage = blockEntity.stage;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.stage == AnimationStage.HIDING) {
            return;
        }

        BlockStateModel model = Minecraft.getInstance().getModelManager().getStandaloneModel(AdventureModuleClient.STAR_CUBE_MODEL);
        if (model == null) {
            return;
        }

        float px = 1F / 16F;

        poseStack.pushPose();
        poseStack.translate(5F * px, 5F * px, 5F * px);

        switch (state.stage) {
            case HIDING -> {}
            case RISING -> {
                float progress = (state.time + state.partialTicks) / AugmentingTableTile.RISE_TIME;
                float rise = Mth.lerp(progress, 0.1F * px, 11F * px);
                poseStack.translate(0F, rise, 0F);
            }
            case FALLING -> {
                float progress = (AugmentingTableTile.RISE_TIME - state.time + state.partialTicks) / AugmentingTableTile.RISE_TIME;
                float rise = Mth.lerp(progress, 11F * px, 0.1F * px);
                poseStack.translate(0F, rise, 0F);
            }
            case SPINNING -> {
                float rotation = (state.time % 360 + state.partialTicks) * Mth.PI / 180F;
                poseStack.translate(0F, 11F * px, 0F);
                poseStack.translate(3F * px, 3F * px, 3F * px);
                poseStack.mulPose(new Quaternionf().rotationXYZ(rotation, 0F, rotation));
                poseStack.translate(-3F * px, -3F * px, -3F * px);
            }
        }

        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);

        submitNodeCollector.submitBlockModel(poseStack, Sheets.translucentBlockSheet(), parts, new int[] { -1 }, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        public int time;
        public float partialTicks;
        public AnimationStage stage;
    }

}
