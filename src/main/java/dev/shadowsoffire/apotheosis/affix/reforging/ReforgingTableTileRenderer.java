package dev.shadowsoffire.apotheosis.affix.reforging;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

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

public class ReforgingTableTileRenderer implements BlockEntityRenderer<ReforgingTableTile, ReforgingTableTileRenderer.State> {

    public ReforgingTableTileRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ReforgingTableTile blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.time = blockEntity.time;
        state.partialTicks = partialTicks;
        state.step1 = blockEntity.step1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getStandaloneModel(AdventureModuleClient.HAMMER_MODEL);
        if (model == null) {
            return;
        }

        float px = 1F / 16F;

        poseStack.pushPose();

        poseStack.scale(1.25F, 1.25F, 1.25F);
        poseStack.translate(8.5F * px / 1.25F, 16F * px / 1.25F - 0.015F, 7F * px / 1.25F);
        poseStack.mulPose(Axis.YP.rotationDegrees(45F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90F));

        float sinSq;
        if (state.step1) {
            float factor = state.time % 60 + state.partialTicks;
            float sin = Mth.sin(factor * Mth.PI / 120F);
            sinSq = sin * sin;
        }
        else {
            float factor = state.time % 5 + state.partialTicks;
            float sin = Mth.sin(Mth.HALF_PI + factor * Mth.PI / 10F);
            sinSq = sin * sin;
        }

        poseStack.translate(0.125F * sinSq, 0F, -0.15F * sinSq);
        poseStack.mulPose(Axis.YN.rotationDegrees(45F * sinSq));

        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);

        submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), parts, new int[] { -1 }, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        public int time;
        public float partialTicks;
        public boolean step1;
    }

}
